package com.syos.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.syos.shared.Request;
import com.syos.shared.Response;

public class RemoteSyosClient {
    private final String host;
    private final int port;

    public RemoteSyosClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Response send(Request request) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.writeObject(request);
            output.flush();
            return (Response) input.readObject();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to communicate with server", ex);
        }
    }
}