package com.syos.server.push;

import java.io.IOException;
import java.io.ObjectOutputStream;

import com.syos.shared.PushEvent;

public class ConnectedClient {
    private final ObjectOutputStream pushOut;
    private final PushNotificationService pushNotificationService;

    public ConnectedClient(ObjectOutputStream pushOut, PushNotificationService pushNotificationService) {
        this.pushOut = pushOut;
        this.pushNotificationService = pushNotificationService;
    }

    public synchronized void sendPushEvent(PushEvent event) {
        try {
            pushOut.writeObject(event);
            pushOut.flush();
            pushOut.reset();
        } catch (IOException ex) {
            System.err.println("Client disconnected, unregistering: " + ex.getMessage());
            pushNotificationService.unregister(this);
        }
    }
}
