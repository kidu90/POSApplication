package com.syos.client;

import java.awt.EventQueue;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import com.syos.client.network.PushListenerClient;
import com.syos.client.network.ServerConnection;
import com.syos.domain.entity.StockBatch;
import com.syos.presentation.gui.GuiAppContext;
import com.syos.presentation.gui.MainMenuFrame;
import com.syos.presentation.gui.StockManagementFrame;
import com.syos.shared.Request;
import com.syos.shared.Response;

public class ClientApplication {
    public static void main(String[] args) {
        ServerConnection serverConnection = new ServerConnection("localhost", 9090);
        GuiAppContext context = new GuiAppContext(serverConnection);

        Runnable stockRefresh = () -> {
            new SwingWorker<List<StockBatch>, Void>() {
                @Override
                protected List<StockBatch> doInBackground() {
                    Response response = serverConnection.sendRequest(new Request("GET_STOCK", Map.of()));
                    @SuppressWarnings("unchecked")
                    List<StockBatch> batches = (List<StockBatch>) response.getData();
                    return batches;
                }

                @Override
                protected void done() {
                    try {
                        StockManagementFrame stockManagementFrame = context.getStockManagementFrame();
                        if (stockManagementFrame != null) {
                            stockManagementFrame.populateStockTable(get());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        };

        PushListenerClient pushClient = new PushListenerClient("localhost", 9091, stockRefresh);
        pushClient.start();

        EventQueue.invokeLater(() -> new MainMenuFrame(context).setVisible(true));
    }
}