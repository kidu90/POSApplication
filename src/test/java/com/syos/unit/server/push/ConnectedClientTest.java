package com.syos.unit.server.push;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.syos.server.push.ConnectedClient;
import com.syos.server.push.PushNotificationService;
import com.syos.shared.PushEvent;

/**
 * Verifies the push client wrapper because it must flush, reset, and unregister safely under contention.
 */
class ConnectedClientTest {

    @Test
    void shouldWriteAndFlushPushEvent() throws Exception {
        PushNotificationService service = new PushNotificationService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ConnectedClient client = new ConnectedClient(new ObjectOutputStream(baos), service);

        PushEvent event = new PushEvent("STOCK_CHANGED", "payload");
        client.sendPushEvent(event);

        PushEvent copy = readSingleEvent(baos.toByteArray());
        assertAll(
            () -> assertEquals("STOCK_CHANGED", copy.getEventType()),
            () -> assertEquals("payload", copy.getPayload())
        );
    }

    @Test
    void shouldResetStreamAfterEachWrite() throws Exception {
        PushNotificationService service = new PushNotificationService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ConnectedClient client = new ConnectedClient(new ObjectOutputStream(baos), service);
        PushEvent event = new PushEvent("STOCK_CHANGED", "same");

        client.sendPushEvent(event);
        client.sendPushEvent(event);

        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            PushEvent first = (PushEvent) inputStream.readObject();
            PushEvent second = (PushEvent) inputStream.readObject();

            assertAll(
                () -> assertEquals(first.getEventType(), second.getEventType()),
                () -> assertEquals(first.getPayload(), second.getPayload()),
                () -> assertNotSame(first, second)
            );
        }
    }

    @Test
    void shouldRemainSynchronizedUnderConcurrentWrites() throws Exception {
        PushNotificationService service = new PushNotificationService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ConnectedClient client = new ConnectedClient(new ObjectOutputStream(baos), service);
        CountDownLatch done = new CountDownLatch(2);

        Runnable writer = () -> {
            try {
                for (int i = 0; i < 50; i++) {
                    client.sendPushEvent(new PushEvent("STOCK_CHANGED", i));
                }
            } finally {
                done.countDown();
            }
        };

        new Thread(writer).start();
        new Thread(writer).start();

        assertTrue(done.await(3, java.util.concurrent.TimeUnit.SECONDS));

        int count = 0;
        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            while (true) {
                try {
                    Object obj = inputStream.readObject();
                    assertTrue(obj instanceof PushEvent);
                    count++;
                } catch (java.io.EOFException ex) {
                    break;
                }
            }
        }

        assertEquals(100, count);
    }

    @Test
    void shouldUnregisterWhenStreamWriteFails() throws Exception {
        TrackingPushNotificationService service = new TrackingPushNotificationService();
        FlakyOutputStream flakyOutputStream = new FlakyOutputStream();
        ConnectedClient client = new ConnectedClient(new ObjectOutputStream(flakyOutputStream), service);
        flakyOutputStream.failAfterConstruction.set(true);

        client.sendPushEvent(new PushEvent("STOCK_CHANGED", null));

        assertTrue(service.unregistered.get());
    }

    private static PushEvent readSingleEvent(byte[] bytes) throws Exception {
        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (PushEvent) inputStream.readObject();
        }
    }

    private static class TrackingPushNotificationService extends PushNotificationService {
        private final AtomicBoolean unregistered = new AtomicBoolean(false);

        @Override
        public synchronized void unregister(ConnectedClient client) {
            unregistered.set(true);
            super.unregister(client);
        }
    }

    private static class FlakyOutputStream extends OutputStream {
        private final AtomicBoolean failAfterConstruction = new AtomicBoolean(false);

        @Override
        public void write(int b) throws IOException {
            if (failAfterConstruction.get()) {
                throw new IOException("broken stream");
            }
        }
    }
}
