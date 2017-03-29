package voter;

import java.util.concurrent.ThreadLocalRandom;

import org.voltdb.client.Client;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;

public class UserGenerator {

    private static final String[] AREA_CODE_STRS = ("907,205,256,334,251,870,501,479" +
            ",480,602,623,928,520,341,764,628,831,925,909,562,661,510,650,949,760" +
            ",415,951,209,669,408,559,626,442,530,916,627,714,707,310,323,213,424" +
            ",747,818,858,935,619,805,369,720,303,970,719,860,203,959,475,202,302" +
            ",689,407,239,850,727,321,754,954,927,352,863,386,904,561,772,786,305" +
            ",941,813,478,770,470,404,762,706,678,912,229,808,515,319,563,641,712" +
            ",208,217,872,312,773,464,708,224,847,779,815,618,309,331,630,317,765" +
            ",574,260,219,812,913,785,316,620,606,859,502,270,504,985,225,318,337" +
            ",774,508,339,781,857,617,978,351,413,443,410,301,240,207,517,810,278" +
            ",679,313,586,947,248,734,269,989,906,616,231,612,320,651,763,952,218" +
            ",507,636,660,975,816,573,314,557,417,769,601,662,228,406,336,252,984" +
            ",919,980,910,828,704,701,402,308,603,908,848,732,551,201,862,973,609" +
            ",856,575,957,505,775,702,315,518,646,347,212,718,516,917,845,631,716" +
            ",585,607,914,216,330,234,567,419,440,380,740,614,283,513,937,918,580" +
            ",405,503,541,971,814,717,570,878,835,484,610,267,215,724,412,401,843" +
            ",864,803,605,423,865,931,615,901,731,254,325,713,940,817,430,903,806" +
            ",737,512,361,210,979,936,409,972,469,214,682,832,281,830,956,432,915" +
            ",435,801,385,434,804,757,703,571,276,236,540,802,509,360,564,206,425" +
            ",253,715,920,262,414,608,304,307").split(",");

    // convert the area code array to a list of digits
    private static final long[] AREA_CODES = new long[AREA_CODE_STRS.length];
    static {
        for (int i = 0; i < AREA_CODES.length; i++)
            AREA_CODES[i] = Long.parseLong(AREA_CODE_STRS[i]);
    }

    /*
INSERT INTO account_types VALUES ('U', null); -- Unlimited Plan
INSERT INTO account_types VALUES ('P', 5000); -- Premium Plan
INSERT INTO account_types VALUES ('S', 1000); -- Standard Plan
INSERT INTO account_types VALUES ('E', 30);   -- Emergency Only Plan (helps test running out of minutes)
INSERT INTO account_types VALUES ('R', 5);    -- Ripoff Plan (for testing running out of minutes)
     */

    static enum AccountType {
        UNLIMITED      ('U', 0,    true),
        PREMIUM        ('P', 1000, false),
        STANDARD       ('S', 200,  false),
        EMERGENCY_ONLY ('E', 30,   false),
        RIPOFF         ('R', 5,    false),
        ;

        final char    key;
        final int     maxMinutes;
        final boolean isUnlimited;

        AccountType( char key, int numMinutes, boolean isUnlimited ){
            this.key = key;
            this.maxMinutes = numMinutes;
            this.isUnlimited = isUnlimited;
        }

        static AccountType getRandom(){
            return AccountType.values()[ThreadLocalRandom.current().nextInt(0, AccountType.values().length)];
        }


    }


    static class SuccessChecker implements ProcedureCallback {
        @Override
        public void clientCallback(ClientResponse clientResponse) throws Exception {
            if (clientResponse.getStatus() != ClientResponse.SUCCESS){
                System.err.println("Procedure failed: " + clientResponse.getStatusString());
            }
        }

        static final SuccessChecker theSingleton = new SuccessChecker();
    }

    Client voltClient;
    long accountIDCounter = 1; // make 0 a reserved value
    long phoneNumberCounter = 1;

    private void generateNewAccount(){
        final long accountID = accountIDCounter++;
        final AccountType accountType = AccountType.getRandom();
        final char accountTypeAsChar = accountType.key;
        final int minutesLeft = accountType.isUnlimited ? Integer.MAX_VALUE : accountType.maxMinutes;

        final int numPhoneLines = ThreadLocalRandom.current().nextInt(4);
        long[] phoneNumberList = new long[numPhoneLines];

        for (int i = 0; i < numPhoneLines; i++){
            phoneNumberList[i] = phoneNumberCounter++;
        }

        voltClient.callProcedure(SuccessChecker.theSingleton, "NewAccount", accountID, accountTypeAsChar, minutesLeft, phoneNumberList);
    }


    public static void main(String[] args) {
        // TODO Auto-generated method stub



        final int NUM_ACCOUNTS = 1000000;
        /*
        CREATE TABLE accounts_realtime
        (
            account_id    integer  NOT NULL PRIMARY KEY,
            account_type  char     NOT NULL, -- see 'account_types' table
            minutes_left  integer  NOT NULL,
            enabled       boolean  NOT NULL, -- set to false if they try something malicious or report that their info was stolen
            valid_numbers bigint[] NOT NULL,
        );
        */



    }

}
