package com.syos.domain.repository;

import java.util.List;
import java.util.Optional;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.ProductId;

public interface StockReadRepository {
    Optional<StockBatch> findByBatchNumber(BatchNumber batchNumber);
    List<StockBatch> findByProductId(ProductId productId);
    List<StockBatch> findByProductIdAndChannel(ProductId productId, InventoryChannel channel);
    List<StockBatch> findAll();
    List<StockBatch> findExpired();
    List<StockBatch> findExpiringSoon(int daysThreshold);
    int getTotalQuantityForProduct(ProductId productId);
    int getTotalQuantityForProductAndChannel(ProductId productId, InventoryChannel channel);
}
