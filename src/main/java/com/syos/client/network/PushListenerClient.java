package com.syos.client.network;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

import javax.swing.SwingUtilities;

import com.syos.shared.PushEvent;

public class PushListenerClient {
    private final String host;
    private final int port;
    private final Runnable refreshCallback;
    private final String clientId = UUID.randomUUID().toString();
    private volatile boolean running = true;

    public PushListenerClient(String host, int port, Runnable refreshCallback) {
        this.host = host;
        this.port = port;
        this.refreshCallback = refreshCallback;
    }

    public void start() {
        Thread listenerThread = new Thread(() -> {
            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

                objectOutputStream.writeObject(clientId);
                objectOutputStream.flush();

                try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
                    while (running) {
                        try {
                            Object obj = objectInputStream.readObject();
                            if (obj instanceof PushEvent event) {
                                handleEvent(event);
                            }
                        } catch (EOFException | SocketException e) {
                            break;
                        } catch (Exception e) {
                            System.err.println("Unexpected push listener error: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Push listener stopped: " + ex.getMessage());
            }
        }, "syos-push-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void handleEvent(PushEvent event) {
        SwingUtilities.invokeLater(() -> {
            if (event.getEventType().equals("STOCK_CHANGED")) {
                refreshCallback.run();
            }
        });
    }
}
