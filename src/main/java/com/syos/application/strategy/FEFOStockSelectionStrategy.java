package com.syos.application.strategy;

import com.syos.domain.entity.StockBatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FEFOStockSelectionStrategy implements StockSelectionStrategy {
    @Override
    public List<StockBatch> selectBatches(List<StockBatch> availableBatches, int requiredQuantity) {
        List<StockBatch> validBatches = availableBatches.stream()
            .filter(batch -> !batch.isExpired() && batch.hasStock())
            .sorted(Comparator.comparing(StockBatch::getExpiryDate))
            .collect(Collectors.toList());

        List<StockBatch> selectedBatches = new ArrayList<>();
        int remainingQuantity = requiredQuantity;

        for (StockBatch batch : validBatches) {
            if (remainingQuantity <= 0) break;

            int quantityToTake = Math.min(remainingQuantity, batch.getQuantity());
            if (quantityToTake > 0) {
                selectedBatches.add(batch);
                remainingQuantity -= quantityToTake;
            }
        }

        if (remainingQuantity > 0) {
            throw new IllegalStateException("Insufficient stock available");
        }

        return selectedBatches;
    }
}
