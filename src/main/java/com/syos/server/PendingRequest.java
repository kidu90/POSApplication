package com.syos.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.syos.shared.Request;
import com.syos.shared.Response;

public class PendingRequest {
    private final Request request;
    private final Socket socket;
    private final ObjectOutputStream outputStream;

    public PendingRequest(Request request, Socket socket, ObjectOutputStream outputStream) {
        this.request = request;
        this.socket = socket;
        this.outputStream = outputStream;
    }

    public Request getRequest() {
        return request;
    }

    public synchronized void complete(Response response) {
        try {
            outputStream.writeObject(response);
            outputStream.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to send response", ex);
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Nothing else we can do here.
            }
        }
    }
}