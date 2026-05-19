package com.syos.unit.application.strategy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.syos.application.strategy.FEFOStockSelectionStrategy;
import com.syos.application.strategy.FIFOStockSelectionStrategy;
import com.syos.application.strategy.StockSelectionStrategy;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.ProductId;

class StockSelectionStrategyTest {

    @Test
    void fifoStrategyShouldSelectOldestBatchesFirst() {
        List<StockBatch> batches = new ArrayList<>();
        batches.add(new StockBatch(
            new BatchNumber("B003"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            30,
            LocalDate.now().plusMonths(3),
            LocalDate.now().minusDays(5)
        ));
        batches.add(new StockBatch(
            new BatchNumber("B001"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            50,
            LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(10)
        ));
        batches.add(new StockBatch(
            new BatchNumber("B002"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            40,
            LocalDate.now().plusMonths(6),
            LocalDate.now().minusDays(8)
        ));

        StockSelectionStrategy strategy = new FIFOStockSelectionStrategy();
        List<StockBatch> selected = strategy.selectBatches(batches, 60);

        assertEquals(2, selected.size());
        assertEquals("B003", selected.get(0).getBatchNumber().getValue());
        assertEquals("B001", selected.get(1).getBatchNumber().getValue());
    }

    @Test
    void fefoStrategyShouldSelectEarliestExpiringBatchesFirst() {
        List<StockBatch> batches = new ArrayList<>();
        batches.add(new StockBatch(
            new BatchNumber("B001"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            50,
            LocalDate.now().plusMonths(6),
            LocalDate.now()
        ));
        batches.add(new StockBatch(
            new BatchNumber("B002"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            40,
            LocalDate.now().plusMonths(3),
            LocalDate.now()
        ));
        batches.add(new StockBatch(
            new BatchNumber("B003"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            30,
            LocalDate.now().plusMonths(9),
            LocalDate.now()
        ));

        StockSelectionStrategy strategy = new FEFOStockSelectionStrategy();
        List<StockBatch> selected = strategy.selectBatches(batches, 60);

        assertEquals(2, selected.size());
        assertEquals("B002", selected.get(0).getBatchNumber().getValue());
        assertEquals("B001", selected.get(1).getBatchNumber().getValue());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        List<StockBatch> batches = new ArrayList<>();
        batches.add(new StockBatch(
            new BatchNumber("B001"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            30,
            LocalDate.now().plusMonths(6),
            LocalDate.now()
        ));

        StockSelectionStrategy strategy = new FIFOStockSelectionStrategy();

        assertThrows(IllegalStateException.class, () ->
            strategy.selectBatches(batches, 50)
        );
    }

    @Test
    void shouldSkipExpiredBatches() {
        List<StockBatch> batches = new ArrayList<>();
        batches.add(new StockBatch(
            new BatchNumber("B001"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            50,
            LocalDate.now().minusDays(1),
            LocalDate.now().minusMonths(2)
        ));
        batches.add(new StockBatch(
            new BatchNumber("B002"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            40,
            LocalDate.now().plusMonths(6),
            LocalDate.now()
        ));

        StockSelectionStrategy strategy = new FEFOStockSelectionStrategy();
        List<StockBatch> selected = strategy.selectBatches(batches, 30);

        assertEquals(1, selected.size());
        assertEquals("B002", selected.get(0).getBatchNumber().getValue());
    }

    @Test
    void shouldThrowExceptionWhenZeroStock() {
        List<StockBatch> batches = new ArrayList<>();
        batches.add(new StockBatch(
            new BatchNumber("B001"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            0,
            LocalDate.now().plusMonths(6),
            LocalDate.now()
        ));

        StockSelectionStrategy strategy = new FEFOStockSelectionStrategy();

        assertThrows(IllegalStateException.class, () ->
            strategy.selectBatches(batches, 1)
        );
    }
}
