package com.syos.client.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.syos.domain.entity.StockBatch;
import com.syos.presentation.gui.StockManagementFrame;
import com.syos.shared.PushEvent;
import com.syos.shared.Request;
import com.syos.shared.Response;

public class PushListenerClient {
    private final String host;
    private final int pushPort;
    private final String clientId;
    private final ServerConnection serverConnection;
    private final StockManagementFrame stockManagementFrame;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private volatile boolean running = true;

    public PushListenerClient(String host,
                              int pushPort,
                              String clientId,
                              ServerConnection serverConnection,
                              StockManagementFrame stockManagementFrame) {
        this.host = host;
        this.pushPort = pushPort;
        this.clientId = clientId;
        this.serverConnection = serverConnection;
        this.stockManagementFrame = stockManagementFrame;
    }

    public void start() {
        try {
            socket = new Socket(host, pushPort);
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(clientId);
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Push channel connection failed: " + e.getMessage());
            return;
        }

        Thread listenerThread = new Thread(() -> {
            while (running) {
                try {
                    Object obj = ois.readObject();
                    if (obj instanceof PushEvent event) {
                        handleEvent(event);
                    }
                } catch (EOFException | SocketException e) {
                    System.out.println("Push channel closed by server.");
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("Unknown push event type: " + e.getMessage());
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Push channel IO error: " + e.getMessage());
                    }
                    break;
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.setName("push-listener-" + clientId);
        listenerThread.start();
    }

    private void handleEvent(PushEvent event) {
        switch (event.getEventType()) {
            case "STOCK_CHANGED" -> SwingUtilities.invokeLater(() -> {
                new SwingWorker<List<StockBatch>, Void>() {
                    @Override
                    protected List<StockBatch> doInBackground() {
                        Response r = serverConnection.sendRequest(
                            new Request("GET_STOCK", Map.of())
                        );
                        @SuppressWarnings("unchecked")
                        List<StockBatch> batches = (List<StockBatch>) r.getData();
                        return batches;
                    }

                    @Override
                    protected void done() {
                        try {
                            List<StockBatch> batches = get();
                            stockManagementFrame.populateStockTable(batches);
                        } catch (InterruptedException | ExecutionException e) {
                            if (e instanceof InterruptedException) {
                                Thread.currentThread().interrupt();
                            }
                            System.err.println("Push stock refresh failed: " + e.getMessage());
                        }
                    }
                }.execute();
            });
            case "PRODUCT_CHANGED" -> SwingUtilities.invokeLater(() -> {
                stockManagementFrame.refreshAll();
            });
        }
    }

    public void stop() {
        running = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
            // Ignore close failures during shutdown.
        }
    }
}
