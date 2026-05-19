package com.syos.application.usecase;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.StockWriteRepository;

public class AddStockUseCase {
    private final StockWriteRepository stockRepository;

    public AddStockUseCase(StockWriteRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void execute(StockBatch batch) {
        if (batch == null) {
            throw new IllegalArgumentException("Stock batch cannot be null");
        }
        stockRepository.save(batch);
    }
}
