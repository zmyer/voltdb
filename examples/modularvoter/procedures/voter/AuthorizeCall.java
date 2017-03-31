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

package voter;

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;

public class AuthorizeCall extends VoltProcedure {

    // FIXME: too much copy and paste from ContinueCall


    // TODO: this needs to be synced with the stored procedures
    public static final byte STORED_PROC_SUCCESS = 1;
    public static final byte STORED_PROC_FAILURE = -1; // TODO this is not observed by anyone - it's just not 1




    private static final SQLStmt getAccountIDStmt = new SQLStmt("SELECT account_id FROM phones WHERE phone_number = ?;");
    private static final SQLStmt getCurrentMinutesStmt = new SQLStmt("SELECT minutes_left FROM accounts_realtime WHERE account_id = ?;");
    private static final SQLStmt updateCurrentMinutesStmt = new SQLStmt("UPDATE accounts_realtime SET minutes_left = ? WHERE account_id = ?;");

    public long run( long phoneNumber ) throws VoltAbortException {

        voltQueueSQL(getAccountIDStmt, EXPECT_SCALAR_LONG, phoneNumber);
        final long accountID = voltExecuteSQL()[0].asScalarLong(); // fetchRow(0).getLong("account_id");

        // TODO care about the account type after initialization as the DDL does; right now we rely on "unlimited" being a huge value.

        voltQueueSQL(getCurrentMinutesStmt, EXPECT_SCALAR_LONG, accountID);
        final long currentMinutes = voltExecuteSQL()[0].asScalarLong();

        if (currentMinutes > 0){
            voltQueueSQL(updateCurrentMinutesStmt, EXPECT_SCALAR_LONG, currentMinutes - 1, accountID);
            voltExecuteSQL();

            // TODO write call log - FIXME this is not in the DDL

            setAppStatusCode(STORED_PROC_SUCCESS);
        } else {
            // ran out of minutes
            setAppStatusCode(STORED_PROC_FAILURE);
        }

        return 0; // TODO does it matter what the return code is?
    }

}


