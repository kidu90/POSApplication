package com.syos.application.strategy;

import com.syos.domain.entity.StockBatch;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public interface StockSelectionStrategy {
    List<StockBatch> selectBatches(List<StockBatch> availableBatches, int requiredQuantity);
}
