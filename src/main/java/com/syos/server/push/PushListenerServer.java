package com.syos.server.push;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PushListenerServer implements Runnable {
    private final int port;
    private final PushNotificationService pushService;

    public PushListenerServer(int port, PushNotificationService pushService) {
        this.port = port;
        this.pushService = pushService;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                Socket clientSocket = socket;
                Thread connectionThread = new Thread(() -> {
                    try {
                        ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                        String clientId = (String) inputStream.readObject();
                        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                        outputStream.flush();
                        ConnectedClient client = new ConnectedClient(outputStream, pushService);
                        pushService.register(client);

                        try {
                            Thread.currentThread().join();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } catch (Exception ex) {
                        System.err.println("Push listener registration failed: " + ex.getMessage());
                    }
                }, "syos-push-acceptor");
                connectionThread.setDaemon(true);
                connectionThread.start();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Push listener failed", ex);
        }
    }
}
