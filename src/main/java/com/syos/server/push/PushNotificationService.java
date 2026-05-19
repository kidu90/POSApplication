package com.syos.server.push;

import java.util.ArrayList;
import java.util.List;

import com.syos.shared.PushEvent;

public class PushNotificationService {
    private final List<ConnectedClient> clients = new ArrayList<>();

    public synchronized void register(ConnectedClient client) {
        clients.add(client);
    }

    public synchronized void unregister(ConnectedClient client) {
        clients.remove(client);
    }

    public synchronized void broadcastStockChange() {
        PushEvent event = new PushEvent("STOCK_CHANGED", null);
        for (ConnectedClient client : new ArrayList<>(clients)) {
            client.sendPushEvent(event);
        }
    }

    public synchronized void broadcastProductChange() {
        PushEvent event = new PushEvent("PRODUCT_CHANGED", null);
        for (ConnectedClient client : new ArrayList<>(clients)) {
            client.sendPushEvent(event);
        }
    }
}
