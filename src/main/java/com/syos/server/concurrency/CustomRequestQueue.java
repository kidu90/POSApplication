package com.syos.server.concurrency;

import java.util.LinkedList;

/**
 * A simple blocking queue implementation for Runnable tasks using the monitor pattern.
 * Internally uses a LinkedList as the backing store and coordinates producers and
 * consumers with wait()/notifyAll(). This class intentionally avoids java.util.concurrent
 * types to keep the implementation minimal and educational.
 */
public class CustomRequestQueue {
    private final LinkedList<Runnable> queue = new LinkedList<>();

    public synchronized void enqueue(Runnable task) {
        queue.addLast(task);
        notifyAll();
    }

    public synchronized Runnable dequeue() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.removeFirst();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}
