/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
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

package prepaidcaller;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.voltdb.client.*;

public class CellTower2 {

    /** NOTE: One "minute" in the DDL is represented with one second of wall clock time.
     * This allows the simulation to run much quicker.
     */
    private static final int MIN_CALL_TIME_SECONDS = 2;
    private static final int MAX_CALL_TIME_SECONDS = 10;
    private static final int MIN_TIME_BETWEEN_CALLS_SECONDS = 2;
    private static final int MAX_TIME_BETWEEN_CALLS_SECONDS = 30;
    private static final int KEEPALIVE_INTERVAL_SECONDS = 1;


    static enum CallState {
        // normal states - as long as caller is here, it will continue to make calls
        INACTIVE,
        PENDING,
        ACTIVE,
        ENDING,
        // benign failure states - no more calls will be placed
        REJECTED,
        DROPPED,
        // error states - if a caller ever ends up in one of these, it does not try to recover
        FAILED,
    };


    class PrepaidCaller {
        // TODO: this needs to be synced with the stored procedures
        public static final byte STORED_PROC_SUCCESS = 1;

        private long phoneNumber;
        private volatile CallState state; // modified by current executor, but also read by stats keeper
        private volatile boolean callScheduled; // set to true by scheduleCall(), set to false by CallEndedTask
        private int callSecondsRemaining;

        /** Asks VoltDB to initiate a call, generating a CallStartResponse.
         */
        class CallInitiateRequest implements Runnable {

            @Override
            public void run(){
                assert (state == CallState.INACTIVE);
                if (!m_stop){
                    boolean processed = false;
                    try {
                        state = CallState.PENDING; // this has to be set first; callProcedure may return after callback is called.
                        // TODO need to add call logging support
                        //processed = voltClient.callProcedure(new CallStartResponse(), "AuthorizeCall", phoneNumber);
                        processed = voltClient.callProcedure(new CallStartResponse(), "ContinueCall", phoneNumber);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!processed){
                        state = CallState.FAILED;
                    }
                }
            }
        }

        /** After VoltDB has responded to a call initiation request, schedules keep-alives or logs that the call failed.
         * @author bshaw
         */
        class CallStartResponse implements ProcedureCallback {

            @Override
            public void clientCallback(ClientResponse clientResponse) {
                assert (state == CallState.PENDING);
                if (clientResponse.getStatus() == ClientResponse.SUCCESS){
                    if (clientResponse.getAppStatus() == STORED_PROC_SUCCESS){
                        state = CallState.ACTIVE;
                        executor.schedule(new CallAliveRequest(), KEEPALIVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
                    } else {
                        state = CallState.REJECTED;
                    }
                } else {
                    state = CallState.FAILED;
                }
            }
        }

        /** Asks VoltDB whether or not the call should continue, generating a CallContinuationResponse.
         */
        class CallAliveRequest implements Runnable {
            @Override
            public void run() {
                assert (state == CallState.ACTIVE);
                if (!m_stop){
                    boolean processed = false;
                    try {
                        processed = voltClient.callProcedure(new CallContinuationResponse(), "ContinueCall", phoneNumber);
                    } catch (IOException e) {
                    }
                    if (!processed){
                        state = CallState.FAILED;
                    }
                } else {
                    // simulation shutdown; voluntarily end the call
                    new CallCompletedTask().run();
                }
            }
        }

        /** After VoltDB has responded to a call continuation request, keep the call alive or ends it. */
        class CallContinuationResponse implements ProcedureCallback {

            @Override
            public void clientCallback(ClientResponse clientResponse) {
                assert (state == CallState.ACTIVE);
                if (clientResponse.getStatus() == ClientResponse.SUCCESS){
                    if (clientResponse.getAppStatus() == STORED_PROC_SUCCESS){
                        callSecondsRemaining--;
                        if (callSecondsRemaining > 1){
                            executor.schedule(new CallAliveRequest(), KEEPALIVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
                        } else {
                            executor.schedule(new CallCompletedTask(), callSecondsRemaining, TimeUnit.SECONDS);
                        }
                    } else {
                        state = CallState.DROPPED;
                    }
                } else {
                    // Database not reachable; drop the call.
                    state = CallState.FAILED;
                }
            }
        }

        /** Informs VoltDB that a call has ended, generating a CallEndedResponse. */
        class CallCompletedTask implements Runnable {

