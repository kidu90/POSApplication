package com.syos.presentation.gui;

import com.syos.client.network.ServerConnection;
import com.syos.client.SyosClientService;

public class GuiAppContext {
    private final SyosClientService clientService;
    private volatile StockManagementFrame stockManagementFrame;

    public GuiAppContext(ServerConnection connection) {
        this.clientService = new SyosClientService(connection);
    }

    public GuiAppContext(String serverHost, int serverPort) {
        this(new ServerConnection(serverHost, serverPort));
    }

    public SyosClientService getClientService() {
        return clientService;
    }

    public void setStockManagementFrame(StockManagementFrame stockManagementFrame) {
        this.stockManagementFrame = stockManagementFrame;
    }

    public StockManagementFrame getStockManagementFrame() {
        return stockManagementFrame;
    }
}