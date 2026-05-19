package com.syos.application.report;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.syos.domain.entity.Product;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.ProductReadRepository;
import com.syos.domain.repository.StockReadRepository;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.ProductId;

public class StockStatusReport extends ReportGenerator {
    private static final int REORDER_LEVEL = 50;
    private final StockReadRepository stockRepository;
    private final ProductReadRepository productRepository;

    public StockStatusReport(StockReadRepository stockRepository, ProductReadRepository productRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
    }

    @Override
    protected String getReportHeader() {
        return "=".repeat(80) + "\n" +
               "STOCK STATUS REPORT - SYNEX OUTLET STORE\n" +
               "=".repeat(80);
    }

    @Override
    protected String generateReportBody() {
        List<StockBatch> allBatches = stockRepository.findAll();
        List<Product> allProducts = productRepository.findAll();

        Map<ProductId, List<StockBatch>> batchesByProduct = allBatches.stream()
            .collect(Collectors.groupingBy(StockBatch::getProductId));

        StringBuilder body = new StringBuilder();
        body.append("\nCURRENT STOCK LEVELS:\n");
        body.append("-".repeat(80)).append("\n");
        body.append(String.format("%-10s %-30s %-12s %-12s %-10s%n", "Product ID", "Name", "Store Qty", "Online Qty", "Batches"));
        body.append("-".repeat(80)).append("\n");

        for (Product product : allProducts) {
            int storeQty = stockRepository.getTotalQuantityForProductAndChannel(product.getId(), InventoryChannel.STORE);
            int onlineQty = stockRepository.getTotalQuantityForProductAndChannel(product.getId(), InventoryChannel.ONLINE);
            int batchCount = batchesByProduct.getOrDefault(product.getId(), List.of()).size();
            
            body.append(String.format("%-10s %-30s %-12d %-12d %-10d%n",
                product.getId(),
                product.getName(),
                storeQty,
                onlineQty,
                batchCount));
        }

        body.append("\nREORDER ALERTS (<= " + REORDER_LEVEL + "):\n");
        body.append("-".repeat(80)).append("\n");
        body.append(String.format("%-10s %-30s %-12s %-12s%n", "Product ID", "Name", "Store Qty", "Online Qty"));
        body.append("-".repeat(80)).append("\n");

        boolean hasReorderAlerts = false;
        for (Product product : allProducts) {
            int storeQty = stockRepository.getTotalQuantityForProductAndChannel(product.getId(), InventoryChannel.STORE);
            int onlineQty = stockRepository.getTotalQuantityForProductAndChannel(product.getId(), InventoryChannel.ONLINE);

            if (storeQty <= REORDER_LEVEL || onlineQty <= REORDER_LEVEL) {
                hasReorderAlerts = true;
                body.append(String.format("%-10s %-30s %-12d %-12d%n",
                    product.getId(),
                    product.getName(),
                    storeQty,
                    onlineQty));
            }
        }

        if (!hasReorderAlerts) {
            body.append("No products at or below reorder level.\n");
        }

        List<StockBatch> expiringSoon = stockRepository.findExpiringSoon(7);
        if (!expiringSoon.isEmpty()) {
            body.append("\nEXPIRING SOON (within 7 days):\n");
            body.append("-".repeat(80)).append("\n");
            body.append(String.format("%-15s %-10s %-15s %-15s%n", "Batch", "Product", "Quantity", "Expiry Date"));
            body.append("-".repeat(80)).append("\n");

            for (StockBatch batch : expiringSoon) {
                body.append(String.format("%-15s %-10s %-15d %-15s%n",
                    batch.getBatchNumber(),
                    batch.getProductId(),
                    batch.getQuantity(),
                    batch.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            }
        }

        List<StockBatch> expired = stockRepository.findExpired();
        if (!expired.isEmpty()) {
            body.append("\nEXPIRED STOCK:\n");
            body.append("-".repeat(80)).append("\n");
            body.append(String.format("%-15s %-10s %-15s %-15s%n", "Batch", "Product", "Quantity", "Expiry Date"));
            body.append("-".repeat(80)).append("\n");

            for (StockBatch batch : expired) {
                body.append(String.format("%-15s %-10s %-15d %-15s%n",
                    batch.getBatchNumber(),
                    batch.getProductId(),
                    batch.getQuantity(),
                    batch.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            }
        }

        return body.toString();
    }
}