            @Override
            public void run(){
                assert (state == CallState.ACTIVE);
                state = CallState.ENDING;
                boolean processed = false;
                try {
                    processed = voltClient.callProcedure(new CallEndedResponse(), "EndCall", phoneNumber);
                } catch (IOException e) {
                }
                if (!processed){
                    state = CallState.FAILED;
                }
            }
        }

        /** After VoltDB has responded to an end call request, mark this Caller inactive so it can make another call. */
        class CallEndedResponse implements ProcedureCallback {

            @Override
            public void clientCallback(ClientResponse clientResponse){
                assert (state == CallState.ENDING);

                if (clientResponse.getStatus() == ClientResponse.SUCCESS){
                    if (clientResponse.getAppStatus() == STORED_PROC_SUCCESS){
                        state = CallState.INACTIVE;
                        callScheduled = false; // allows a new call to be scheduled by the main() thread.
                    } else {
                        state = CallState.FAILED;
                    }
                } else {
                    state = CallState.FAILED;
                }
            }
        }

        public CallState queryState(){
            return state;
        }

        /** Asynchronously queries current state of this caller.
         * @return State of the phone call
         */
        public PrepaidCaller( long phoneNumber ){
            this.phoneNumber = phoneNumber;
            this.state = CallState.INACTIVE;
            this.callScheduled = false;
            this.callSecondsRemaining = 0;
        }

        public void scheduleCall(){
            if (!callScheduled){
                callScheduled = true;
                callSecondsRemaining = generateCallDurationSeconds();
                executor.schedule(new CallInitiateRequest(), generateCallDelaySeconds(), TimeUnit.SECONDS);
            }
        }
    }

    private final Client voltClient;
    private final ScheduledExecutorService executor;
    private long phoneNumberCounter;
    private final List<PrepaidCaller> callerList = new Vector<PrepaidCaller>();
    private volatile boolean m_stop = false; // set to true by stop()
    private final ClientStatsContext statistics;


    CellTower2(Client client, long phoneNumberStart){
        voltClient = client;
        executor = Executors.newSingleThreadScheduledExecutor();
        phoneNumberCounter = phoneNumberStart;
        statistics = client.createStatsContext();
    }

    private static int generateCallDurationSeconds(){
        return ThreadLocalRandom.current().nextInt(MIN_CALL_TIME_SECONDS, MAX_CALL_TIME_SECONDS);
    }

    private static int generateCallDelaySeconds(){
        return ThreadLocalRandom.current().nextInt(MIN_TIME_BETWEEN_CALLS_SECONDS, MAX_TIME_BETWEEN_CALLS_SECONDS);
    }


    void addCaller(int totalDurationSeconds){
        PrepaidCaller caller = new PrepaidCaller( phoneNumberCounter );
        phoneNumberCounter++;
        callerList.add(caller);
        caller.scheduleCall();
    }

    void scheduleMoreCalls(){
        for (PrepaidCaller caller : callerList){
            // this will not schedule a call unless the most recent one successfully completed
            caller.scheduleCall();
        }
    }

    public void stop(){
        m_stop = true;
    }

    public void cleanup(){
        // NOTE: This will cancel all scheduled "New Call" tasks - and that is OK.
        executor.shutdownNow();
        try {
            voltClient.drain();
            voltClient.close();
        } catch (InterruptedException | IOException e){
            // ignore but print an error since this is unexpected
            e.printStackTrace();
        }
    }

