package org.voltdb.importclient.kafka10;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.voltcore.logging.VoltLogger;
import org.voltdb.importer.AbstractImporter;
import org.voltdb.utils.CSVDataLoader;

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


        int consumerCount = 3; // NEEDSWORK: Pass this through config

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
                m_consumers.add(new Kafka10ConsumerRunner(this, m_config, consumer));
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

}
