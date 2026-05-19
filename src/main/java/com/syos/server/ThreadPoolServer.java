package com.syos.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadPoolServer {
    private final int port;
    private final RequestQueue requestQueue;
    private final WorkerThreadPool workerThreadPool;
    private volatile boolean running = true;
    private ServerSocket serverSocket;

    public ThreadPoolServer(int port, RequestQueue requestQueue, WorkerThreadPool workerThreadPool) {
        this.port = port;
        this.requestQueue = requestQueue;
        this.workerThreadPool = workerThreadPool;
    }

    public void start() {
        workerThreadPool.start();

        try {
            serverSocket = new ServerSocket(port);
            while (running) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, requestQueue), "syos-client-handler").start();
            }
        } catch (Exception ex) {
            if (running) {
                throw new RuntimeException("Server failed", ex);
            }
        } finally {
            workerThreadPool.stop();
        }
    }

    public void shutdown() {
        running = false;
        workerThreadPool.stop();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
                // Ignore close failures during shutdown.
            }
        }
    }
}