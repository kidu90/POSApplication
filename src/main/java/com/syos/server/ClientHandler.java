package com.syos.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.syos.shared.Request;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final RequestQueue requestQueue;

    public ClientHandler(Socket socket, RequestQueue requestQueue) {
        this.socket = socket;
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            Request request = (Request) inputStream.readObject();
            requestQueue.enqueue(new PendingRequest(request, socket, outputStream));
        } catch (IOException | ClassNotFoundException ex) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Ignore close failures.
            }
        }
    }
}