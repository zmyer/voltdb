/* This file is part of VoltDB.
 * Copyright (C) 2008-2016 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.voltdb;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.voltcore.logging.VoltLogger;
import org.voltdb.catalog.Table;
import org.voltdb.client.BatchTimeoutOverrideType;
import org.voltdb.client.ProcedureInvocationExtensions;
import org.voltdb.client.ProcedureInvocationType;
import org.voltdb.common.Constants;
import org.voltdb.utils.SerializationHelper;

/**
 * Represents a serializeable bundle of procedure name and parameters. This
 * is the object that is sent by the client library to call a stored procedure.
 *
 * Note, the client (java) serializes a ProcedureInvocation, which is deserialized
 * by the server as a StoredProcedureInvocation. This avoids dragging some extra
 * code into the client. The point is that the serialization of these classes
 * need to be in sync.
 *
 * Note also there are a few places that need to be updated if the serialization
 * is changed. See getSerializedSize().
 *
 */
public abstract class StoredProcedureInvocation {
    protected static final VoltLogger hostLog = new VoltLogger("HOST");

    public static final byte CURRENT_MOST_RECENT_VERSION = ProcedureInvocationType.VERSION2.getValue();

    ProcedureInvocationType type = ProcedureInvocationType.ORIGINAL;
    protected String procName = null;
    protected byte m_procNameBytes[] = null;
    protected byte m_extensionCount = 0;

    public static final long UNITIALIZED_ID = -1L;

    protected Integer m_serializedParamSize = null;


    /** A descriptor provided by the client, opaque to the server,
        returned to the client in the ClientResponse */
    long clientHandle = -1;

    protected int m_batchTimeout = BatchTimeoutOverrideType.NO_TIMEOUT;
    private boolean m_allPartition = false;

    /**
     * Serialize and then deserialize an invocation so that it has serializedParams set for command logging if the
     * invocation is sent to a local site.
     * @return The round-tripped version of the invocation
     * @throws IOException
     */
    public abstract SPIfromSerialization roundTripForCL() throws IOException;

    public abstract StoredProcedureInvocation getShallowCopy();