    public void printVoltStats() {
        final String HORIZONTAL_RULE =
            "----------" + "----------" + "----------" + "----------" +
            "----------" + "----------" + "----------" + "----------" + "\n";

        ClientStats stats = statistics.fetch().getStats();
        System.out.print(HORIZONTAL_RULE);
        System.out.println(" Client Workload Statistics");
        System.out.println(HORIZONTAL_RULE);

        System.out.printf("Average throughput:            %,9d txns/sec\n", stats.getTxnThroughput());

        System.out.printf("Average latency:               %,9.2f ms\n", stats.getAverageLatency());
        System.out.printf("10th percentile latency:       %,9.2f ms\n", stats.kPercentileLatencyAsDouble(.1));
        System.out.printf("25th percentile latency:       %,9.2f ms\n", stats.kPercentileLatencyAsDouble(.25));
        System.out.printf("50th percentile latency:       %,9.2f ms\n", stats.kPercentileLatencyAsDouble(.5));
        System.out.printf("75th percentile latency:       %,9.2f ms\n", stats.kPercentileLatencyAsDouble(.75));
        System.out.printf("90th percentile latency:       %,9.2f ms\n", stats.kPercentileLatencyAsDouble(.9));
        System.out.printf("95th percentile latency:       %,9.2f ms\n", stats.kPercentileLatencyAsDouble(.95));
        System.out.printf("99th percentile latency:       %,9.2f ms\n", stats.kPercentileLatencyAsDouble(.99));
        System.out.printf("99.5th percentile latency:     %,9.2f ms\n", stats.kPercentileLatencyAsDouble(.995));
        System.out.printf("99.9th percentile latency:     %,9.2f ms\n", stats.kPercentileLatencyAsDouble(.999));
        System.out.printf("99.99th percentile latency:    %,9.2f ms\n", stats.kPercentileLatencyAsDouble(.9999));
        System.out.printf("Server Internal Avg latency:   %,9.2f ms\n", stats.getAverageInternalLatency());

        System.out.print("\n" + HORIZONTAL_RULE);
        System.out.println(" Latency Histogram");
        System.out.println(HORIZONTAL_RULE);
        System.out.println(stats.latencyHistoReport());
    }

    private void computeStats( Map<CallState, Integer> stateCount ){
        for (PrepaidCaller caller : callerList){
            CallState callerState = caller.queryState();
            stateCount.put(callerState, stateCount.get(callerState) + 1);
        }
    }

    public static void printStats(Collection<CellTower2> cellTowerList){
        Map<CallState, Integer> stateCount = new EnumMap<CallState, Integer>(CallState.class);
        for (CallState state : CallState.class.getEnumConstants()){
            stateCount.put(state, 0);
        }
        for (CellTower2 cellTower : cellTowerList){
            cellTower.computeStats(stateCount);
        }
        for (Map.Entry<CallState, Integer> entry : stateCount.entrySet()){
            System.out.println("    " + entry.getKey().toString() + ": " + entry.getValue().toString());
        }
    }

    // FIXME lots of data duplication in this file - clean it up if I decide to eliminate the stored proc Initialize

    static enum AccountType {
        UNLIMITED      ('U', 0,    true),
        PREMIUM        ('P', 1000, false),
        STANDARD       ('S', 200,  false),
        EMERGENCY_ONLY ('E', 10,   false),
        RIPOFF         ('R', 0,    false),
        ;

        final char    key;
        final int     maxMinutes;

        AccountType( char key, int maxMinutes, boolean isUnlimited ){
            this.key = key;
            this.maxMinutes = isUnlimited ? Integer.MAX_VALUE : maxMinutes;
        }

        static AccountType getRandom( Random random ){
            return AccountType.values()[random.nextInt(AccountType.values().length)];
        }
    }


    /** After VoltDB has responded to a call continuation request, keep the call alive or ends it. */
    static class SuccessChecker implements ProcedureCallback {

        @Override
        public void clientCallback(ClientResponse clientResponse) {
            if (clientResponse.getStatus() != ClientResponse.SUCCESS){
                System.err.println("Procedure could not be executed");
                System.exit(1);
            }
        }

        static final SuccessChecker theSingleton = new SuccessChecker();
    }


    // Domain data: number of digits in a US/CA phone number
    // Our simulation only handles a single area code, for simplicity's sake.
    public static final long NUM_PHONES_PER_AREA_CODE = 10000000L;

    // tuning parameters
    public static final int MAX_PHONES_PER_ACCOUNT = 4;


    static class Initializer {
        int accountPhoneNumberCounter = 0;
        int accountMaxPhoneNumbers = 0;
        long accountIDCounter;
        AccountType accountType;

        private void generateAccount(Client voltClient) throws IOException, ProcCallException {
            accountPhoneNumberCounter++;
            if (accountPhoneNumberCounter >= accountMaxPhoneNumbers){
                accountPhoneNumberCounter = 0;
                accountIDCounter++;  // reserve 0 as an invalid value

                accountMaxPhoneNumbers = ThreadLocalRandom.current().nextInt(MAX_PHONES_PER_ACCOUNT);
                accountType = AccountType.getRandom(ThreadLocalRandom.current());

                voltClient.callProcedure(SuccessChecker.theSingleton, "NewAccount", accountIDCounter, (byte) accountType.key, accountType.maxMinutes);
                //voltClient.callProcedure("NewAccount", accountIDCounter, (byte) accountType.key, accountType.maxMinutes);
            }
        }

