package org.voltdb.importclient.kafka;

import org.voltcore.logging.Level;
import org.voltdb.importer.CommitTracker;
import org.voltdb.importer.ImporterLogger;

public class DurableTracker implements CommitTracker {
    long c = 0;
    long s = -1L;
    long offer = -1L;
    final long [] lag;
    ImporterLogger m_logger;
    private final int m_gapFullWait = Integer.getInteger("KAFKA_IMPORT_GAP_WAIT", 2_000);
    private String m_topicAndPartition;

    public DurableTracker(int leeway, ImporterLogger logger, String topicPartitionName) {
        m_logger = logger;
        m_topicAndPartition = topicPartitionName;
        if (leeway <= 0) {
            throw new IllegalArgumentException("leeways is zero or negative");
        }
        lag = new long[leeway];
    }

    @Override
    public synchronized void submit(long offset) {
        if (s == -1L && offset >= 0) {
            lag[idx(offset)] = c = s = offset;
        }
        if ((offset - c) >= lag.length) {
            offer = offset;
            try {
                wait(m_gapFullWait);
            } catch (InterruptedException e) {
                m_logger.rateLimitedLog(Level.WARN, e, "Gap tracker wait was interrupted for" + m_topicAndPartition);
            }
        }
        if (offset > s) {
            s = offset;
        }
    }

    private final int idx(long offset) {
        return (int)(offset % lag.length);
    }

    @Override
    public synchronized void resetTo(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset is negative");
        }
        lag[idx(offset)] = s = c = offset;
        offer = -1L;
    }

    @Override
    public synchronized long commit(long offset) {
        if (offset <= s && offset > c) {
            int ggap = (int)Math.min(lag.length, offset-c);
            if (ggap == lag.length) {
                m_logger.rateLimitedLog(Level.WARN,
                          null, "Gap tracker moving topic commit point from %d to %d for "
                          + m_topicAndPartition, c, (offset - lag.length + 1)
                        );
                c = offset - lag.length + 1;
                lag[idx(c)] = c;
            }
            lag[idx(offset)] = offset;
            while (ggap > 0 && lag[idx(c)]+1 == lag[idx(c+1)]) {
                ++c;
            }
            if (offer >=0 && (offer-c) < lag.length) {
                offer = -1L;
                notify();
            }
        }
        return c;
    }
}
