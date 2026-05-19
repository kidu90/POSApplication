package com.syos.client.test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.syos.client.network.ServerConnection;
import com.syos.shared.Request;
import com.syos.shared.Response;

final class TestClientSupport {
    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    private TestClientSupport() {
    }

    static Response send(String action, Map<String, Object> params) {
        ServerConnection connection = new ServerConnection(HOST, PORT);
        return connection.sendRequest(new Request(action, params));
    }

    static Map<String, Object> checkoutCart(String productId, int quantity) {
        Map<String, Object> params = new LinkedHashMap<>();
        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put(productId, quantity);
        params.put("cart", cart);
        params.put("customerName", "Test User");
        params.put("customerAddress", "Test Address");
        return params;
    }

    static Map<String, Object> addStockParams(String batchNumber, String productId, String channel, int quantity) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("batchNumber", batchNumber);
        params.put("productId", productId);
        params.put("channel", channel);
        params.put("quantity", quantity);
        params.put("expiryDate", LocalDate.now().plusDays(30).toString());
        params.put("receivedDate", LocalDate.now().toString());
        return params;
    }

    static String now() {
        return LocalTime.now().toString();
    }
}
