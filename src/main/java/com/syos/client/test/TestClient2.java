package com.syos.client.test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.syos.domain.entity.Bill;
import com.syos.shared.Response;

public class TestClient2 {
    public static void main(String[] args) {
        run();
    }

    public static void run() {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(3);
        Set<String> billNumbers = java.util.Collections.synchronizedSet(new HashSet<>());
        AtomicInteger duplicates = new AtomicInteger(0);

        for (int threadIndex = 1; threadIndex <= 3; threadIndex++) {
            int clientThreadIndex = threadIndex;
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int requestIndex = 1; requestIndex <= 10; requestIndex++) {
                        String sentAt = TestClientSupport.now();
                        System.out.println("[TestClient2] thread=" + clientThreadIndex + " sent checkout=" + requestIndex + " at=" + sentAt);
                        try {
                            Response response = TestClientSupport.send("CHECKOUT_ONLINE", TestClientSupport.checkoutCart("P005", 1));
                            String receivedAt = TestClientSupport.now();
                            Object data = response.getData();
                            String billNumber = extractBillNumber(data);
                            if (billNumber != null) {
                                if (!billNumbers.add(billNumber)) {
                                    duplicates.incrementAndGet();
                                }
                            }
                            System.out.println("[TestClient2] thread=" + clientThreadIndex + " response=" + requestIndex + " at=" + receivedAt + " success=" + response.isSuccess() + " bill=" + billNumber + " message=" + response.getMessage());
                        } catch (Exception ex) {
                            String failedAt = TestClientSupport.now();
                            System.out.println("[TestClient2] thread=" + clientThreadIndex + " failed checkout=" + requestIndex + " at=" + failedAt + " error=" + ex.getMessage());
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }, "test-client-2-" + clientThreadIndex);
            thread.start();
        }

        startLatch.countDown();

        try {
            doneLatch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        System.out.println("[TestClient2] duplicate bill numbers=" + duplicates.get());
    }

    private static String extractBillNumber(Object data) {
        if (data instanceof Bill bill) {
            return bill.getBillNumber().getValue();
        }
        if (data == null) {
            return null;
        }
        return String.valueOf(data);
    }
}
