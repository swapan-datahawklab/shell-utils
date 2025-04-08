package com.example.shelldemo.monitoring.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread-safe buffer for storing metric events.
 * This buffer is optimized for concurrent access and provides efficient draining of events.
 */
public class MetricBuffer {
    private static final int DEFAULT_MAX_SIZE = 10000;
    
    private final ConcurrentLinkedQueue<MetricEvent> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger size = new AtomicInteger(0);
    private final int maxSize;

    /**
     * Creates a new MetricBuffer with the default maximum size.
     */
    public MetricBuffer() {
        this(DEFAULT_MAX_SIZE);
    }

    /**
     * Creates a new MetricBuffer with the specified maximum size.
     *
     * @param maxSize the maximum number of events to buffer
     * @throws IllegalArgumentException if maxSize is less than 1
     */
    public MetricBuffer(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize must be at least 1");
        }
        this.maxSize = maxSize;
    }

    /**
     * Adds a metric event to the buffer.
     * If the buffer is full, the oldest event is removed to make space.
     *
     * @param event the metric event to add
     * @throws NullPointerException if event is null
     */
    public void add(MetricEvent event) {
        if (event == null) {
            throw new NullPointerException("event cannot be null");
        }
        
        queue.offer(event);
        if (size.incrementAndGet() > maxSize) {
            queue.poll(); // Remove oldest event
            size.decrementAndGet();
        }
    }

    /**
     * Drains all events from the buffer and returns them in a list.
     * The buffer is cleared after draining.
     *
     * @return a list containing all drained events
     */
    public List<MetricEvent> drain() {
        List<MetricEvent> events = new ArrayList<>();
        MetricEvent event;
        while ((event = queue.poll()) != null) {
            events.add(event);
            size.decrementAndGet();
        }
        return events;
    }

    /**
     * Returns the current number of events in the buffer.
     *
     * @return the current size of the buffer
     */
    public int size() {
        return size.get();
    }

    /**
     * Returns the maximum number of events this buffer can hold.
     *
     * @return the maximum size of the buffer
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Checks if the buffer is empty.
     *
     * @return true if the buffer contains no events
     */
    public boolean isEmpty() {
        return size.get() == 0;
    }
} 