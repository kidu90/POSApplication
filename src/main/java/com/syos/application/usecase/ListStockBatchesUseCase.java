package com.syos.application.usecase;

import java.util.List;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.StockReadRepository;

public class ListStockBatchesUseCase {
    private final StockReadRepository stockRepository;

    public ListStockBatchesUseCase(StockReadRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public List<StockBatch> execute() {
        return stockRepository.findAll();
    }
}