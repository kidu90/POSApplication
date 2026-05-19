package com.syos.client.test;

public class RunBothTestClients {
    public static void main(String[] args) {
        Thread client1 = new Thread(TestClient1::run, "run-both-test-client-1");
        Thread client2 = new Thread(TestClient2::run, "run-both-test-client-2");

        client1.start();
        client2.start();

        try {
            client1.join();
            client2.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
