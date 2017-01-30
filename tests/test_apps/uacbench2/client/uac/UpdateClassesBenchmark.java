/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
 *
 * This file contains original code and/or modifications of original code.
 * Any modifications made by VoltDB Inc. are licensed under the following
 * terms and conditions:
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package uac;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.voltdb.CLIConfig;
import org.voltdb.VoltTable;
import org.voltdb.client.Client;
import org.voltdb.client.ClientConfig;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ClientStatsContext;
import org.voltdb.client.ClientStatusListenerExt;
import org.voltdb.client.NoConnectionsException;
import org.voltdb.client.NullCallback;
import org.voltdb.client.ProcCallException;

public class UpdateClassesBenchmark {

    // handy, rather than typing this out several times
    static final String HORIZONTAL_RULE =
            "----------" + "----------" + "----------" + "----------" +
            "----------" + "----------" + "----------" + "----------" + "\n";

    // validated command line configuration
    final UpdateClassesConfig config;
    // Reference to the database connection we will use
    final Client client;

    // Statistics manager objects from the client
    final ClientStatsContext fullStatsContext;

    /**
     * Uses included {@link CLIConfig} class to
     * declaratively state command line options with defaults
     * and validation.
     */
    static class UpdateClassesConfig extends CLIConfig {
        @Option(desc = "Comma separated list of the form server[:port] to connect to.")
        String servers = "localhost";

        @Option(desc = "Number of invocations.")
        int steps = 15;

        @Option(desc = "Number of tables")
        int tablecount = 1000;

        @Option(desc = "Stored procedure count")
        int procedurecount = 20;

        @Option(desc = "Extra class procedure count")
        int classcount = 1000;

        @Option(desc = "batch size count")
        int batchsize = 1;

        @Option(desc = "base working direcotry")
        String dir = "default";

        @Option(desc = "Filename to write raw summary statistics to.")
        String statsfile = "stats";

        @Override
        public void validate() {
            if (procedurecount <= 0) exitWithMessageAndUsage("procedure number must be greater than 0");
        }
    }

    /**
     * Provides a callback to be notified on node failure.
     * This example only logs the event.
     */
    class StatusListener extends ClientStatusListenerExt {
        @Override
        public void connectionLost(String hostname, int port, int connectionsLeft, DisconnectCause cause) {
            // if the benchmark is still active
            System.err.printf("Connection to %s:%d was lost.\n", hostname, port);
        }
    }

    /**
     * Constructor for benchmark instance.
     * Configures VoltDB client and prints configuration.
     *
     * @param config Parsed & validated CLI options.
     */
    public UpdateClassesBenchmark(UpdateClassesConfig config) {
        this.config = config;

        ClientConfig clientConfig = new ClientConfig("", "", new StatusListener());
        //snapshot restore needs > default 2 minute timeout
        clientConfig.setProcedureCallTimeout(0);
        clientConfig.setMaxOutstandingTxns(300);

        client = ClientFactory.createClient(clientConfig);

        fullStatsContext = client.createStatsContext();

        System.out.print(HORIZONTAL_RULE);
        System.out.println(" Command Line Configuration");
        System.out.println(HORIZONTAL_RULE);
        System.out.println(config.getConfigDumpString());
    }

    /**
     * Connect to a single server with retry. Limited exponential backoff.
     * No timeout. This will run until the process is killed if it's not
     * able to connect.
     *
     * @param server hostname:port or just hostname (hostname can be ip).
     */
    void connectToOneServerWithRetry(String server) {
        int sleep = 1000;
        while (true) {
            try {
                client.createConnection(server);

                break;
            }
            catch (Exception e) {
                System.err.printf("Connection failed - retrying in %d second(s).\n", sleep / 1000);
                try { Thread.sleep(sleep); } catch (Exception interruted) {}
                if (sleep < 8000) sleep += sleep;
            }
        }
        System.out.printf("Connected to VoltDB node at: %s.\n", server);
    }