        private void generatePhone(Client voltClient, long phoneNumberIndex) throws IOException, ProcCallException {
            generateAccount(voltClient);
            final long phoneNumber = phoneNumberIndex;
            voltClient.callProcedure(SuccessChecker.theSingleton, "NewPhone", phoneNumber, accountIDCounter);
            //voltClient.callProcedure("NewPhone", phoneNumber, accountIDCounter);
        }

        void initialize(Client voltClient, int numPhones) throws IOException, ProcCallException {

            if (numPhones >= NUM_PHONES_PER_AREA_CODE){
                throw new RuntimeException(
                        "LIMITATION: Since generated phone numbers aren't checked for duplicates, " +
                        "cannot generate more than " + NUM_PHONES_PER_AREA_CODE + " phone numbers.");
            }

            accountIDCounter = 0; // FIXME this is for testing; Initialize() queries the DB for existing number of phones and accounts (maybe it shouldn't?).

            for (long i = 0; i < numPhones; i++){
                generatePhone(voltClient, i + 1); // reserve 000-0000 as an invalid value
            }
        }
    }

    public static void main(String[] args){
        final int NUM_VOLT_CLIENTS = 2;
        final int NUM_CALLERS_PER_EXECUTOR = 500000; // the memory problem is not exhibited
        //final int NUM_CALLERS_PER_EXECUTOR = 50000;
        final int TEST_DURATION_SECONDS = 45;

        List<CellTower2> cellTowerList = new Vector<CellTower2>();

        try {
            for (int clientIdx = 0; clientIdx < NUM_VOLT_CLIENTS; clientIdx++){
                Client voltClient = ClientFactory.createClient();
                voltClient.createConnection("localhost");
                final long startingPhoneNumber = clientIdx * NUM_CALLERS_PER_EXECUTOR + 1; // reserve phone number of 0
                CellTower2 cellTower = new CellTower2(voltClient, startingPhoneNumber);
                cellTowerList.add(cellTower);
            }

            final int totalCallers = NUM_VOLT_CLIENTS * NUM_CALLERS_PER_EXECUTOR;
            System.out.println("Initializing simulation with " + totalCallers + " callers.");
            new Initializer().initialize(cellTowerList.get(0).voltClient, totalCallers);
            //cellTowerList.get(0).voltClient.drain();

            System.out.println("Simulation initialized - scheduling phone calls");
            for (int i = 0; i < NUM_CALLERS_PER_EXECUTOR; i++){
                for (CellTower2 cellTower : cellTowerList){
                    cellTower.addCaller(TEST_DURATION_SECONDS);
                }
            }

            System.out.println("Simulation running");
            for (int i = 0; i < TEST_DURATION_SECONDS; i++){
                for (CellTower2 cellTower : cellTowerList){
                    cellTower.scheduleMoreCalls();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // should not happen; ignore
                }
                System.out.println("Statistics at " + (i + 1) + " of " + TEST_DURATION_SECONDS + " seconds:");
                printStats(cellTowerList);
            }

            System.out.println("Waiting for calls to complete");
            for (CellTower2 cellTower : cellTowerList){
                cellTower.stop();
            }

            final int WAIT_TIME_SECS = KEEPALIVE_INTERVAL_SECONDS * 2;
            for (int i = 0; i < WAIT_TIME_SECS - 1; i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // should not happen; ignore
                }
                System.out.println("Statistics at " + (i + 1) + " of " + WAIT_TIME_SECS + " seconds:");
                printStats(cellTowerList);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // should not happen; ignore
            }
            System.out.println("Final statistics:");
            printStats(cellTowerList);

        } catch (IOException e){
            e.printStackTrace();
            System.err.println("Could not connect to VoltDB at " + "localhost");
            System.exit(-1);
        } catch (ProcCallException e){
            e.printStackTrace();
            System.err.println("Could not execute procedure");
            System.exit(-1);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            for (CellTower2 cellTower : cellTowerList){
                cellTower.printVoltStats();
                cellTower.cleanup();
            }
        }
    }
}
