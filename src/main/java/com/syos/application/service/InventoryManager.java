package com.syos.application.service;

import java.util.List;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.StockReadRepository;
import com.syos.domain.repository.StockWriteRepository;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.ProductId;

public class InventoryManager {
    private final StockReadRepository stockReadRepository;
    private final StockWriteRepository stockWriteRepository;

    public InventoryManager(StockReadRepository stockReadRepository, StockWriteRepository stockWriteRepository) {
        this.stockReadRepository = stockReadRepository;
        this.stockWriteRepository = stockWriteRepository;
    }

    public List<StockBatch> getAvailableBatches(ProductId productId, InventoryChannel channel) {
        return stockReadRepository.findByProductIdAndChannel(productId, channel);
    }

    public int getTotalQuantity(ProductId productId, InventoryChannel channel) {
        return stockReadRepository.getTotalQuantityForProductAndChannel(productId, channel);
    }

    public void addStock(StockBatch batch, InventoryChannel channel) {
        if (batch.getInventoryChannel() != channel) {
            throw new IllegalArgumentException("Stock batch channel mismatch: " + channel);
        }
        stockWriteRepository.save(batch);
    }

    public void updateStock(StockBatch batch) {
        stockWriteRepository.update(batch);
    }
}
