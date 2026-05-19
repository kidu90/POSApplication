package com.syos.unit.server;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.syos.application.factory.ProductFactory;
import com.syos.application.factory.StockBatchFactory;
import com.syos.application.service.BillCalculationService;
import com.syos.application.service.BillNumberGenerator;
import com.syos.application.service.InventoryManager;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.Product;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.entity.User;
import com.syos.domain.repository.BillRepository;
import com.syos.domain.repository.ProductRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.repository.UserRepository;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.BillNumber;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;
import com.syos.server.ServerRequestHandler;
import com.syos.server.push.PushNotificationService;
import com.syos.shared.Request;
import com.syos.shared.Response;

/**
 * Verifies request dispatch because the server must map wire actions to use cases deterministically.
 */
class ServerRequestHandlerTest {
    private InMemoryProductRepository productRepository;
    private InMemoryStockRepository stockRepository;
    private InMemoryBillRepository billRepository;
    private InMemoryUserRepository userRepository;
    private ServerRequestHandler handler;

    ServerRequestHandlerTest() {
        setUp();
    }

    private void setUp() {
        BillNumberGenerator.getInstance().reset();
        productRepository = new InMemoryProductRepository();
        stockRepository = new InMemoryStockRepository();
        billRepository = new InMemoryBillRepository();
        userRepository = new InMemoryUserRepository();

        productRepository.save(new Product(new ProductId("P001"), "Rice", "Grains", new Money(10), "kg"));
        productRepository.save(new Product(new ProductId("P005"), "Sugar", "Grains", new Money(12), "kg"));
        stockRepository.save(new StockBatch(new BatchNumber("B001"), new ProductId("P001"), InventoryChannel.STORE, 10, LocalDate.now().plusDays(10), LocalDate.now()));
        stockRepository.save(new StockBatch(new BatchNumber("B005"), new ProductId("P005"), InventoryChannel.ONLINE, 10, LocalDate.now().plusDays(10), LocalDate.now()));
        userRepository.save(new User("alice", "password", "Alice Example", "1 Test Lane"));

        handler = new ServerRequestHandler(
            productRepository,
            stockRepository,
            billRepository,
            userRepository,
            new ProductFactory(),
            new StockBatchFactory(),
            BillNumberGenerator.getInstance(),
            new BillCalculationService(),
            new InventoryManager(stockRepository, stockRepository),
            new PushNotificationService()
        );
    }

    @Test
    void shouldLoginSuccessfully_whenCredentialsAreValid() {
        Response response = handler.handle(new Request("LOGIN", Map.of("username", "alice", "password", "password")));

        assertAll(
            () -> assertTrue(response.isSuccess()),
            () -> assertTrue(response.getData() instanceof User),
            () -> assertEquals("Alice Example", ((User) response.getData()).getFullName())
        );
    }

    @Test
    void shouldFailLogin_whenPasswordIsWrong() {
        Response response = handler.handle(new Request("LOGIN", Map.of("username", "alice", "password", "wrong")));

        assertFalse(response.isSuccess());
    }

    @Test
    void shouldFailLogin_whenUsernameIsUnknown() {
        Response response = handler.handle(new Request("LOGIN", Map.of("username", "missing", "password", "password")));

        assertFalse(response.isSuccess());
    }

    @Test
    void shouldRegisterUser_whenUsernameIsNew() {
        Response response = handler.handle(new Request("REGISTER", Map.of(
            "fullName", "Bob Example",
            "address", "2 Test Lane",
            "username", "bob",
            "password", "password"
        )));

        assertAll(
            () -> assertTrue(response.isSuccess()),
            () -> assertTrue(response.getData() instanceof User),
            () -> assertNotNull(userRepository.findByUsername("bob").orElse(null))
        );
    }

    @Test
    void shouldFailRegister_whenUsernameIsDuplicate() {
        Response response = handler.handle(new Request("REGISTER", Map.of(
            "fullName", "Alice Example",
            "address", "1 Test Lane",
            "username", "alice",
            "password", "password"
        )));

        assertFalse(response.isSuccess());
    }

    @Test
    void shouldReturnProducts_whenGetProductsIsRequested() {
        Response response = handler.handle(new Request("GET_PRODUCTS", Map.of()));

        assertAll(
            () -> assertTrue(response.isSuccess()),
            () -> assertTrue(response.getData() instanceof List),
            () -> assertFalse(((List<?>) response.getData()).isEmpty())
        );
    }

    @Test
    void shouldReturnStock_whenGetStockIsRequested() {
        Response response = handler.handle(new Request("GET_STOCK", Map.of()));

        assertAll(
            () -> assertTrue(response.isSuccess()),
            () -> assertTrue(response.getData() instanceof List),
            () -> assertFalse(((List<?>) response.getData()).isEmpty())
        );
    }

    @Test
    void shouldAddProductOnce_whenAddProductIsRequested() {
        int before = productRepository.saveCount;
        Response response = handler.handle(new Request("ADD_PRODUCT", Map.of(
            "id", "P100",
            "name", "Tea",
            "category", "Beverages",
            "price", 15.0,
            "unit", "pack"
        )));

        assertAll(
            () -> assertTrue(response.isSuccess()),
            () -> assertEquals(before + 1, productRepository.saveCount)
        );
    }

