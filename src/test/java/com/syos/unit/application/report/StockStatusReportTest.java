package com.syos.unit.application.report;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.syos.application.report.StockStatusReport;
import com.syos.domain.entity.Product;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.ProductReadRepository;
import com.syos.domain.repository.StockReadRepository;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;

class StockStatusReportTest {

    @Test
    void shouldFlagReorderLevelAtExactlyFifty() {
        Product product = new Product(new ProductId("P001"), "Rice", "Grains", new Money(10.0), "kg");

        StockBatch storeBatch = new StockBatch(
            new BatchNumber("B001"),
            new ProductId("P001"),
            InventoryChannel.STORE,
            50,
            LocalDate.now().plusMonths(6),
            LocalDate.now()
        );

        StockBatch onlineBatch = new StockBatch(
            new BatchNumber("B002"),
            new ProductId("P001"),
            InventoryChannel.ONLINE,
            60,
            LocalDate.now().plusMonths(6),
            LocalDate.now()
        );

        StockReadRepository stockRepository = new InMemoryStockReadRepository(List.of(storeBatch, onlineBatch));
        ProductReadRepository productRepository = new InMemoryProductReadRepository(List.of(product));

        StockStatusReport report = new StockStatusReport(stockRepository, productRepository);

        String output = report.generateReport();

        assertTrue(output.contains("REORDER ALERTS"));
        assertTrue(output.contains("P001"));
        assertTrue(output.contains("50"));
    }

    private static class InMemoryStockReadRepository implements StockReadRepository {
        private final List<StockBatch> batches;

        private InMemoryStockReadRepository(List<StockBatch> batches) {
            this.batches = batches;
        }

        @Override
        public Optional<StockBatch> findByBatchNumber(BatchNumber batchNumber) {
            return batches.stream().filter(b -> b.getBatchNumber().equals(batchNumber)).findFirst();
        }

        @Override
        public List<StockBatch> findByProductId(ProductId productId) {
            return batches.stream().filter(b -> b.getProductId().equals(productId)).toList();
        }

        @Override
        public List<StockBatch> findByProductIdAndChannel(ProductId productId, InventoryChannel channel) {
            return batches.stream()
                .filter(b -> b.getProductId().equals(productId))
                .filter(b -> b.getInventoryChannel() == channel)
                .toList();
        }

        @Override
        public List<StockBatch> findAll() {
            return batches;
        }

        @Override
        public List<StockBatch> findExpired() {
            return Collections.emptyList();
        }

        @Override
        public List<StockBatch> findExpiringSoon(int daysThreshold) {
            return Collections.emptyList();
        }

        @Override
        public int getTotalQuantityForProduct(ProductId productId) {
            return batches.stream()
                .filter(b -> b.getProductId().equals(productId))
                .mapToInt(StockBatch::getQuantity)
                .sum();
        }

        @Override
        public int getTotalQuantityForProductAndChannel(ProductId productId, InventoryChannel channel) {
            return batches.stream()
                .filter(b -> b.getProductId().equals(productId))
                .filter(b -> b.getInventoryChannel() == channel)
                .mapToInt(StockBatch::getQuantity)
                .sum();
        }
    }

    private static class InMemoryProductReadRepository implements ProductReadRepository {
        private final List<Product> products;

        private InMemoryProductReadRepository(List<Product> products) {
            this.products = products;
        }

        @Override
        public Optional<Product> findById(ProductId id) {
            return products.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public List<Product> findAll() {
            return products;
        }

        @Override
        public List<Product> findByCategory(String category) {
            return products.stream().filter(p -> p.getCategory().equals(category)).toList();
        }
    }
}
