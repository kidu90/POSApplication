package com.syos.client.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.syos.shared.Response;

public class TestClient1 {
    public static void main(String[] args) {
        run();
    }

    public static void run() {
        List<Thread> threads = new ArrayList<>();
        CountDownLatch done = new CountDownLatch(20);

        for (int i = 0; i < 5; i++) {
            threads.add(startRequestThread("CHECKOUT_INSTORE", i + 1, done));
            threads.add(startRequestThread("GET_STOCK", i + 1, done));
            threads.add(startRequestThread("ADD_STOCK", i + 1, done));
            threads.add(startRequestThread("GET_DAILY_REPORT", i + 1, done));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            done.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static Thread startRequestThread(String action, int index, CountDownLatch done) {
        return new Thread(() -> {
            String sentAt = TestClientSupport.now();
            System.out.println("[TestClient1] sent action=" + action + " index=" + index + " at=" + sentAt);
            try {
                Response response = switch (action) {
                    case "CHECKOUT_INSTORE" -> TestClientSupport.send(action, TestClientSupport.checkoutCart("P001", 1));
                    case "GET_STOCK" -> TestClientSupport.send(action, Map.of());
                    case "ADD_STOCK" -> TestClientSupport.send(action, TestClientSupport.addStockParams("TB1-" + index, "P001", "STORE", 1));
                    case "GET_DAILY_REPORT" -> TestClientSupport.send(action, Map.of());
                    default -> throw new IllegalArgumentException("Unsupported action: " + action);
                };
                String receivedAt = TestClientSupport.now();
                System.out.println("[TestClient1] response action=" + action + " index=" + index + " at=" + receivedAt + " success=" + response.isSuccess() + " message=" + response.getMessage());
            } catch (Exception ex) {
                String failedAt = TestClientSupport.now();
                System.out.println("[TestClient1] failed action=" + action + " index=" + index + " at=" + failedAt + " error=" + ex.getMessage());
            } finally {
                done.countDown();
            }
        }, "test-client-1-" + action + "-" + index);
    }
}