    @Test
    void shouldAddStockOnce_whenAddStockIsRequested() {
        int before = stockRepository.saveCount;
        Response response = handler.handle(new Request("ADD_STOCK", Map.of(
            "batchNumber", "B100",
            "productId", "P001",
            "channel", "STORE",
            "quantity", 2,
            "expiryDate", LocalDate.now().plusDays(20).toString(),
            "receivedDate", LocalDate.now().toString()
        )));

        assertAll(
            () -> assertTrue(response.isSuccess()),
            () -> assertEquals(before + 1, stockRepository.saveCount)
        );
    }

    @Test
    void shouldCheckoutInStore_whenCartIsValid() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("cart", Map.of("P001", 1));
        params.put("customerName", null);
        params.put("customerAddress", null);

        Response response = handler.handle(new Request("CHECKOUT_INSTORE", params));

        assertAll(
            () -> assertTrue(response.isSuccess()),
            () -> assertTrue(response.getData() instanceof Bill),
            () -> assertTrue(((Bill) response.getData()).getBillNumber().getValue().contains("POS"))
        );
    }

    @Test
    void shouldCheckoutOnline_whenCustomerDetailsAreProvided() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("cart", Map.of("P005", 1));
        params.put("customerName", "Alice");
        params.put("customerAddress", "Street");

        Response response = handler.handle(new Request("CHECKOUT_ONLINE", params));

        assertAll(
            () -> assertTrue(response.isSuccess()),
            () -> assertTrue(response.getData() instanceof Bill),
            () -> assertTrue(((Bill) response.getData()).getBillNumber().getValue().contains("ONL"))
        );
    }

    @Test
    void shouldReturnFailure_whenCheckoutProductHasNoStock() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("cart", Map.of("P001", 999));
        params.put("customerName", null);
        params.put("customerAddress", null);

        Response response = handler.handle(new Request("CHECKOUT_INSTORE", params));

        assertAll(
            () -> assertFalse(response.isSuccess()),
            () -> assertTrue(response.getMessage().toLowerCase().contains("stock"))
        );
    }

    @Test
    void shouldReturnDailyReport_whenRequested() {
        billRepository.save(new Bill(new BillNumber("POS-TEST-1"), LocalDate.now().atStartOfDay(), Bill.SaleType.IN_STORE));
        Response response = handler.handle(new Request("GET_DAILY_REPORT", Map.of()));

        assertAll(
            () -> assertTrue(response.isSuccess()),
            () -> assertTrue(response.getData() instanceof String),
            () -> assertFalse(((String) response.getData()).isBlank())
        );
    }

    @Test
    void shouldReturnStockReport_whenRequested() {
        Response response = handler.handle(new Request("GET_STOCK_REPORT", Map.of()));

        assertAll(
            () -> assertTrue(response.isSuccess()),
            () -> assertTrue(response.getData() instanceof String),
            () -> assertFalse(((String) response.getData()).isBlank())
        );
    }

    @Test
    void shouldFailUnknownAction_whenActionIsUnsupported() {
        Response response = handler.handle(new Request("DOES_NOT_EXIST", Map.of()));

        assertAll(
            () -> assertFalse(response.isSuccess()),
            () -> assertTrue(response.getMessage().toLowerCase().contains("unknown action"))
        );
    }

    private static class InMemoryProductRepository implements ProductRepository {
        private final Map<ProductId, Product> products = new LinkedHashMap<>();
        private int saveCount;

        @Override
        public void save(Product product) {
            saveCount++;
            products.put(product.getId(), product);
        }

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
        public void delete(ProductId id) {
            products.remove(id);
        }
    }

    private static class InMemoryStockRepository implements StockRepository {
        private final Map<BatchNumber, StockBatch> batches = new LinkedHashMap<>();
        private int saveCount;

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
            saveCount++;
            batches.put(batch.getBatchNumber(), batch);
        }

        @Override
        public void update(StockBatch batch) {
            batches.put(batch.getBatchNumber(), batch);
        }
    }

    private static class InMemoryBillRepository implements BillRepository {
        private final List<Bill> bills = new ArrayList<>();

        @Override
        public void save(Bill bill) {
            bills.add(bill);
        }

        @Override
        public Optional<Bill> findByBillNumber(BillNumber billNumber) {
            return bills.stream().filter(bill -> bill.getBillNumber().equals(billNumber)).findFirst();
        }

        @Override
        public List<Bill> findAll() {
            return new ArrayList<>(bills);
        }

        @Override
        public List<Bill> findByDate(LocalDate date) {
            return new ArrayList<>(bills);
        }

        @Override
        public List<Bill> findBySaleType(Bill.SaleType saleType) {
            return new ArrayList<>(bills);
        }
    }

    private static class InMemoryUserRepository implements UserRepository {
        private final Map<String, User> users = new HashMap<>();

        @Override
        public void save(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(users.get(username));
        }
    }
}
