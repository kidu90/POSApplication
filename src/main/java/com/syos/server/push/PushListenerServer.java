package com.syos.server.push;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

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
                handlePushConnection(serverSocket.accept());
            }
        } catch (Exception ex) {
            throw new RuntimeException("Push listener failed", ex);
        }
    }

    private void handlePushConnection(Socket clientSocket) {
        Thread t = new Thread(() -> {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(
                    clientSocket.getOutputStream()
                );
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(
                    clientSocket.getInputStream()
                );

                String clientId = (String) ois.readObject();
                System.out.println("Push client registered: " + clientId);

                ConnectedClient client = new ConnectedClient(oos, pushService);
                pushService.register(client);

                try {
                    while (true) {
                        ois.readObject();
                    }
                } catch (EOFException | SocketException ex) {
                    System.out.println("Push client disconnected: " + clientId);
                    pushService.unregister(client);
                }
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println("Push registration error: " + ex.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ignored) {
                    // Ignore close failures during disconnect handling.
                }
            }
        });
        t.setDaemon(true);
        t.setName("push-server-handler");
        t.start();
    }
}
