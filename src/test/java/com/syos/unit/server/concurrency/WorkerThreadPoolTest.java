package com.syos.unit.server.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.syos.server.concurrency.CustomRequestQueue;
import com.syos.server.concurrency.WorkerThreadPool;

/**
 * Exercises the worker pool because the server relies on it for draining queued request tasks.
 */
class WorkerThreadPoolTest {
    private CustomRequestQueue queue;

    @BeforeEach
    void setUp() {
        queue = new CustomRequestQueue();
    }

    @Test
    void shouldExecuteSubmittedTask() throws Exception {
        WorkerThreadPool pool = new WorkerThreadPool(2, queue);
        CountDownLatch latch = new CountDownLatch(1);
        pool.start();

        queue.enqueue(latch::countDown);

        assertTrue(latch.await(2, java.util.concurrent.TimeUnit.SECONDS));
        pool.shutdown();
    }

    @Test
    void shouldDrainTwentyTasksWithinFiveSeconds() throws Exception {
        WorkerThreadPool pool = new WorkerThreadPool(3, queue);
        CountDownLatch latch = new CountDownLatch(20);
        pool.start();

        for (int i = 0; i < 20; i++) {
            queue.enqueue(latch::countDown);
        }

        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
        pool.shutdown();
    }

    @Test
    void shouldRunTasksConcurrently() throws Exception {
        WorkerThreadPool pool = new WorkerThreadPool(5, queue);
        CountDownLatch started = new CountDownLatch(5);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(5);
        pool.start();

        long before = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            queue.enqueue(() -> {
                started.countDown();
                try {
                    release.await();
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }

        assertTrue(started.await(2, java.util.concurrent.TimeUnit.SECONDS));
        release.countDown();
        assertTrue(finished.await(2, java.util.concurrent.TimeUnit.SECONDS));
        long elapsed = System.currentTimeMillis() - before;

        pool.shutdown();
        assertTrue(elapsed < 600, "Expected concurrent execution, elapsed=" + elapsed);
    }

    @Test
    void shouldContinueProcessingAfterTaskThrows() throws Exception {
        WorkerThreadPool pool = new WorkerThreadPool(1, queue);
        AtomicInteger counter = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);
        pool.start();

        queue.enqueue(() -> { throw new RuntimeException("boom"); });
        queue.enqueue(() -> {
            counter.incrementAndGet();
            latch.countDown();
        });

        assertTrue(latch.await(2, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(1, counter.get());
        pool.shutdown();
    }

    @Test
    void shouldRejectNewTasksAfterShutdown() {
        WorkerThreadPool pool = new WorkerThreadPool(1, queue);
        pool.start();
        pool.shutdown();

        assertThrows(IllegalStateException.class, () -> queue.enqueue(() -> { }));
    }

    @Test
    void shouldExecuteSequentiallyWithSingleWorker() throws Exception {
        WorkerThreadPool pool = new WorkerThreadPool(1, queue);
        StringBuilder builder = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(3);
        pool.start();

        queue.enqueue(() -> { builder.append('A'); latch.countDown(); });
        queue.enqueue(() -> { builder.append('B'); latch.countDown(); });
        queue.enqueue(() -> { builder.append('C'); latch.countDown(); });

        assertTrue(latch.await(2, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals("ABC", builder.toString());
        pool.shutdown();
    }
}
