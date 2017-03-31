package org.voltdb.client.VoltBulkLoader;

import com.lmax.disruptor.EventFactory;

public class RowEvent {
    public static final Factory FACTORY = new Factory();

    /**
     * Creates the events that will be put in the RingBuffer.
     */
    private static class Factory implements EventFactory<RowEvent> {
        @Override
        public RowEvent newInstance() {
            final RowEvent result = new RowEvent();
            return result;
        }
    }

    VoltBulkLoaderRow m_row;

    public void setValues(final VoltBulkLoaderRow row) {
        this.m_row = row;
    }

    public void clear() {
    }
}
