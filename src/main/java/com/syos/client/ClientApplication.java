package com.syos.client;

import java.awt.EventQueue;
import java.util.UUID;

import com.syos.client.network.PushListenerClient;
import com.syos.client.network.ServerConnection;
import com.syos.presentation.gui.GuiAppContext;
import com.syos.presentation.gui.MainMenuFrame;
import com.syos.presentation.gui.StockManagementFrame;

public class ClientApplication {
    public static void main(String[] args) {
        ServerConnection serverConnection = new ServerConnection("localhost", 9090);
        GuiAppContext context = new GuiAppContext(serverConnection);

        StockManagementFrame stockManagementFrame = new StockManagementFrame(context);
        context.setStockManagementFrame(stockManagementFrame);
        stockManagementFrame.refreshAll();
        String clientId = UUID.randomUUID().toString();
        PushListenerClient pushClient = new PushListenerClient(
            "localhost",
            9091,
            clientId,
            serverConnection,
            stockManagementFrame
        );
        pushClient.start();

        EventQueue.invokeLater(() -> new MainMenuFrame(context).setVisible(true));
    }
}