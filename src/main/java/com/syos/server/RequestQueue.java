package com.syos.server;

import java.util.LinkedList;

public class RequestQueue {
    private final LinkedList<PendingRequest> queue = new LinkedList<>();

    public synchronized void enqueue(PendingRequest request) {
        queue.addLast(request);
        notifyAll();
    }

    public synchronized PendingRequest take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }

        return queue.removeFirst();
    }
}