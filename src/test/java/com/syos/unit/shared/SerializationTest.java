package com.syos.unit.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.syos.shared.PushEvent;
import com.syos.shared.Request;
import com.syos.shared.Response;

/**
 * Verifies that the shared wire protocol objects remain serializable and stable across round-trips.
 */
class SerializationTest {

    @Test
    void shouldRoundTripRequest_whenSerializedAndDeserialized() throws Exception {
        Request original = new Request("GET_STOCK", Map.of("productId", "P001", "quantity", 2));
        Request copy = roundTrip(original);

        assertAll(
            () -> assertEquals(original.getAction(), copy.getAction()),
            () -> assertEquals(original.getParams(), copy.getParams())
        );
    }

    @Test
    void shouldRoundTripResponse_whenSerializedAndDeserialized() throws Exception {
        Response original = new Response(true, "OK", List.of("A", "B"));
        Response copy = roundTrip(original);

        assertAll(
            () -> assertTrue(copy.isSuccess()),
            () -> assertEquals("OK", copy.getMessage()),
            () -> assertEquals(List.of("A", "B"), copy.getData())
        );
    }

    @Test
    void shouldPreserveNullData_whenResponseIsSerialized() throws Exception {
        Response original = new Response(false, "Missing", null);
        Response copy = roundTrip(original);

        assertAll(
            () -> assertTrue(!copy.isSuccess()),
            () -> assertEquals("Missing", copy.getMessage()),
            () -> assertEquals(null, copy.getData())
        );
    }

    @Test
    void shouldRoundTripPushEvent_whenSerializedAndDeserialized() throws Exception {
        PushEvent original = new PushEvent("STOCK_CHANGED", Map.of("batch", "B001"));
        PushEvent copy = roundTrip(original);

        assertAll(
            () -> assertEquals("STOCK_CHANGED", copy.getEventType()),
            () -> assertEquals(Map.of("batch", "B001"), copy.getPayload())
        );
    }

    @Test
    void shouldRoundTripEmptyParamsRequest_whenSerializedAndDeserialized() throws Exception {
        Request original = new Request("GET_PRODUCTS", Map.of());
        Request copy = roundTrip(original);

        assertAll(
            () -> assertEquals("GET_PRODUCTS", copy.getAction()),
            () -> assertTrue(copy.getParams().isEmpty())
        );
    }

    @Test
    void shouldThrowWhenDeserializingCorruptBytes() {
        byte[] corrupt = new byte[] {0x01, 0x02, 0x03, 0x04};

        try {
            readCorruptObject(corrupt);
        } catch (StreamCorruptedException expected) {
            return;
        } catch (Exception unexpected) {
            throw new AssertionError(unexpected);
        }

        throw new AssertionError("Expected StreamCorruptedException");
    }

    private static void readCorruptObject(byte[] corrupt) throws Exception {
        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(corrupt))) {
            inputStream.readObject();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T object) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(object);
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray()))) {
            return (T) inputStream.readObject();
        }
    }
}