    /**
     * Connect to a set of servers in parallel. Each will retry until
     * connection. This call will block until all have connected.
     *
     * @param servers A comma separated list of servers using the hostname:port
     * syntax (where :port is optional).
     * @throws InterruptedException if anything bad happens with the threads.
     */
    void connect(String servers) throws InterruptedException {
        System.out.println("Connecting to VoltDB...");

        String[] serverArray = servers.split(",");
        final CountDownLatch connections = new CountDownLatch(serverArray.length);

        // use a new thread to connect to each server
        for (final String server : serverArray) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connectToOneServerWithRetry(server);
                    connections.countDown();
                }
            }).start();
        }
        // block until all have connected
        connections.await();
    }

    static long doUpdateClassesWork(Client client, String prevStmts, byte[] jar, String delPattern, String stmts)
            throws IOException, NoConnectionsException, ProcCallException {
        long startTS = System.nanoTime();

        if (prevStmts != null && prevStmts.length() > 0) {
            client.callProcedure("@AdHoc", prevStmts);
        }

        if (jar != null) {
            client.callProcedure("@UpdateClasses", jar, delPattern);
        }

        if (stmts != null && stmts.length() > 0) {
            client.callProcedure("@AdHoc", stmts);
        }

        return System.nanoTime() - startTS;
    }

    static byte[] readFileIntoByteArray(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    static double toMillis(long nanos) {
        return nanos * 1.0 / 1000 / 1000;
    }

    static String readClassDeletePattern(String filePath) throws IOException {
        String[] delClasses = new String(readFileIntoByteArray(filePath)).split("\n");

        String result = "";
        for (int i = 0; i < delClasses.length; i++) {
            result += delClasses[i];
            if (i != delClasses.length - 1) {
                result += ",";
            }
        }
        return result;
    }

    static class BenchmarkResult {
        long min;
        long max;
        long sum;

        BenchmarkResult() {
            min = Long.MAX_VALUE;
            max = Long.MIN_VALUE;
            sum = 0;
        }
    }

    static void runAdd(Client client, UpdateClassesConfig config, String jarPath) throws Exception {
        BenchmarkResult res = new BenchmarkResult();
        while (true) {
            for (int i = 0; i < config.steps; i++) {
                String path = jarPath + "uac_" + String.valueOf(i) + ".jar";
                byte[] jarBytes = readFileIntoByteArray(path);
                long duration = doUpdateClassesWork(client, null, jarBytes, null, null);
                res.sum += duration;
                if (duration > res.max) res.max = duration;
                if (duration < res.min) res.min = duration;
                System.out.printf("Moved to step %d in %dms\n", i, duration / 1000000);
                Thread.sleep(5 * 60 * 1000);
            }
            for (int i = config.steps - 2; i > 0; i--) {
                String path = jarPath + "uac_" + String.valueOf(i) + ".jar";
                byte[] jarBytes = readFileIntoByteArray(path);
                long duration = doUpdateClassesWork(client, null, jarBytes, null, null);
                res.sum += duration;
                if (duration > res.max) res.max = duration;
                if (duration < res.min) res.min = duration;
                System.out.printf("Moved to step %d in %dms\n", i, duration / 1000000);
                Thread.sleep(5 * 60 * 1000);
            }
        }
        //return res;
    }

    /**
     * Core benchmark code.
     * Connect. Initialize. Run the loop. Cleanup. Print Results.
     *
     * @throws Exception if anything unexpected happens.
     */
    public void runBenchmark() throws Exception {
        // connect to one or more servers, loop until success
        connect(config.servers);

        FileWriter fw = null;
        if ((config.statsfile != null) && (config.statsfile.length() != 0)) {
            fw = new FileWriter(config.statsfile);
        }

        System.out.print(HORIZONTAL_RULE);

        VoltTable vt = null;
        String dirPath = config.dir;
        String jarPath = dirPath + "/jars/";
        byte[] jarBytes = readFileIntoByteArray(jarPath + "uac_0.jar");
        // setup the base case: 500 tables with 1000 procedures
        String stmts = new String(readFileIntoByteArray("ddlbase.sql"));
        long duration = doUpdateClassesWork(client, null, jarBytes , null, stmts);
        System.out.println(String.format("Created %d procedure using %f ms",
                config.procedurecount, toMillis(duration)));

        final Random rand = new Random();

        Thread t = new Thread() {
            @Override
            public void run() {
                while(true) {
                    for (int i = 0; i < config.procedurecount; i++) {
                        try {
                            client.callProcedure(new NullCallback(), "Proc" + String.valueOf(i), rand.nextLong());
                            //System.out.print(".");
                            //System.out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        t.start();

        /*BenchmarkResult res = */runAdd(client, config, jarPath);

        /*double avg = toMillis(res.sum / config.steps);
        System.out.printf("\n(Benchmark ran %d times in average %f ms, min %f ms, max %f ms)\n",
                config.steps, avg, toMillis(res.min), toMillis(res.max));

        //retrieve stats
        ClientStats stats = fullStatsContext.fetch().getStats();
        // write stats to file
        //client.writeSummaryCSV(stats, config.statsfile);

        // name, duration,invocations/tps,latmin,latmax,lat95,lat99
        fw.append(String.format("-1,%f,0,0,%f,%f,0,0,0,0,0,0\n",
                                avg,
                                toMillis(res.min),
                                toMillis(res.max)
                                ));*/

        fw.flush();
        fw.close();
        // block until all outstanding txns return
        client.drain();
        // close down the client connections
        client.close();
    }

    /**
     * Main routine creates a benchmark instance and kicks off the run method.
     *
     * @param args Command line arguments.
     * @throws Exception if anything goes wrong.
     * @see {@link UpdateClassesConfig}
     */
    public static void main(String[] args) throws Exception {
        // create a configuration from the arguments
        UpdateClassesConfig config = new UpdateClassesConfig();
        config.parse(UpdateClassesBenchmark.class.getName(), args);

        UpdateClassesBenchmark benchmark = new UpdateClassesBenchmark(config);
        benchmark.runBenchmark();
    }
}
