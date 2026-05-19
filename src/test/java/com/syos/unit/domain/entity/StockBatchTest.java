package com.syos.unit.domain.entity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.ProductId;

class StockBatchTest {

    private StockBatch batch;

    @BeforeEach
    void setUp() {
        batch = new StockBatch(
            new BatchNumber("B001"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            100,
            LocalDate.now().plusDays(30),
            LocalDate.now()
        );
    }

    @Test
    void shouldCreateBatchWithValidData() {
        assertEquals("B001", batch.getBatchNumber().getValue());
        assertEquals("P001", batch.getProductId().getValue());
        assertEquals(InventoryChannel.STORE, batch.getInventoryChannel());
        assertEquals(100, batch.getQuantity());
        assertTrue(batch.hasStock());
    }

    @Test
    void shouldReduceQuantityCorrectly() {
        batch.reduceQuantity(30);
        assertEquals(70, batch.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenReducingMoreThanAvailable() {
        assertThrows(IllegalArgumentException.class, () -> batch.reduceQuantity(150));
    }

    @Test
    void shouldAddQuantityCorrectly() {
        batch.addQuantity(50);
        assertEquals(150, batch.getQuantity());
    }

    @Test
    void shouldDetectExpiredBatch() {
        StockBatch expiredBatch = new StockBatch(
            new BatchNumber("B002"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            50,
            LocalDate.now().minusDays(1),
            LocalDate.now().minusMonths(2)
        );

        assertTrue(expiredBatch.isExpired());
    }

    @Test
    void shouldDetectExpiringSoonBatch() {
        StockBatch expiringSoonBatch = new StockBatch(
            new BatchNumber("B003"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            50,
            LocalDate.now().plusDays(5),
            LocalDate.now()
        );

        assertTrue(expiringSoonBatch.isExpiringSoon(7));
        assertFalse(expiringSoonBatch.isExpired());
    }

    @Test
    void shouldDetectNoStock() {
        batch.reduceQuantity(100);
        assertFalse(batch.hasStock());
        assertEquals(0, batch.getQuantity());
    }
}
