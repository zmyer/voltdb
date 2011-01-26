/* This file is part of VoltDB.
 * Copyright (C) 2008-2011 VoltDB Inc.
 *
 * VoltDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VoltDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.voltdb.exportclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import org.voltdb.client.ConnectionUtil;
import org.voltdb.export.ExportProtoMessage;
import org.voltdb.export.ExportProtoMessage.AdvertisedDataSource;
import org.voltdb.logging.VoltLogger;
import org.voltdb.messaging.FastDeserializer;
import org.voltdb.utils.Pair;

/**
 * Manage the connection to a single server's export port
 */

public class ExportConnection {

    private static final VoltLogger m_logger = new VoltLogger("ExportClient");

    static final private int CLOSED = 0;
    static final private int CONNECTING = 1;
    static final private int CONNECTED = 2;
    static final private int CLOSING = 3;

    private int m_state = CLOSED;

    public final String name;
    private final InetSocketAddress serverAddr;
    private SocketChannel m_socket;

    private final String m_username;
    private final String m_password;

    // cached reference to ELClientBase's collection of ELDataSinks
    private final HashMap<Long, HashMap<Integer, ExportDataSink>> m_sinks;

    public final ArrayList<AdvertisedDataSource> dataSources;

    private long m_lastAckOffset;

    public ExportConnection(
            String username, String password,
            InetSocketAddress serverAddr,
            HashMap<Long, HashMap<Integer, ExportDataSink>> dataSinks)
    {
        m_username = username != null ? username : "";
        m_password = password != null ? password : "";
        m_sinks = dataSinks;
        this.serverAddr = serverAddr;
        name = serverAddr.toString();
        dataSources = new ArrayList<AdvertisedDataSource>();
    }

    /**
     * Open the Export connection to the processor on m_socket.  This will
     * currently block until it receives the OPEN RESPONSE from the server
     * that contains the information about the available data sources
     */
    public void openExportConnection() throws IOException
    {
        m_logger.info("Starting EL Client socket to: " + name);
        byte hashedPassword[] = ConnectionUtil.getHashedPassword(m_password);
        Object[] cxndata =
            ConnectionUtil.
            getAuthenticatedExportConnection(serverAddr.getHostName(),
                                             m_username,
                                             hashedPassword,
                                             serverAddr.getPort());

        m_socket = (SocketChannel) cxndata[0];

        if (m_state == CLOSED) {
            open();
            m_state = CONNECTING;
        }

        while (m_state == CONNECTING) {
            ExportProtoMessage m = nextMessage();
            if (m != null && m.isOpenResponse())
            {
                Pair<ArrayList<AdvertisedDataSource>,ArrayList<String>> advertisement;
                advertisement = m.getAdvertisedDataSourcesAndNodes();
                dataSources.addAll(advertisement.getFirst());
                m_state = CONNECTED;
            }
        }
    }

    public void closeExportConnection() throws IOException {
        if (m_socket.isConnected()) {
            m_socket.close();
        }
        // seems hard to argue with...
        m_state = CLOSED;

        // perhaps a more controversial assertion?
        dataSources.clear();
    }

    /**
     * Retrieve the connected-ness of this EL Connection
     * @return true if in CONNECTED state
     */
    public boolean isConnected()
    {
        return (m_state == CONNECTED);
    }

    /**
     * perform a single iteration of work for the EL connection.
     * @return the number of server messages offered to the rxqueue(s).
     */
    public int work()
    {
        int messagesOffered = 0;

        // exportxxx need better error handling code in here
        if (!m_socket.isConnected() || !m_socket.isOpen()) {
            m_state = CLOSING;
        }

        if (m_state == CLOSING) {
            return messagesOffered;
        }

        // loop here to empty RX ?
        // receive data from network and hand to the proper ELProtocolHandler RX queue
        ExportProtoMessage m = null;
        do {
            try {
                m = nextMessage();
            } catch (IOException e) {
                m_logger.error("Socket error: " + e.getMessage());
                m_state = CLOSING;
            }

            if (m != null && m.isError()) {
                // XXX handle error from server, just die for now
                m_state = CLOSING;
            }

            // exportxxx need better error handling code in here
            if (!m_socket.isConnected() || !m_socket.isOpen()) {
                m_state = CLOSING;
            }

            if (m != null && m.isPollResponse()) {
                m_lastAckOffset = m.getAckOffset();
                ExportDataSink rx_sink = m_sinks.get(m.getTableId()).get(m.getPartitionId());
                rx_sink.getRxQueue(name).offer(m);
                messagesOffered++;
            }
        }
        while (m_state == CONNECTED && m != null);

        // service all the ELDataSink TX queues
        for (HashMap<Integer, ExportDataSink> part_map : m_sinks.values()) {
            for (ExportDataSink tx_sink : part_map.values()) {
                Queue<ExportProtoMessage> tx_queue =
                    tx_sink.getTxQueue(name);
                // this connection might not be connected to every sink
                if (tx_queue != null) {
                    // XXX loop to drain the tx queue?
                    ExportProtoMessage tx_m =
                        tx_queue.poll();
                    if (tx_m != null) {
                        try {
                            sendMessage(tx_m);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return messagesOffered;
    }

    private void open() throws IOException
    {
        m_logger.info("Opening new EL stream connection.");
        ExportProtoMessage m = new ExportProtoMessage(-1, -1);
        m.open();
        sendMessage(m);
    }

    public ExportProtoMessage nextMessage() throws IOException
    {
        FastDeserializer fds;
        final ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        int bytes_read = 0;
        do {
            bytes_read = m_socket.read(lengthBuffer);
        }
        while (lengthBuffer.hasRemaining() && bytes_read > 0);

        if (bytes_read < 0) {
            // Socket closed, try to bail out
            m_state = CLOSING;
            return null;
        }

        if (bytes_read == 0) {
            if (lengthBuffer.position() != 0 && m_socket.isConnected()) {
                // we're committed now, baby
                do {
                    bytes_read = m_socket.read(lengthBuffer);
                }
                while (lengthBuffer.hasRemaining() && bytes_read >= 0);

                if (bytes_read < 0) {
                    //  Socket closed, try to bail out
                    m_state = CLOSING;
                    return null;
                }
            }
            else {
                // non-blocking case
                return null;
            }
        }

        lengthBuffer.flip();
        int length = lengthBuffer.getInt();
        ByteBuffer messageBuf = ByteBuffer.allocate(length);
        do {
            bytes_read = m_socket.read(messageBuf);
        }
        while (messageBuf.remaining() > 0 && bytes_read >= 0);

        if (bytes_read < 0) {
            //  Socket closed, try to bail out
            m_state = CLOSING;
            return null;
        }
        messageBuf.flip();
        fds = new FastDeserializer(messageBuf);
        ExportProtoMessage m = ExportProtoMessage.readExternal(fds);
        return m;
    }

    public void sendMessage(ExportProtoMessage m) throws IOException
    {
        ByteBuffer buf = m.toBuffer();
        while (buf.remaining() > 0) {
            m_socket.write(buf);
        }
    }

    public long getLastAckOffset() {
        return m_lastAckOffset;
    }
}
