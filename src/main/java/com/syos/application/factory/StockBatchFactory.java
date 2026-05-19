package com.syos.application.factory;

import java.time.LocalDate;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.ProductId;

public class StockBatchFactory {
    public StockBatch createBatch(String batchNumber, String productId, InventoryChannel inventoryChannel, int quantity,
                                  LocalDate expiryDate, LocalDate receivedDate) {
        return new StockBatch(
            new BatchNumber(batchNumber),
            new ProductId(productId),
            inventoryChannel,
            quantity,
            expiryDate,
            receivedDate
        );
    }

    public StockBatch createBatch(String batchNumber, String productId, InventoryChannel inventoryChannel, int quantity,
                                  LocalDate expiryDate) {
        return createBatch(batchNumber, productId, inventoryChannel, quantity, expiryDate, LocalDate.now());
    }
}
