package com.syos.client.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.syos.shared.Request;
import com.syos.shared.Response;

public class ServerConnection {
    private final String host;
    private final int port;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

  
    public synchronized Response sendRequest(Request request) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException("Server communication failed", ex);
        }
    }
}
