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

import org.voltcore.utils.HBBPool;
import org.voltcore.utils.HBBPool.SharedBBContainer;

public class SPIfromSerializedContainer extends SPIfromSerialization {

    /*
     * This ByteBuffer is accessed from multiple threads concurrently.
     * Always duplicate it before reading
     */
    private SharedBBContainer serializedParams = null;

    @Override
    public ByteBuffer GetUnsafeSerializedBBParams() {
        // This will not bump the refcount of the container so the container could be reused
        // while holding this copy of the bytebuffer if we are not careful
        return serializedParams.b().duplicate();
    }

    @Override
    public ByteBuffer GetSafeSerializedBBParams() {
        ByteBuffer copy = ByteBuffer.allocate(m_serializedParamSize);
        copy.put(serializedParams.b().array(), serializedParams.b().arrayOffset(), m_serializedParamSize);
        copy.flip();
        return copy;
    }

    public void setSerializedParams(SharedBBContainer serializedParams) {
        assert(this.serializedParams == null);
        assert(serializedParams.b().position() == 0);
        this.m_serializedParamSize = serializedParams.b().limit();
        this.serializedParams = serializedParams;
    }

    protected void initFromParameterSet(ParameterSet params) throws IOException {
        String initTag;
        if (serializedParams != null) {
            // deallocate existing params and swap with new params
            assert(serializedParams.m_tags.size() == 1);
            initTag = serializedParams.m_tags.iterator().next();
            serializedParams.discard(initTag);
            serializedParams = null;
        }
        else {
            initTag = "Params";
        }
        SharedBBContainer newSerialization = HBBPool.allocateHeapAndPool(m_serializedParamSize, initTag);
        params.flattenToBuffer(newSerialization.b());
        newSerialization.b().flip();
        setSerializedParams(newSerialization);
    }

    @Override
    public StoredProcedureInvocation getShallowCopy()
    {
        SPIfromSerializedContainer copy = new SPIfromSerializedContainer();
        commonShallowCopy(copy);
        copy.serializedParams = serializedParams.duplicate("Params");
        copy.m_serializedParamSize = m_serializedParamSize;

        return copy;
    }

    public void initFromContainer(SharedBBContainer container, String tag) throws IOException
    {
        ByteBuffer buf = container.b();
        genericInit(buf);
        // do not deserialize parameters in ClientInterface context
        setSerializedParams(container.slice(tag));
    }

    /*
     * Store a copy of the parameters to the procedure in serialized form.
     * In a cluster there is no reason to throw away the serialized bytes
     * because it will be forwarded in most cases and there is no need to repeat the work.
     * Command logging also takes advantage of this to avoid reserializing the parameters.
     * In some cases the params will never have been serialized (null) because
     * the SPI is generated internally. A duplicate view of the buffer is returned
     * to make access thread safe. Can't return a read only view because ByteBuffer.array()
     * is invoked by the command log.
     */
    public SharedBBContainer getSerializedParams() {
        assert(serializedParams != null);
        return serializedParams.duplicate("Params");
    }

    @Override
    public void implicitReference(String tag) {
        serializedParams.implicitReference(tag);
    }

    @Override
    public void discard(String tag) {
        assert(serializedParams != null);
        serializedParams.discard(tag);
    }

    public boolean parametersSerialized() {
        return serializedParams != null;
    }
}
