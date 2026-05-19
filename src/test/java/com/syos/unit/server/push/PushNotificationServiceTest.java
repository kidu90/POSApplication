package com.syos.unit.server.push;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.syos.server.push.ConnectedClient;
import com.syos.server.push.PushNotificationService;
import com.syos.shared.PushEvent;

/**
 * Verifies push broadcasts because server-side push delivery must be safe and deterministic.
 */
class PushNotificationServiceTest {

    @Test
    void shouldBroadcastStockChangeToRegisteredClient() throws Exception {
        PushNotificationService service = new PushNotificationService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ConnectedClient client = createClient(baos, service);

        service.register(client);
        service.broadcastStockChange();

        PushEvent event = readSingleEvent(baos.toByteArray());
        assertEquals("STOCK_CHANGED", event.getEventType());
    }

    @Test
    void shouldBroadcastToAllRegisteredClients() throws Exception {
        PushNotificationService service = new PushNotificationService();
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        ConnectedClient client1 = createClient(baos1, service);
        ConnectedClient client2 = createClient(baos2, service);

        service.register(client1);
        service.register(client2);
        service.broadcastStockChange();

        assertAll(
            () -> assertEquals("STOCK_CHANGED", readSingleEvent(baos1.toByteArray()).getEventType()),
            () -> assertEquals("STOCK_CHANGED", readSingleEvent(baos2.toByteArray()).getEventType())
        );
    }

    @Test
    void shouldRemoveClientWhenUnregistered() throws Exception {
        PushNotificationService service = new PushNotificationService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ConnectedClient client = createClient(baos, service);
        int initialSize = baos.size();

        service.register(client);
        service.unregister(client);
        service.broadcastStockChange();

        assertEquals(initialSize, baos.toByteArray().length);
    }

    @Test
    void shouldNotThrowWhenBroadcastingWithNoClients() {
        PushNotificationService service = new PushNotificationService();

        assertDoesNotThrow(service::broadcastStockChange);
    }

    @Test
    void shouldBroadcastProductChange() throws Exception {
        PushNotificationService service = new PushNotificationService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ConnectedClient client = createClient(baos, service);

        service.register(client);
        service.broadcastProductChange();

        assertEquals("PRODUCT_CHANGED", readSingleEvent(baos.toByteArray()).getEventType());
    }

    @Test
    void shouldUnregisterBrokenClientOnIOException() throws Exception {
        TrackingService trackingService = new TrackingService();
        FlakyOutputStream flaky = new FlakyOutputStream();
        ConnectedClient client = createClient(flaky, trackingService);
        trackingService.register(client);
        flaky.failOnWrite.set(true);

        trackingService.broadcastStockChange();

        assertTrue(trackingService.unregistered.get());
    }

    @Test
    void shouldRemainThreadSafeDuringConcurrentAccess() throws Exception {
        PushNotificationService service = new PushNotificationService();
        List<ConnectedClient> clients = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            clients.add(createClient(baos, service));
        }

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(20);
        List<Exception> errors = java.util.Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    for (int j = 0; j < 20; j++) {
                        service.broadcastStockChange();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    errors.add(ex);
                } finally {
                    done.countDown();
                }
            });
            thread.setUncaughtExceptionHandler((ignoredThread, throwable) -> errors.add(new RuntimeException(throwable)));
            thread.start();
        }

        for (ConnectedClient client : clients) {
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    service.register(client);
                    service.unregister(client);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    errors.add(ex);
                } finally {
                    done.countDown();
                }
            });
            thread.setUncaughtExceptionHandler((ignoredThread, throwable) -> errors.add(new RuntimeException(throwable)));
            thread.start();
        }

        start.countDown();
        assertTrue(done.await(3, java.util.concurrent.TimeUnit.SECONDS));
        assertTrue(errors.isEmpty(), "Thread errors: " + errors);
    }

    private static ConnectedClient createClient(OutputStream outputStream, PushNotificationService service) throws Exception {
        return new ConnectedClient(new ObjectOutputStream(outputStream), service);
    }

    private static PushEvent readSingleEvent(byte[] bytes) throws Exception {
        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (PushEvent) inputStream.readObject();
        }
    }

    private static class TrackingService extends PushNotificationService {
        private final AtomicBoolean unregistered = new AtomicBoolean(false);

        @Override
        public synchronized void unregister(ConnectedClient client) {
            unregistered.set(true);
            super.unregister(client);
        }
    }

    private static class FlakyOutputStream extends OutputStream {
        private final AtomicBoolean failOnWrite = new AtomicBoolean(false);

        @Override
        public void write(int b) throws IOException {
            if (failOnWrite.get()) {
                throw new IOException("broken stream");
            }
        }
    }

}

