package com.syos.unit.application.usecase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.syos.application.service.BillCalculationService;
import com.syos.application.service.BillNumberService;
import com.syos.application.service.InventoryManager;
import com.syos.application.strategy.DiscountStrategy;
import com.syos.application.strategy.StockSelectionStrategy;
import com.syos.application.usecase.CheckoutCommand;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.Product;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.BillWriteRepository;
import com.syos.domain.repository.ProductReadRepository;
import com.syos.domain.repository.StockReadRepository;
import com.syos.domain.repository.StockWriteRepository;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;

/**
 * Verifies checkout concurrency and persistence because checkout is the most sensitive business workflow.
 */
class CheckoutCommandTest {
    private InMemoryProductRepository productRepository;
    private InMemoryStockRepository stockRepository;
    private CountingBillRepository billRepository;
    private InventoryManager inventoryManager;
    private BillCalculationService billCalculationService;
    private BillNumberService billNumberService;
    private StockSelectionStrategy stockSelectionStrategy;
    private DiscountStrategy discountStrategy;

    CheckoutCommandTest() {
        setUp();
    }

    private void setUp() {
        productRepository = new InMemoryProductRepository();
        stockRepository = new InMemoryStockRepository();
        billRepository = new CountingBillRepository();
        inventoryManager = new InventoryManager(stockRepository, stockRepository);
        billCalculationService = new BillCalculationService();
        billNumberService = new SequentialBillNumberService();
        stockSelectionStrategy = (availableBatches, requiredQuantity) -> {
            List<StockBatch> selected = new ArrayList<>();
            int remaining = requiredQuantity;
            for (StockBatch batch : availableBatches) {
                if (remaining <= 0) {
                    break;
                }
                int quantity = Math.min(batch.getQuantity(), remaining);
                selected.add(batch);
                remaining -= quantity;
            }
            return selected;
        };
        discountStrategy = new DiscountStrategy() {
            @Override
            public Money calculateDiscount(Bill bill) {
                return new Money(0);
            }

            @Override
            public String getDescription() {
                return "No discount";
            }
        };

        Product product = new Product(new ProductId("P001"), "Rice", "Grains", new Money(10), "kg");
        productRepository.save(product);
        stockRepository.save(new StockBatch(new BatchNumber("B001"), new ProductId("P001"), InventoryChannel.STORE, 10, LocalDate.now().plusDays(10), LocalDate.now()));
    }

    @Test
    void shouldNotProduceNegativeStock_whenTwoCommandsRunConcurrently() throws Exception {
        Map<String, Integer> cart = Map.of("P001", 5);
        CountDownLatch start = new CountDownLatch(1);
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        Thread first = new Thread(() -> runCheckout(cart, Bill.SaleType.IN_STORE, start, errors));
        Thread second = new Thread(() -> runCheckout(cart, Bill.SaleType.IN_STORE, start, errors));
        first.start();
        second.start();
        start.countDown();
        first.join(2000);
        second.join(2000);

        StockBatch batch = stockRepository.findByBatchNumber(new BatchNumber("B001")).orElseThrow();

        assertAll(
            () -> assertTrue(errors.isEmpty(), "Thread errors: " + errors),
            () -> assertTrue(batch.getQuantity() >= 0)
        );
    }

    @Test
    void shouldGenerateUniqueBillNumbers_whenTenCommandsRunInParallel() throws Exception {
        Set<String> billNumbers = ConcurrentHashMap.newKeySet();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(10);
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    CheckoutCommand command = new CheckoutCommand(
                        productRepository,
                        inventoryManager,
                        billRepository,
                        billNumberService,
                        billCalculationService,
                        stockSelectionStrategy,
                        discountStrategy,
                        Map.of("P001", 1),
                        Bill.SaleType.IN_STORE,
                        null,
                        null
                    );
                    command.execute();
                    billNumbers.add(command.getGeneratedBill().getBillNumber().getValue());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    errors.add(ex);
                } catch (RuntimeException ex) {
                    errors.add(ex);
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        assertTrue(done.await(3, java.util.concurrent.TimeUnit.SECONDS));
        assertAll(
            () -> assertTrue(errors.isEmpty(), "Thread errors: " + errors),
            () -> assertEquals(10, billNumbers.size())
        );
    }

