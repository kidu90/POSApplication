package com.syos.server;

import java.time.LocalDate;

import com.syos.application.factory.ProductFactory;
import com.syos.application.factory.StockBatchFactory;
import com.syos.application.service.BillCalculationService;
import com.syos.application.service.BillNumberGenerator;
import com.syos.application.service.BillNumberService;
import com.syos.application.service.InventoryManager;
import com.syos.application.usecase.AddProductUseCase;
import com.syos.application.usecase.AddStockUseCase;
import com.syos.domain.entity.Product;
import com.syos.domain.repository.BillRepository;
import com.syos.domain.repository.ProductRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.repository.UserRepository;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.infrastructure.database.DatabaseManager;
import com.syos.infrastructure.persistence.SQLiteBillRepository;
import com.syos.infrastructure.persistence.SQLiteProductRepository;
import com.syos.infrastructure.persistence.SQLiteStockRepository;
import com.syos.infrastructure.persistence.SQLiteUserRepository;

public class ServerApplication {
    public static void main(String[] args) {
        DatabaseManager dbManager = DatabaseManager.getInstance("syos.db");
        ProductRepository productRepository = new SQLiteProductRepository(dbManager);
        StockRepository stockRepository = new SQLiteStockRepository(dbManager);
        BillRepository billRepository = new SQLiteBillRepository(dbManager);
        UserRepository userRepository = new SQLiteUserRepository(dbManager);

        ProductFactory productFactory = new ProductFactory();
        StockBatchFactory stockBatchFactory = new StockBatchFactory();
        BillNumberService billNumberService = BillNumberGenerator.getInstance();
        BillCalculationService billCalculationService = new BillCalculationService();
        InventoryManager inventoryManager = new InventoryManager(stockRepository, stockRepository);

        AddProductUseCase addProductUseCase = new AddProductUseCase(productRepository);
        AddStockUseCase addStockUseCase = new AddStockUseCase(stockRepository);

        seedSampleData(productRepository, stockRepository, userRepository, addProductUseCase, addStockUseCase, productFactory, stockBatchFactory);

        com.syos.server.push.PushNotificationService pushService = new com.syos.server.push.PushNotificationService();

        ServerRequestProcessor processor = new ServerRequestProcessor(
            productRepository,
            stockRepository,
            billRepository,
            userRepository,
            productFactory,
            stockBatchFactory,
            billNumberService,
            billCalculationService,
            inventoryManager,
            pushService
        );

        RequestQueue requestQueue = new RequestQueue();
        WorkerThreadPool workerThreadPool = new WorkerThreadPool(5, requestQueue, processor);
        ThreadPoolServer server = new ThreadPoolServer(9090, requestQueue, workerThreadPool);
        // Start push listener on port 9091
        Thread pushThread = new Thread(new com.syos.server.push.PushListenerServer(9091, pushService), "syos-push-listener");
        pushThread.setDaemon(true);
        pushThread.start();

        System.out.println("Server starting: request port=9090, push port=9091");

        server.start();
    }

    private static void seedSampleData(ProductRepository productRepository,
                                       StockRepository stockRepository,
                                       UserRepository userRepository,
                                       AddProductUseCase addProductUseCase,
                                       AddStockUseCase addStockUseCase,
                                       ProductFactory productFactory,
                                       StockBatchFactory stockBatchFactory) {
        if (productRepository.findAll().isEmpty()) {
            Product rice = productFactory.createProduct("P001", "Basmati Rice", "Grains", 85.00, "kg");
            Product milk = productFactory.createProduct("P002", "Full Cream Milk", "Dairy", 65.00, "liter");
            Product bread = productFactory.createProduct("P003", "Whole Wheat Bread", "Bakery", 45.00, "loaf");
            Product oil = productFactory.createProduct("P004", "Sunflower Oil", "Oil", 180.00, "liter");
            Product sugar = productFactory.createProduct("P005", "White Sugar", "Grains", 55.00, "kg");

            addProductUseCase.execute(rice);
            addProductUseCase.execute(milk);
            addProductUseCase.execute(bread);
            addProductUseCase.execute(oil);
            addProductUseCase.execute(sugar);
        }

        if (stockRepository.findAll().isEmpty()) {
            addStockUseCase.execute(stockBatchFactory.createBatch("B001", "P001", InventoryChannel.STORE, 100, LocalDate.now().plusMonths(6), LocalDate.now()));
            addStockUseCase.execute(stockBatchFactory.createBatch("B002", "P001", InventoryChannel.ONLINE, 50, LocalDate.now().plusMonths(8), LocalDate.now()));
            addStockUseCase.execute(stockBatchFactory.createBatch("B003", "P002", InventoryChannel.STORE, 80, LocalDate.now().plusDays(5), LocalDate.now()));
            addStockUseCase.execute(stockBatchFactory.createBatch("B004", "P003", InventoryChannel.STORE, 40, LocalDate.now().plusDays(2), LocalDate.now()));
            addStockUseCase.execute(stockBatchFactory.createBatch("B005", "P004", InventoryChannel.ONLINE, 60, LocalDate.now().plusMonths(12), LocalDate.now()));
            addStockUseCase.execute(stockBatchFactory.createBatch("B006", "P005", InventoryChannel.ONLINE, 120, LocalDate.now().plusMonths(10), LocalDate.now()));
        }

        if (userRepository.findByUsername("rajesh").isEmpty()) {
            userRepository.save(new com.syos.domain.entity.User("rajesh", "password", "Rajesh Kumar", "123 MG Road, Bangalore"));
        }
    }
}