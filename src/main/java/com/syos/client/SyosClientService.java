package com.syos.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.Product;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.entity.User;
import com.syos.shared.Request;
import com.syos.shared.Response;

public class SyosClientService {
    private final RemoteSyosClient remoteClient;
    private final com.syos.client.network.ServerConnection serverConnection;

    public SyosClientService(RemoteSyosClient remoteClient) {
        this.remoteClient = remoteClient;
        this.serverConnection = null;
    }

    public SyosClientService(com.syos.client.network.ServerConnection serverConnection) {
        this.remoteClient = null;
        this.serverConnection = serverConnection;
    }

    public User login(String username, String password) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("username", username);
        params.put("password", password);
        return castData(send("LOGIN", params));
    }

    public User register(String fullName, String address, String username, String password) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("fullName", fullName);
        params.put("address", address);
        params.put("username", username);
        params.put("password", password);
        return castData(send("REGISTER", params));
    }

    public List<Product> getProducts() {
        return castData(send("GET_PRODUCTS", Map.of()));
    }

    public List<StockBatch> getStock() {
        return castData(send("GET_STOCK", Map.of()));
    }

    public void addProduct(String id, String name, String category, double price, String unit) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("id", id);
        params.put("name", name);
        params.put("category", category);
        params.put("price", price);
        params.put("unit", unit);
        send("ADD_PRODUCT", params);
    }

    public void addStock(String batchNumber, String productId, String channel, int quantity, String expiryDate, String receivedDate) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("batchNumber", batchNumber);
        params.put("productId", productId);
        params.put("channel", channel);
        params.put("quantity", quantity);
        params.put("expiryDate", expiryDate);
        params.put("receivedDate", receivedDate);
        send("ADD_STOCK", params);
    }

    public Bill checkoutInStore(Map<String, Integer> cart) {
        return checkout("CHECKOUT_INSTORE", cart, null, null);
    }

    public Bill checkoutOnline(Map<String, Integer> cart, String customerName, String customerAddress) {
        return checkout("CHECKOUT_ONLINE", cart, customerName, customerAddress);
    }

    public String getDailyReport() {
        return castData(send("GET_DAILY_REPORT", Map.of()));
    }

    public String getStockReport() {
        return castData(send("GET_STOCK_REPORT", Map.of()));
    }

    public String getBillReport() {
        return castData(send("GET_BILL_REPORT", Map.of()));
    }

    public String getReshelveReport() {
        return castData(send("GET_RESHELVE_REPORT", Map.of()));
    }

    private Bill checkout(String action, Map<String, Integer> cart, String customerName, String customerAddress) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("cart", new LinkedHashMap<>(cart));
        params.put("customerName", customerName);
        params.put("customerAddress", customerAddress);
        return castData(send(action, params));
    }

    private Response send(String action, Map<String, Object> params) {
        Request req = new Request(action, params);
        Response response;
        if (serverConnection != null) {
            response = serverConnection.sendRequest(req);
        } else {
            response = remoteClient.send(req);
        }
        if (!response.isSuccess()) {
            throw new IllegalArgumentException(response.getMessage());
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    private <T> T castData(Response response) {
        return (T) response.getData();
    }
}