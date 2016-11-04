/* This file is part of VoltDB.
 * Copyright (C) 2008-2016 VoltDB Inc.
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

package org.voltdb;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;
import org.junit.Test;
import org.voltdb.VoltDB.Configuration;
import org.voltdb.client.Client;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;
import org.voltdb.compiler.VoltProjectBuilder;
import org.voltdb.regressionsuites.LocalCluster;
import org.voltdb.utils.VoltFile;

import com.google_voltpatches.common.collect.Maps;

public class TestAddReplicaSite {

    static final int K = 1;
    static final int SPH_FAKE = 2;
    static final int HOST_COUNT = 3;
    static final String JAR_NAME = "addsite.jar";
    static final String JAR_NAME_NO_SCHEMA = "addsite-no-schema.jar";

    @Test
    public void testAddSiteWithSchema() throws UnknownHostException, InterruptedException, IOException, ProcCallException {
        // just use it to fool VoltDB compiler, use overrides CLI option to provide actual sites per host
        final VoltProjectBuilder builder = new VoltProjectBuilder();
        builder.addLiteralSchema("CREATE TABLE V0 (id BIGINT);");
        builder.configureLogging(null, null, false, false, 200, Integer.MAX_VALUE, null);
        assertTrue(builder.compile(Configuration.getPathToCatalogForTest(JAR_NAME), SPH_FAKE, HOST_COUNT, K));
        runTest(builder, JAR_NAME, true);
    }

    @Test
    public void testAddSiteWithNoSchema() throws UnknownHostException, InterruptedException, IOException, ProcCallException {
        // just use it to fool VoltDB compiler, use overrides CLI option to provide actual sites per host
        final VoltProjectBuilder builder = new VoltProjectBuilder();
        builder.configureLogging(null, null, false, false, 200, Integer.MAX_VALUE, null);
        assertTrue(builder.compile(Configuration.getPathToCatalogForTest(JAR_NAME_NO_SCHEMA), SPH_FAKE, HOST_COUNT, K));
        runTest(builder, JAR_NAME_NO_SCHEMA, false);
    }

    private void runTest(VoltProjectBuilder builder, String jar, boolean loadData) throws InterruptedException, UnknownHostException, IOException, ProcCallException {

        VoltFile.recursivelyDelete(new File("/tmp/" + System.getProperty("user.name")));
        File f = new File("/tmp/" + System.getProperty("user.name"));
        f.mkdirs();

        LocalCluster cluster = new LocalCluster(
                jar,
                2,
                3,
                K,
                BackendTarget.NATIVE_EE_JNI);

        //use mixed site per host, total of 10 sites in the cluster, k=1
        Map<Integer, Integer> sphMap = Maps.newHashMap();
            sphMap.put(0, 4);
            sphMap.put(1, 3);
            sphMap.put(2, 3);
        cluster.setOverridesForSitesperhost(sphMap);

        cluster.setHasLocalServer(false);
        cluster.setDeploymentAndVoltDBRoot(
                builder.getPathToDeployment(),
                builder.getPathToVoltRoot().getAbsolutePath());
        cluster.startUp();
        Client client = ClientFactory.createClient();

        try {
            //connect to host 1
            client.createConnection("localhost", cluster.port(1));

            //load some data
            if (loadData) {
                for (int i = 0; i < 10000; i++) {
                    client.callProcedure("@AdHoc", "insert into V0 values(" + i + ")");
                }
            }

            //check topology, partition 2 and 3 are on 2 sites.
            ClientResponse resp = client.callProcedure("@Statistics", "TOPO", 0);
            assert(resp.getStatus() == ClientResponse.SUCCESS);
            VoltTable topo = resp.getResults()[0];
            while (topo.advanceRow()) {
                int partitionId = (int)topo.getLong("Partition");
                if ( partitionId == 2 || partitionId == 3) {
                    String[] sites = topo.getString("sites").split(",");
                    assert(sites.length == 2);
                }
            }
            System.out.println("topo:" + topo.toString());

            //add one site on host 1 for partition 2
            client.callProcedure("@Replicate", 2, 1);

            //give time for the rejoin to complete
            Thread.sleep(80000);
            resp = client.callProcedure("@Statistics", "TOPO", 0);
            assert(resp.getStatus() == ClientResponse.SUCCESS);
            topo = resp.getResults()[0];
            while (topo.advanceRow()) {
                //partition 1 is on all 3 sites now
                if ((int)topo.getLong("Partition") == 2) {
                    String[] sites = topo.getString("sites").split(",");
                    assert(sites.length == 3);
                    break;
                }
            }
            System.out.println("topo:" + topo.toString());

            //add one site on host 1 for partition 3
            client.callProcedure("@Replicate", 3, 1);

            //give time for the rejoin to complete
            Thread.sleep(80000);
            resp = client.callProcedure("@Statistics", "TOPO", 0);
            assert(resp.getStatus() == ClientResponse.SUCCESS);
            topo = resp.getResults()[0];
            while (topo.advanceRow()) {
                //both partition 2 and 3  are on 3 sites now
                int partitionId = (int)topo.getLong("Partition");
                if ( partitionId == 2 || partitionId == 3) {
                    String[] sites = topo.getString("sites").split(",");
                    assert(sites.length == 3);
                }
            }
            System.out.println("topo:" + topo.toString());

            //load more data
            if (loadData) {
                for (int i = 10000; i < 20000; i++) {
                    client.callProcedure("@AdHoc", "insert into V0 values(" + i + ")");
                }
                resp = client.callProcedure("@AdHoc", "select count(*) from V0");
                assert(resp.getStatus() == ClientResponse.SUCCESS);
                topo = resp.getResults()[0];
                topo.advanceRow();
                assert(topo.getLong(0) == 20000);
            }
        } finally {
            try { client.close();} catch (Exception e) {}
            cluster.shutDown();
        }
    }
}
