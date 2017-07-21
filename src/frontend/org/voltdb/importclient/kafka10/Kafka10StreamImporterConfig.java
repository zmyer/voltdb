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
package org.voltdb.importclient.kafka10;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.voltdb.importclient.ImportBaseException;
import org.voltdb.importclient.kafka.KafkaImporterCommitPolicy;
import org.voltdb.importer.ImporterConfig;
import org.voltdb.importer.formatter.FormatterBuilder;

/**
 * Holds configuration information required to connect to a single partition for a topic.
 */
public class Kafka10StreamImporterConfig implements ImporterConfig
{
    // NEEDSWORK: Lots of consolidation/subclassing can happen here. Clean up the factory (static) method in favor of constructor, too?


    private static final Logger m_logger = Logger.getLogger("IMPORT");

    public static final String CLIENT_ID = "voltdb-importer";
    private static final String GROUP_ID = "voltdb";
    private static final int KAFKA_DEFAULT_BROKER_PORT = 9092;

    // We don't allow period in topic names because we construct URIs using it
    private static final Pattern legalTopicNamesPattern = Pattern.compile("[a-zA-Z0-9\\_-]+");
    private static final int topicMaxNameLength = 255;

    private URI m_uri;

    //private List<HostAndPort> m_brokers;
    private String m_brokers;
    private String m_topics;
    private String m_groupId;
    private int m_fetchSize;
    private int m_soTimeout;
    private String m_procedure;
    private int m_consumers; // NEEDSWORK: Default

    private FormatterBuilder m_formatterBuilder;
    private KafkaImporterCommitPolicy m_commitPolicy;
    private long m_triggerValue;

    public Kafka10StreamImporterConfig(Properties properties, FormatterBuilder formatterBuilder) {

        // NEEDSWORK: Better way to parse properties
        String brokers = properties.getProperty("brokers", "").trim();
//        if (brokers.isEmpty()) {
//            throw new IllegalArgumentException("Missing kafka broker");
//        }
//        List<String> brokerList = Arrays.asList(brokers.split("\\s*,\\s*"));
//        if (brokerList == null || brokerList.isEmpty()) {
//            throw new IllegalArgumentException("Missing kafka broker");
//        }
//        List<HostAndPort> hapList = new ArrayList<HostAndPort>();
//        for (String broker : brokerList) {
//            HostAndPort hap = HostAndPort.fromString(broker);
//            hapList.add(hap);
//        }
//        m_brokers = hapList;
        m_brokers = brokers;

        m_topics = properties.getProperty("topics");
        m_groupId = properties.getProperty("groupid");
        m_procedure = properties.getProperty("procedure");

        // NEEDSWORK??
        try {
            m_uri = new URI("fake://uri/for/kafka/consumer/group/" + m_groupId);
        }
        catch (URISyntaxException e) {
        }

        m_formatterBuilder = formatterBuilder;
    }

    public URI getURI() {
        return m_uri;
    }

    //public List<HostAndPort> getBrokers() {
    public String getBrokers() {
        return m_brokers;
    }


    public String getTopic()
    {
        return m_topics;
    }


    public String getGroupId()
    {
        return m_groupId;
    }


    public int getFetchSize()
    {
        return m_fetchSize;
    }


    public int getSocketTimeout()
    {
        return m_soTimeout;
    }


    public String getProcedure()
    {
        return m_procedure;
    }


    @Override
    public URI getResourceID()
    {
        return m_uri;
    }

    public KafkaImporterCommitPolicy getCommitPolicy() {
        return m_commitPolicy;
    }

    public long getTriggerValue() {
        return m_triggerValue;
    }

    public int getConsumers() {
        return m_consumers;
    }

    //Simple Host and Port abstraction....dont want to use our big stuff here osgi bundle import nastiness.
    public static class HostAndPort {

        private final String m_host;
        private final int m_port;
        private final String m_connectionString;

        public HostAndPort(String h, int p) {
            m_host = h;
            m_port = p;
            m_connectionString = m_host + ":" + m_port;
        }

        public static HostAndPort fromString(String hap) {
            String s[] = hap.split(":");
            int p = KAFKA_DEFAULT_BROKER_PORT;
            if (s.length > 1 && s[1] != null && s[1].length() > 0) {
                p = Integer.parseInt(s[1].trim());
            }
            return new HostAndPort(s[0].trim(), p);
        }

        public String getHost() {
            return m_host;
        }

        public int getPort() {
            return m_port;
        }

        @Override
        public String toString() {
            return m_connectionString;
        }

        @Override
        public int hashCode() {
            return m_connectionString.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof HostAndPort)) {
                return false;
            }
            if (this.getClass() != o.getClass()) {
                return false;
            }
            HostAndPort hap = (HostAndPort )o;
            if (hap == this) {
                return true;
            }
            return (hap.getHost().equals(getHost()) && hap.getPort() == getPort());
        }
    }

    public static class KafkaConfigurationException extends ImportBaseException {

        private static final long serialVersionUID = -3413349105074207334L;

        public KafkaConfigurationException() {
            super();
        }

        public KafkaConfigurationException(String format, Object... args) {
            super(format, args);
        }

        public KafkaConfigurationException(String format, Throwable cause,
                Object... args) {
            super(format, cause, args);
        }

        public KafkaConfigurationException(Throwable cause) {
            super(cause);
        }
    }

    @Override
    public FormatterBuilder getFormatterBuilder()
    {
        return m_formatterBuilder;
    }
}
