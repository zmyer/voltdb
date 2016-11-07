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

package org.voltdb.messaging;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.voltcore.messaging.VoltMessage;
import org.voltcore.network.NIOReadStream;
import org.voltcore.network.VoltProtocolHandler;
import org.voltcore.utils.HBBPool.SharedBBContainer;
import org.voltdb.ClientResponseImpl;

public class Dr2MultipartResponseMessage extends VoltMessage {

    private boolean m_drain;
    private int m_producerPID;
    private ClientResponseImpl m_response;

    Dr2MultipartResponseMessage() {
        super();
    }

    public Dr2MultipartResponseMessage(int producerPID, ClientResponseImpl response)  {
        m_drain = false;
        m_producerPID = producerPID;
        m_response = response;
    }

    public static Dr2MultipartResponseMessage createDrainMessage(int producerPID) {
        final Dr2MultipartResponseMessage msg = new Dr2MultipartResponseMessage();
        msg.m_drain = true;
        msg.m_producerPID = producerPID;
        msg.m_response = null;
        return msg;
    }

    public int getProducerPID() {
        return m_producerPID;
    }

    public ClientResponseImpl getResponse() {
        return m_response;
    }

    public boolean isDrain() {
        return m_drain;
    }

    @Override
    protected void initFromBuffer(ByteBuffer buf) throws IOException {
        assert(false);
    }

    @Override
    protected void initFromContainer(SharedBBContainer container)
            throws IOException {
        ByteBuffer buf = container.b();
        m_drain = buf.get() == 1;
        m_producerPID = buf.getInt();
        if (buf.remaining() > 0) {
            m_response = new ClientResponseImpl();
            m_response.initFromBuffer(buf);
        }
        assert(buf.limit() == buf.position());
        container.discard(getClass().getSimpleName());
    }

    @Override
    public void flattenToBuffer(ByteBuffer buf) throws IOException {
        buf.put(VoltDbMessageFactory.DR2_MULTIPART_RESPONSE_ID);
        buf.put((byte) (m_drain ? 1 : 0));
        buf.putInt(m_producerPID);

        if (!m_drain) {
            m_response.flattenToBuffer(buf);
        }

        assert(buf.capacity() == buf.position());
        buf.limit(buf.position());
    }

    @Override
    public int getSerializedSize() {
        int size = super.getSerializedSize()
                   + 1  // drain or not
                   + 4; // producer partition ID
        if (!m_drain) {
            size += m_response.getSerializedSize();
        }
        return size;
    }

    @Override
    protected void initFromInputHandler(VoltProtocolHandler handler,
            NIOReadStream inputStream) throws IOException {
        initFromContainer(handler.getNextHBBMessage(inputStream, getClass().getSimpleName()));
    }

    @Override
    public void implicitReference(String tag) {}

    @Override
    public void discard(String tag) {}
}
