package com.syos.domain.repository;

import com.syos.domain.entity.StockBatch;

public interface StockWriteRepository {
    void save(StockBatch batch);
    void update(StockBatch batch);
}
