package com.syos.server;

import java.net.ServerSocket;
import java.net.Socket;

public class ThreadPoolServer {
    private final int port;
    private final RequestQueue requestQueue;
    private final WorkerThreadPool workerThreadPool;

    public ThreadPoolServer(int port, RequestQueue requestQueue, WorkerThreadPool workerThreadPool) {
        this.port = port;
        this.requestQueue = requestQueue;
        this.workerThreadPool = workerThreadPool;
    }

    public void start() {
        workerThreadPool.start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, requestQueue), "syos-client-handler").start();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Server failed", ex);
        } finally {
            workerThreadPool.stop();
        }
    }
}