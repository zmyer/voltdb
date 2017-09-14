/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;

import org.voltcore.utils.DBBPool.BBContainer;
import org.voltcore.utils.Pair;
import org.voltdb.utils.VoltTableUtil;

/*
 * Filter that converts snapshot data to CSV format
 */
public class AVROSnapshotFilter implements SnapshotDataFilter {

    private final byte m_schemaBytes[];
    private final ArrayList<VoltType> m_columnTypes;
    private final ArrayList<String> m_columnNames;
    private final Schema m_schema;
    private final String m_tableName;
    private byte[] m_sync;

    public AVROSnapshotFilter(
            String tableName,
            VoltTable vt) {
        m_tableName = tableName.toUpperCase();
        m_columnTypes = new ArrayList<>(vt.getColumnCount());
        m_columnNames = new ArrayList<>(vt.getColumnCount());
        for (int ii = 0; ii < vt.getColumnCount(); ii++) {
            m_columnTypes.add(vt.getColumnType(ii));
            m_columnNames.add(vt.getColumnName(ii));
        }
        int i = 0;

        SchemaBuilder.FieldAssembler<Schema> fa =
                SchemaBuilder.record(m_tableName).namespace("voltdb-snapshot").fields();

        for (VoltType type : m_columnTypes) {
            String name = m_columnNames.get(i++).toUpperCase();
            if (null != type) switch (type) {
                case BIGINT:
                    fa = fa.requiredLong(name);
                    break;
                case TINYINT:
                    fa = fa.requiredInt(name);
                    break;
                case SMALLINT:
                    fa = fa.requiredInt(name);
                    break;
                case INTEGER:
                    fa = fa.requiredInt(name);
                    break;
                case FLOAT:
                    fa = fa.requiredDouble(name);
                    break;
                case DECIMAL:
                    fa = fa.requiredDouble(name);
                    break;
                case STRING:
                    fa = fa.requiredString(name);
                    break;
                case TIMESTAMP:
                    fa = fa.requiredLong(name);
                    break;
                case VARBINARY:
                    fa = fa.requiredBytes(name);
                    break;
                case GEOGRAPHY_POINT:
                    fa = fa.requiredString(name);
                case GEOGRAPHY:
                    fa = fa.requiredString(name);
                default:
                    throw new IllegalArgumentException("Type for AVRO snapshot is not supported: " + type);
            }
        }
        m_schema = fa.endRecord();
        System.out.println("Schema is: " + m_schema.toString());
        try {
          MessageDigest digester = MessageDigest.getInstance("MD5");
          long time = System.currentTimeMillis();
          digester.update((UUID.randomUUID()+"@"+time).getBytes());
          m_sync = digester.digest();
        } catch (NoSuchAlgorithmException e) {
          throw new RuntimeException(e);
        }

        m_schemaBytes = PrivateVoltTableFactory.getSchemaBytes(vt);
    }

    @Override
    public Callable<BBContainer> filter(final Callable<BBContainer> input) {
        return new Callable<BBContainer>() {
            @Override
            public BBContainer call() throws Exception {
                BBContainer cont = input.call();
                if (cont == null) {
                    return null;
                }
                try {
                    ByteBuffer buf = ByteBuffer.allocate(m_schemaBytes.length + cont.b().remaining() - 4);
                    buf.put(m_schemaBytes);
                    cont.b().position(4);
                    buf.put(cont.b());

                    VoltTable vt = PrivateVoltTableFactory.createVoltTableFromBuffer(buf, true);
                    final ByteArrayOutputStream bbos = new ByteArrayOutputStream();
                    GenericDatumWriter<GenericRecord> dwriter = new GenericDatumWriter<>(m_schema);
                    final DataFileWriter<GenericRecord> fileWriter = new DataFileWriter<>(dwriter);
                    //Set codec before fileWriter is created.
                    fileWriter.setCodec(CodecFactory.snappyCodec());
                    fileWriter.create(m_schema, bbos, m_sync);
                    fileWriter.setFlushOnEveryBlock(false);

                    GenericData.Record to = new GenericData.Record(m_schema);
                    while (vt.advanceRow()) {
                        Pair<String[], Object[]> l = VoltTableUtil.valuesAsList(vt, m_columnTypes);
                        for (int ii = 0; ii < l.getFirst().length; ii++) {
                            String key = l.getFirst()[ii];
                            Object val = l.getSecond()[ii];
                            to.put(key, val);
                        }
                        fileWriter.append(to);
                        fileWriter.flush();
                    }
                    final BBContainer origin = cont;
                    cont = null;
                    BBContainer retVal = new BBContainer(ByteBuffer.wrap(bbos.toByteArray())) {
                        @Override
                        public void discard() {
                            checkDoubleFree();
                            origin.discard();
                        }
                    };
                    return retVal;
                } finally {
                    if (cont != null) {
                        cont.discard();
                    }
                }
            }
        };
    }

}
