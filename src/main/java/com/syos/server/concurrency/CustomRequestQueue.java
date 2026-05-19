package com.syos.server.concurrency;

import java.util.LinkedList;


public class CustomRequestQueue {
    private final LinkedList<Runnable> queue = new LinkedList<>();
    private boolean shutdown;

    public synchronized void enqueue(Runnable task) {
        if (shutdown) {
            throw new IllegalStateException("Queue is shut down");
        }
        queue.addLast(task);
        notifyAll();
    }

    public synchronized Runnable dequeue() throws InterruptedException {
        while (queue.isEmpty() && !shutdown) {
            wait();
        }
        if (shutdown && queue.isEmpty()) {
            return null;
        }
        return queue.removeFirst();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void shutdown() {
        shutdown = true;
        notifyAll();
    }
}
