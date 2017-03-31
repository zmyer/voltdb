package org.voltdb.client.VoltBulkLoader;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;


/**
 * This class is responsible for writing elements that make up a lsp event into
 * the ringbuffer. After this translator populated the event,
 * the disruptor will update the sequence number so that
 * the event can be consumed by another thread.
 */
public class RowProducerWithTranslator {
    private final RingBuffer<RowEvent> ringBuffer;

    public RowProducerWithTranslator(RingBuffer<RowEvent> ringBuffer)
    {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<RowEvent, VoltBulkLoaderRow> TRANSLATOR =
            (event, sequence, row) -> event.setValues(row);

    public void onData(VoltBulkLoaderRow row)
    {
        ringBuffer.publishEvent(TRANSLATOR, row);
    }
}
