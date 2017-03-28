package voter;

import org.voltdb.VoltProcedure;
import org.voltdb.VoltProcedure.VoltAbortException;

public class EndCall extends VoltProcedure {

    // TODO: this needs to be synced with the stored procedures
    public static final byte STORED_PROC_SUCCESS = 1;

    public long run( long uniqueID ) throws VoltAbortException {

        // TODO this needs to register the end call in the call logs
        setAppStatusCode(STORED_PROC_SUCCESS);
        return 0; // TODO this tests if app status code is different!
    }
}
