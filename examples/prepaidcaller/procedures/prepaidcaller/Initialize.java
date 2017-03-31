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

//
// Initializes the database, pushing the list of contestants and documenting domain data (Area codes and States).
//

package prepaidcaller;

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;

import java.util.Random;


public class Initialize extends VoltProcedure
{
    // Domain data: number of digits in a US/CA phone number
    // Our simulation only handles a single area code, for simplicity's sake.
    public static final long NUM_PHONES_PER_AREA_CODE = 10000000L;

    // tuning parameters
    public static final int MAX_PHONES_PER_ACCOUNT = 4;

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

    public final SQLStmt getNumPhonesStmt = new SQLStmt("SELECT COUNT(*) FROM phones;");
    public final SQLStmt getNumAccountsStmt = new SQLStmt("SELECT COUNT(*) FROM accounts_realtime;");
    public final SQLStmt newAccountTypeStmt = new SQLStmt("INSERT INTO account_types VALUES (?, ?);");
    public final SQLStmt newAccountStmt = new SQLStmt("INSERT INTO accounts_realtime VALUES (?, ?, ?, 1);");
    public final SQLStmt newPhoneStmt = new SQLStmt("INSERT INTO phones VALUES (?, ?, null, null, 1);");

    int accountPhoneNumberCounter = 0;
    int accountMaxPhoneNumbers = 0;
    long accountIDCounter;
    AccountType accountType;

    private void generateAccount(){
        accountPhoneNumberCounter++;
        if (accountPhoneNumberCounter >= accountMaxPhoneNumbers){
            accountPhoneNumberCounter = 0;
            accountIDCounter++;  // reserve 0 as an invalid value

            Random rng = getSeededRandomNumberGenerator();
            accountMaxPhoneNumbers = rng.nextInt(MAX_PHONES_PER_ACCOUNT);
            accountType = AccountType.getRandom(rng);

            voltQueueSQL(newAccountStmt, EXPECT_SCALAR_LONG, accountIDCounter, (byte) accountType.key, accountType.maxMinutes);
        }
    }

    private void generatePhone(long phoneNumberIndex){
        generateAccount();
        final long phoneNumber = phoneNumberIndex;
        voltQueueSQL(newPhoneStmt, EXPECT_SCALAR_LONG, phoneNumber, accountIDCounter);
        voltExecuteSQL();
    }

    private long howManyAccountsExist(){
        voltQueueSQL(getNumAccountsStmt, EXPECT_SCALAR_LONG);
        return voltExecuteSQL()[0].asScalarLong();
    }

    private long howManyPhonesExist(){
        voltQueueSQL(getNumPhonesStmt, EXPECT_SCALAR_LONG);
        return voltExecuteSQL()[0].asScalarLong();
    }


    public long run(final long numPhones) throws VoltAbortException {

        final long numExistingPhones = howManyPhonesExist();
        if (numPhones <= numExistingPhones){
            return 0;
        }
        if (numPhones >= NUM_PHONES_PER_AREA_CODE){
            throw new VoltAbortException(
                    "LIMITATION: Since generated phone numbers aren't checked for duplicates, " +
                    "cannot generate more than " + NUM_PHONES_PER_AREA_CODE + " phone numbers.");
        }

        final long numExistingAccounts = howManyAccountsExist();
        accountIDCounter = numExistingAccounts;

        if (numExistingAccounts == 0){
            // set up account types
            for (AccountType type : AccountType.class.getEnumConstants()){
                voltQueueSQL(newAccountTypeStmt, (byte) type.key, type.maxMinutes);
            }
            voltExecuteSQL();
        }

        for (long i = numExistingPhones; i < numPhones; i++){
            generatePhone(i + 1); // reserve 000-0000 as an invalid value
        }
        return numPhones - numExistingPhones;
    }
}
