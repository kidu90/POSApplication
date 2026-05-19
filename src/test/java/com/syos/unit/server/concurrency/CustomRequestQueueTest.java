package com.syos.unit.server.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.syos.server.concurrency.CustomRequestQueue;

/**
 * Verifies the blocking monitor behavior of the custom request queue because it drives server concurrency.
 */
class CustomRequestQueueTest {
    private CustomRequestQueue queue;

    @BeforeEach
    void setUp() {
        queue = new CustomRequestQueue();
    }

    @Test
    void shouldReturnNotEmptyAfterEnqueue() {
        queue.enqueue(() -> { });

        assertFalse(queue.isEmpty());
    }

    @Test
    void shouldReturnTasksInFifoOrder() throws Exception {
        List<String> order = new ArrayList<>();
        queue.enqueue(() -> order.add("first"));
        queue.enqueue(() -> order.add("second"));

        queue.dequeue().run();
        queue.dequeue().run();

        assertEquals(List.of("first", "second"), order);
    }

    @Test
    void shouldBlockAndUnblockWhenTaskIsEnqueued() throws Exception {
        AtomicReference<Runnable> result = new AtomicReference<>();
        Thread dequeuer = new Thread(() -> {
            try {
                result.set(queue.dequeue());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });

        dequeuer.start();
        Thread.sleep(50);
        assertNull(result.get());

        Runnable task = () -> { };
        queue.enqueue(task);
        dequeuer.join(1000);

        assertAll(
            () -> assertFalse(dequeuer.isAlive()),
            () -> assertNotNull(result.get()),
            () -> assertEquals(task, result.get())
        );
    }

    @Test
    void shouldPreserveAllTasksFromMultipleProducers() throws Exception {
        int producerCount = 5;
        int tasksPerProducer = 100;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(producerCount);

        for (int producerIndex = 0; producerIndex < producerCount; producerIndex++) {
            int base = producerIndex * tasksPerProducer;
            new Thread(() -> {
                try {
                    startGate.await();
                    for (int i = 0; i < tasksPerProducer; i++) {
                        int id = base + i;
                        queue.enqueue(new IndexedTask(id));
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    finished.countDown();
                }
            }).start();
        }

        startGate.countDown();
        finished.await();

        Set<Integer> ids = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < producerCount * tasksPerProducer; i++) {
            Runnable task = queue.dequeue();
            assertNotNull(task);
            int id = ((IndexedTask) task).id;
            assertTrue(ids.add(id), "Duplicate task id: " + id);
        }

        assertEquals(500, ids.size());
    }

    @Test
    void shouldWakeBlockedDequeuerAfterShutdown() throws Exception {
        AtomicReference<Runnable> result = new AtomicReference<>();
        Thread dequeuer = new Thread(() -> {
            try {
                result.set(queue.dequeue());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });

        dequeuer.start();
        Thread.sleep(50);
        queue.shutdown();
        dequeuer.join(1000);

        assertAll(
            () -> assertFalse(dequeuer.isAlive()),
            () -> assertNull(result.get())
        );
    }

    @Test
    void shouldThrowWhenEnqueuingAfterShutdown() {
        queue.shutdown();

        assertThrows(IllegalStateException.class, () -> queue.enqueue(() -> { }));
    }

    private static class IndexedTask implements Runnable {
        private final int id;

        private IndexedTask(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            // No-op: the queue test only checks identity and order.
        }
    }
}
