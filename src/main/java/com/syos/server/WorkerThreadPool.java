package com.syos.server;

import com.syos.shared.Response;

public class WorkerThreadPool {
    private final RequestQueue requestQueue;
    private final ServerRequestProcessor processor;
    private final Thread[] workers;
    private volatile boolean running = true;

    public WorkerThreadPool(int workerCount, RequestQueue requestQueue, ServerRequestProcessor processor) {
        this.requestQueue = requestQueue;
        this.processor = processor;
        this.workers = new Thread[workerCount];
    }

    public void start() {
        for (int index = 0; index < workers.length; index++) {
            workers[index] = new Thread(() -> {
                while (running) {
                    PendingRequest pendingRequest = null;
                    try {
                        pendingRequest = requestQueue.take();
                        Object data = processor.handle(pendingRequest.getRequest().getAction(), pendingRequest.getRequest().getParams());
                        pendingRequest.complete(new Response(true, "OK", data));
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception ex) {
                        if (pendingRequest != null) {
                            try {
                                pendingRequest.complete(new Response(false, ex.getMessage(), null));
                            } catch (Exception ignored) {
                                // Ignore secondary failure.
                            }
                        }
                    }
                }
            }, "syos-worker-" + (index + 1));
            workers[index].setDaemon(true);
            workers[index].start();
        }
    }

    public void stop() {
        running = false;
        synchronized (requestQueue) {
            requestQueue.notifyAll();
        }
        for (Thread worker : workers) {
            if (worker != null) {
                worker.interrupt();
            }
        }
    }
}