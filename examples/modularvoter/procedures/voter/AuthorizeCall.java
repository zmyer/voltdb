package voter;

import org.voltdb.VoltProcedure;

public class AuthorizeCall extends VoltProcedure {


    // TODO: this needs to be synced with the stored procedures
    public static final byte STORED_PROC_SUCCESS = 1;


    public AuthorizeCall() {
        // TODO Auto-generated constructor stub
    }

    public long run( long uniqueID ) throws VoltAbortException {


        // TODO need to check if unique ID has an account
        setAppStatusCode(STORED_PROC_SUCCESS);
        return 0; // TODO this tests if app status code is different!
    }

}
