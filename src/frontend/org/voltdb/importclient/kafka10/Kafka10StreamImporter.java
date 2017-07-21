package org.voltdb.importclient.kafka10;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.voltcore.logging.VoltLogger;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;
import org.voltdb.importer.AbstractImporter;
import org.voltdb.importer.Invocation;
import org.voltdb.importer.formatter.FormatException;
import org.voltdb.importer.formatter.Formatter;
import org.voltdb.utils.CSVDataLoader;

import au.com.bytecode.opencsv_voltpatches.CSVParser;

public class Kafka10StreamImporter extends AbstractImporter {

    private static final VoltLogger m_log = new VoltLogger("KAFKA10IMPORTER");

    private Kafka10StreamImporterConfig m_config;

    public Kafka10StreamImporter(Kafka10StreamImporterConfig config) {
        super();
        m_config = config;

    }

    @Override
    public String getName() {
       return Kafka10StreamImporter.class.getName();
    }

    @Override
    public URI getResourceID() {
        return m_config.getURI();
    }

    @Override
    protected void accept() {

        // NEEDSWORK: Lifecycle

        try {
            m_executorService = getExecutor();
            m_executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Close, etc?
        }
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    // NEEDSWORK: Hook up lifecycle, stop, error handling, etc.
    private List<Kafka10ConsumerRunner> m_consumers;
    private CSVDataLoader m_loader;
    private ExecutorService m_executorService;

    private ExecutorService getExecutor() throws Exception {

        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, m_config.getGroupId());
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, m_config.getBrokers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"); // NEEDSWORK: Hack for now


        int consumerCount = 3; //m_cliOptions.kpartitions; // NEEDSWORK: Pass this through config

        String consumerAssignorStrategy = org.apache.kafka.clients.consumer.RangeAssignor.class.getName(); // "partition.assignment.strategy"
        props.put("partition.assignment.strategy", consumerAssignorStrategy); // NEEDSWORK: Put this in the config properties, with default

        ExecutorService executor = Executors.newFixedThreadPool(consumerCount, new ThreadFactory() {
            private int threadNum = 0;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Kafka_Consumer_" + m_config.getTopic() + "_" + threadNum++); // NEEDSWORK: Account for a list of topics
            }
        });

        m_consumers = new ArrayList<Kafka10ConsumerRunner>();

        try {
            ClassLoader previous = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            for (int i = 0; i < consumerCount; i++) {
               KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(props); // NEEDSWORK: Make this a factory, so we can use the MockConsumer in unit tests
                m_consumers.add(new Kafka10ConsumerRunner(consumer));
            }
            Thread.currentThread().setContextClassLoader(previous);
        } catch (Throwable terminate) {
            terminate.printStackTrace(); // NEEDSWORK: Logging
            for (Kafka10ConsumerRunner consumerRunner : m_consumers) {
                // close all consumer connections
                consumerRunner.forceClose();
            }
            return null;
        }

        for (Kafka10ConsumerRunner consumerRunner : m_consumers) {
            executor.submit(consumerRunner);
        }
        return executor;
    }


    class Kafka10ConsumerRunner implements Runnable {

        private KafkaConsumer<byte[], byte[]> m_consumer;
        private CSVDataLoader m_loader;
        private CSVParser m_csvParser;
        private Formatter m_formatter;
        private AtomicBoolean m_closed = new AtomicBoolean(false);

        Kafka10ConsumerRunner(KafkaConsumer<byte[], byte[]> consumer)
                throws FileNotFoundException, IOException, ClassNotFoundException, NoSuchMethodException,
                SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
                InvocationTargetException {
//            m_loader = loader;
            m_csvParser = new CSVParser();
//            if (m_config.m_formatterProperties.size() > 0) {
//                String formatter = m_config.m_formatterProperties.getProperty("formatter");
//                String format = m_config.m_formatterProperties.getProperty("format", "csv");
//                Class classz = Class.forName(formatter);
//                Class[] ctorParmTypes = new Class[]{ String.class, Properties.class };
//                Constructor ctor = classz.getDeclaredConstructor(ctorParmTypes);
//                Object[] ctorParms = new Object[]{ format, m_config.m_formatterProperties };
//                m_formatter = (Formatter )ctor.newInstance(ctorParms);
//            } else {
//                m_formatter = null;
//            }
            m_consumer = consumer;
        }

        void forceClose() {
            m_closed.set(true);
            try {
                m_consumer.close();
            } catch (Exception ignore) {}
        }

        void shutdown() {
            if (m_closed.compareAndSet(false,  true)) {
                m_consumer.wakeup();
            }

        }

        // NEEDSWORK: Clean this up
        ProcedureCallback procedureCallback = new ProcedureCallback() {
            @Override
            public void clientCallback(ClientResponse clientResponse) throws Exception {
                System.out.println("In clientCallback! clientResponse=" + clientResponse.getStatusString());
            }
        };

        @Override
        public void run() {
            String smsg = null;
            try {
                m_consumer.subscribe(Arrays.asList(m_config.getTopic()));
                while (!m_closed.get()) {
                    ConsumerRecords<byte[], byte[]> records = m_consumer.poll(0); // NEEDSWORK: pollTimedWaitInMilliSec);
                    for (ConsumerRecord<byte[], byte[]> record : records) {
                        byte[] msg  = record.value();
                        long offset = record.offset();
                        smsg = new String(msg);
                        Object params[];
                        if (m_formatter != null) {
                            try {
                                params = m_formatter.transform(ByteBuffer.wrap(msg));
                            } catch (FormatException badMsg) {
                                m_log.warn("Failed to transform message " + smsg + " at offset " + offset
                                        + ", error message: " + badMsg.getMessage());
                                continue;
                            }
                        } else {
                            params = m_csvParser.parseLine(smsg);
                        }
                        if (params == null) continue;
                        callProcedure(new Invocation(m_config.getProcedure(), params), procedureCallback);
                        // NEEDSWORK: Hook this up later m_loader.insertRow(new RowWithMetaData(smsg, offset), params);
                    }
                }
            } catch (IllegalArgumentException invalidTopic) {
                m_closed.set(true);
                m_log.error("Failed subscribing to the topic " + m_config.getTopic(), invalidTopic);
            } catch (WakeupException wakeup) {
                m_closed.set(true);
                m_log.debug("Consumer signalled to terminate ", wakeup);
            } catch (IOException ioExcp) {
                m_closed.set(true);
                if (m_formatter == null) {
                    m_log.error("Failed to parse message" + smsg);
                } else {
                    m_log.error("Error seen when processing message ", ioExcp);
                }
            } catch (Throwable terminate) {
                m_closed.set(true);
                m_log.error("Error seen during poll", terminate);
            } finally {
                try {
                    m_consumer.close();
                } catch (Exception ignore) {}
               // NEEDSWORK:  notifyShutdown();
            }
        }
    }


}
