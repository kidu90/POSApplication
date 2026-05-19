package com.syos.integration.infrastructure.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.ProductId;
import com.syos.infrastructure.database.DatabaseManager;
import com.syos.infrastructure.persistence.SQLiteStockRepository;

class SQLiteStockRepositoryIT {

    private Path tempDb;
    private SQLiteStockRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        tempDb = Files.createTempFile("syos-test-", ".db");
        DatabaseManager.resetInstance();
        DatabaseManager dbManager = DatabaseManager.getInstance(tempDb.toString());
        repository = new SQLiteStockRepository(dbManager);
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseManager.resetInstance();
        Files.deleteIfExists(tempDb);
    }

    @Test
    void shouldPersistAndLoadByChannel() {
        StockBatch storeBatch = new StockBatch(
            new BatchNumber("B001"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            25,
            LocalDate.now().plusMonths(2),
            LocalDate.now()
        );
        StockBatch onlineBatch = new StockBatch(
            new BatchNumber("B002"),
            new ProductId("P001"),
            InventoryChannel.ONLINE,
            30,
            LocalDate.now().plusMonths(2),
            LocalDate.now()
        );

        repository.save(storeBatch);
        repository.save(onlineBatch);

        List<StockBatch> storeBatches = repository.findByProductIdAndChannel(new ProductId("P001"), InventoryChannel.STORE);
        List<StockBatch> onlineBatches = repository.findByProductIdAndChannel(new ProductId("P001"), InventoryChannel.ONLINE);

        assertEquals(1, storeBatches.size());
        assertEquals(1, onlineBatches.size());
        assertEquals(InventoryChannel.STORE, storeBatches.get(0).getInventoryChannel());
        assertEquals(InventoryChannel.ONLINE, onlineBatches.get(0).getInventoryChannel());
    }
}