    public void setProcName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("SPI setProcName(String name) doesn't accept NULL.");
        }
        procName = name;
        m_procNameBytes = procName.getBytes(Constants.UTF8ENCODING);
    }

    public void setProcName(byte[] name) {
        if (name == null) {
            throw new IllegalArgumentException("SPI setProcName(byte[] name) doesn't accept NULL.");
        }
        m_procNameBytes = name;
        procName = new String(m_procNameBytes, Constants.UTF8ENCODING);
    }

    public ProcedureInvocationType getType() {
        return type;
    }

    public String getProcName() {
        return procName;
    }

    public byte[] getProcNameBytes() {
        return m_procNameBytes;
    }

    public abstract ParameterSet getParams();

    public void setClientHandle(long aHandle) {
        clientHandle = aHandle;
    }

    public long getClientHandle() {
        return clientHandle;
    }

    public int getBatchTimeout() {
        return m_batchTimeout;
    }

    public void setBatchTimeout(int timeout) {
        m_batchTimeout = timeout;
    }

    public void setAllPartition(boolean allPartition) {
        m_allPartition = allPartition;
    }

    public boolean getAllPartition() {
        return m_allPartition;
    }

    abstract Object getParameterAtIndex(int partitionIndex);

    /**
     *
     * NOTE: If you change this method, you may have to fix
     * getLoadVoltTablesMagicSeriazlizedSize below too.
     * Also line 38 of PartitionDRGatewayImpl.java
     * Also line 30 of AbstactDRTupleStream.h
     * Also line 38 of InvocationBuffer.java
     */
    public int getSerializedSize()
    {
        // get extension sizes - if not present, size is 0 for each
        // 6 is one byte for ext type, one for size, and 4 for integer value
        int batchExtensionSize = m_batchTimeout != BatchTimeoutOverrideType.NO_TIMEOUT ? 6 : 0;
        // 2 is one byte for ext type, one for size
        int allPartitionExtensionSize = m_allPartition ? 2 : 0;

        // compute the size
        int size =
            1 + // type
            4 + getProcNameBytes().length + // procname
            8 + // client handle
            1 + // extension count
            batchExtensionSize + allPartitionExtensionSize + // extensions
            getSerializedParamSize(); // parameters
        assert(size > 0); // sanity

        // MAKE SURE YOU SEE COMMENT ON TOP OF METHOD!!!
        return size;
    }

    /**
     * Get the serialized size of this SPI in the original serialization version.
     * This is currently used by DR.
     */
    public int getSerializedSizeForOriginalVersion()
    {
        return (1 + // type
                4 + getProcNameBytes().length + // procname
                8 + // client handle
                getSerializedParamSize());
    }

    private int getSerializedParamSize()
    {
        if (m_serializedParamSize != null) {
            getParams();
        }
        return m_serializedParamSize;
    }

    /**
     * Hack for SyncSnapshotBuffer. Note that this is using the ORIGINAL (version 0) serialization format.
     * Moved to this file from that one so you might see it sooner than I did.
     * If you change the serialization, you have to change this too.
     */
    public static int getLoadVoltTablesMagicSeriazlizedSize(Table catTable, boolean isPartitioned) {

        // code below is used to compute the right value slowly
        /*
        StoredProcedureInvocation spi = new StoredProcedureInvocation();
        spi.setProcName("@LoadVoltTableSP");
        if (isPartitioned) {
            spi.setParams(0, catTable.getTypeName(), null);
        }
        else {
            spi.setParams(0, catTable.getTypeName(), null);
        }
        int size = spi.getSerializedSizeForOriginalVersion() + 4;
        int realSize = size - catTable.getTypeName().getBytes(Constants.UTF8ENCODING).length;
        System.err.printf("@LoadVoltTable** padding size: %d or %d\n", size, realSize);
        return size;
        */

        // Magic size of @LoadVoltTable* StoredProcedureInvocation
        int tableNameLengthInBytes =
                catTable.getTypeName().getBytes(Constants.UTF8ENCODING).length;
        int metadataSize = 41 + // serialized size for original version
                           tableNameLengthInBytes;
        if (isPartitioned) {
            metadataSize += 5;
        }
        return metadataSize;
    }


    protected void commonShallowCopy(StoredProcedureInvocation copy) {
        copy.type = type;
        copy.clientHandle = clientHandle;
        copy.procName = procName;
        copy.m_procNameBytes = m_procNameBytes;
        copy.m_extensionCount = m_extensionCount;
        copy.m_batchTimeout = m_batchTimeout;
    }

    protected void commonFlattenToBuffer(ByteBuffer buf) throws IOException {
     // write current format version only (we read all old formats)
        buf.put(CURRENT_MOST_RECENT_VERSION);

        SerializationHelper.writeVarbinary(getProcNameBytes(), buf);

        buf.putLong(clientHandle);

        // there are two possible extensions, count which apply
        byte extensionCount = 0;
        if (m_batchTimeout != BatchTimeoutOverrideType.NO_TIMEOUT) ++extensionCount;
        if (m_allPartition) ++extensionCount;
        // write the count as one byte
        buf.put(extensionCount);
        // write any extensions that apply
        if (m_batchTimeout != BatchTimeoutOverrideType.NO_TIMEOUT) {
            ProcedureInvocationExtensions.writeBatchTimeoutWithTypeByte(buf, m_batchTimeout);
        }
        if (m_allPartition) {
            ProcedureInvocationExtensions.writeAllPartitionWithTypeByte(buf);
        }
    }

    public abstract void flattenToBuffer(ByteBuffer buf) throws IOException;

    public abstract void implicitReference(String tag);

    public abstract void discard(String tag);

    @Override
    public String toString() {
        String retval = type.name() + " Invocation: " + procName + "(";
        ParameterSet params = getParams();
        if (params != null)
            for (Object o : params.toArray()) {
                retval += String.valueOf(o) + ", ";
            }
        else
            retval += "null";
        retval += ")";
        retval += " type=" + String.valueOf(type);
        retval += " batchTimeout=" + BatchTimeoutOverrideType.toString(m_batchTimeout);
        retval += " clientHandle=" + String.valueOf(clientHandle);

        return retval;
    }
}