    @Test
    void shouldCreateEmptyBill_whenCartIsEmpty() {
        CheckoutCommand command = new CheckoutCommand(
            productRepository,
            inventoryManager,
            billRepository,
            billNumberService,
            billCalculationService,
            stockSelectionStrategy,
            discountStrategy,
            Map.of(),
            Bill.SaleType.IN_STORE,
            null,
            null
        );

        command.execute();

        assertAll(
            () -> assertNotNull(command.getGeneratedBill()),
            () -> assertTrue(command.getGeneratedBill().getItems().isEmpty())
        );
    }

    @Test
    void shouldSaveExactlyOneBillPerExecuteCall() {
        CheckoutCommand command = new CheckoutCommand(
            productRepository,
            inventoryManager,
            billRepository,
            billNumberService,
            billCalculationService,
            stockSelectionStrategy,
            discountStrategy,
            Map.of("P001", 1),
            Bill.SaleType.IN_STORE,
            null,
            null
        );

        command.execute();

        assertEquals(1, billRepository.saveCount.get());
    }

    private void runCheckout(Map<String, Integer> cart,
                             Bill.SaleType saleType,
                             CountDownLatch start,
                             List<Exception> errors) {
        try {
            start.await();
            CheckoutCommand command = new CheckoutCommand(
                productRepository,
                inventoryManager,
                billRepository,
                billNumberService,
                billCalculationService,
                stockSelectionStrategy,
                discountStrategy,
                cart,
                saleType,
                "Customer",
                "Address"
            );
            command.execute();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            errors.add(ex);
        } catch (RuntimeException ex) {
            errors.add(ex);
        }
    }

    private static class SequentialBillNumberService implements BillNumberService {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public String generateBillNumber(String prefix) {
            return prefix + "-TEST-" + counter.getAndIncrement();
        }
    }

    private static class CountingBillRepository implements BillWriteRepository {
        private final AtomicInteger saveCount = new AtomicInteger();
        private final List<Bill> savedBills = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void save(Bill bill) {
            saveCount.incrementAndGet();
            savedBills.add(bill);
        }
    }

    private static class InMemoryProductRepository implements ProductReadRepository, com.syos.domain.repository.ProductWriteRepository {
        private final Map<ProductId, Product> products = new HashMap<>();

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.ofNullable(products.get(id));
        }

        @Override
        public List<Product> findAll() {
            return new ArrayList<>(products.values());
        }

        @Override
        public List<Product> findByCategory(String category) {
            return new ArrayList<>(products.values());
        }

        @Override
        public void save(Product product) {
            products.put(product.getId(), product);
        }

        @Override
        public void delete(ProductId id) {
            products.remove(id);
        }
    }

    private static class InMemoryStockRepository implements StockReadRepository, StockWriteRepository {
        private final Map<BatchNumber, StockBatch> batches = new ConcurrentHashMap<>();

        @Override
        public Optional<StockBatch> findByBatchNumber(BatchNumber batchNumber) {
            return Optional.ofNullable(batches.get(batchNumber));
        }

        @Override
        public List<StockBatch> findByProductId(ProductId productId) {
            return batches.values().stream().filter(batch -> batch.getProductId().equals(productId)).toList();
        }

        @Override
        public List<StockBatch> findByProductIdAndChannel(ProductId productId, InventoryChannel channel) {
            return batches.values().stream().filter(batch -> batch.getProductId().equals(productId) && batch.getInventoryChannel() == channel).toList();
        }

        @Override
        public List<StockBatch> findAll() {
            return new ArrayList<>(batches.values());
        }

        @Override
        public List<StockBatch> findExpired() {
            return List.of();
        }

        @Override
        public List<StockBatch> findExpiringSoon(int daysThreshold) {
            return List.of();
        }

        @Override
        public int getTotalQuantityForProduct(ProductId productId) {
            return batches.values().stream().filter(batch -> batch.getProductId().equals(productId)).mapToInt(StockBatch::getQuantity).sum();
        }

        @Override
        public int getTotalQuantityForProductAndChannel(ProductId productId, InventoryChannel channel) {
            return batches.values().stream().filter(batch -> batch.getProductId().equals(productId) && batch.getInventoryChannel() == channel).mapToInt(StockBatch::getQuantity).sum();
        }

        @Override
        public void save(StockBatch batch) {
            batches.put(batch.getBatchNumber(), batch);
        }

        @Override
        public void update(StockBatch batch) {
            batches.put(batch.getBatchNumber(), batch);
        }
    }
}
