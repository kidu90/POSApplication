package com.syos.application.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.syos.domain.entity.StockBatch;

public class FIFOStockSelectionStrategy implements StockSelectionStrategy {
    @Override
    public List<StockBatch> selectBatches(List<StockBatch> availableBatches, int requiredQuantity) {
        List<StockBatch> validBatches = availableBatches.stream()
            .filter(batch -> !batch.isExpired() && batch.hasStock())
            .sorted(Comparator.comparing(StockBatch::getExpiryDate)
                .thenComparing(StockBatch::getReceivedDate))
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
