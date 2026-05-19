package com.syos.server.concurrency;

/**
 * A simple worker thread pool that consumes Runnable tasks from a CustomRequestQueue.
 * Workers block on dequeue() when the queue is empty and wake when new tasks arrive.
 */
public class WorkerThreadPool {
    private final Thread[] workers;
    private final CustomRequestQueue queue;
    private volatile boolean running = true;

    public WorkerThreadPool(int poolSize, CustomRequestQueue queue) {
        this.queue = queue;
        this.workers = new Thread[poolSize];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(this::workerLoop, "syos-concurrency-worker-" + (i + 1));
            workers[i].setDaemon(true);
        }
    }

    private void workerLoop() {
        while (running) {
            try {
                Runnable task = queue.dequeue();
                if (task != null) {
                    task.run();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                // Swallow to keep worker alive for other tasks.
                System.err.println("Worker encountered error: " + ex.getMessage());
            }
        }
    }

    public void start() {
        for (Thread worker : workers) {
            worker.start();
        }
    }

    public void shutdown() {
        running = false;
        synchronized (queue) {
            queue.notifyAll();
        }
        for (Thread worker : workers) {
            if (worker != null) worker.interrupt();
        }
    }
}
