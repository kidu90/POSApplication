package com.syos.presentation.console;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.syos.application.factory.ProductFactory;
import com.syos.application.factory.StockBatchFactory;
import com.syos.application.report.IReportGenerator;
import com.syos.application.service.BillCalculationService;
import com.syos.application.service.BillNumberService;
import com.syos.application.service.InventoryManager;
import com.syos.application.strategy.DiscountStrategy;
import com.syos.application.strategy.StockSelectionStrategy;
import com.syos.application.usecase.AddProductUseCase;
import com.syos.application.usecase.CheckoutCommand;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.Product;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.BillWriteRepository;
import com.syos.domain.repository.ProductReadRepository;
import com.syos.domain.valueobject.InventoryChannel;

public class SYOSConsoleUI {
    private final ProductReadRepository productReadRepository;
    private final BillWriteRepository billWriteRepository;
    private final InventoryManager inventoryManager;
    private final AddProductUseCase addProductUseCase;
    private final ProductFactory productFactory;
    private final StockBatchFactory stockBatchFactory;
    private final BillNumberService billNumberService;
    private final BillCalculationService billCalculationService;
    private final StockSelectionStrategy stockSelectionStrategy;
    private final DiscountStrategy inStoreDiscountStrategy;
    private final DiscountStrategy onlineDiscountStrategy;
    private final BillPrinter billPrinter;
    private final IReportGenerator dailySalesReport;
    private final IReportGenerator stockStatusReport;

    public SYOSConsoleUI(ProductReadRepository productReadRepository,
                         BillWriteRepository billWriteRepository,
                         InventoryManager inventoryManager,
                         AddProductUseCase addProductUseCase,
                         ProductFactory productFactory,
                         StockBatchFactory stockBatchFactory,
                         BillNumberService billNumberService,
                         BillCalculationService billCalculationService,
                         StockSelectionStrategy stockSelectionStrategy,
                         DiscountStrategy inStoreDiscountStrategy,
                         DiscountStrategy onlineDiscountStrategy,
                         BillPrinter billPrinter,
                         IReportGenerator dailySalesReport,
                         IReportGenerator stockStatusReport) {
        this.productReadRepository = productReadRepository;
        this.billWriteRepository = billWriteRepository;
        this.inventoryManager = inventoryManager;
        this.addProductUseCase = addProductUseCase;
        this.productFactory = productFactory;
        this.stockBatchFactory = stockBatchFactory;
        this.billNumberService = billNumberService;
        this.billCalculationService = billCalculationService;
        this.stockSelectionStrategy = stockSelectionStrategy;
        this.inStoreDiscountStrategy = inStoreDiscountStrategy;
        this.onlineDiscountStrategy = onlineDiscountStrategy;
        this.billPrinter = billPrinter;
        this.dailySalesReport = dailySalesReport;
        this.stockStatusReport = stockStatusReport;
    }

    public void run() {
        printHeader();

        System.out.println("\n>>> INITIALIZING SYSTEM <<<\n");
        initializeSampleData();

        System.out.println("\n>>> SCENARIO 1: IN-STORE TRANSACTION <<<\n");
        performInStoreTransaction();

        System.out.println("\n>>> SCENARIO 2: ONLINE TRANSACTION <<<\n");
        performOnlineTransaction();

        System.out.println("\n>>> GENERATING REPORTS <<<\n");
        generateReports();

        printFooter();
    }

    private void printHeader() {
        System.out.println("=".repeat(80));
        System.out.println(" ".repeat(20) + "SYNEX OUTLET STORE (SYOS)");
        System.out.println(" ".repeat(15) + "Billing & Stock Management System");
        System.out.println("=".repeat(80));
    }

    private void printFooter() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(" ".repeat(25) + "System Demonstration Complete");
        System.out.println("=".repeat(80));
    }

    private void initializeSampleData() {
        System.out.println("Adding products to catalog...");

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

        System.out.println("✓ Added 5 products to catalog");

        System.out.println("\nAdding stock batches...");

        StockBatch riceBatch1 = stockBatchFactory.createBatch("B001", "P001", InventoryChannel.STORE, 100, LocalDate.now().plusMonths(6));
        StockBatch riceBatch2 = stockBatchFactory.createBatch("B002", "P001", InventoryChannel.ONLINE, 50, LocalDate.now().plusMonths(8));
        StockBatch milkBatch = stockBatchFactory.createBatch("B003", "P002", InventoryChannel.STORE, 80, LocalDate.now().plusDays(5));
        StockBatch breadBatch = stockBatchFactory.createBatch("B004", "P003", InventoryChannel.STORE, 40, LocalDate.now().plusDays(2));
        StockBatch oilBatch = stockBatchFactory.createBatch("B005", "P004", InventoryChannel.ONLINE, 60, LocalDate.now().plusMonths(12));
        StockBatch sugarBatch = stockBatchFactory.createBatch("B006", "P005", InventoryChannel.ONLINE, 120, LocalDate.now().plusMonths(10));

        inventoryManager.addStock(riceBatch1, InventoryChannel.STORE);
        inventoryManager.addStock(riceBatch2, InventoryChannel.ONLINE);
        inventoryManager.addStock(milkBatch, InventoryChannel.STORE);
        inventoryManager.addStock(breadBatch, InventoryChannel.STORE);
        inventoryManager.addStock(oilBatch, InventoryChannel.ONLINE);
        inventoryManager.addStock(sugarBatch, InventoryChannel.ONLINE);

        System.out.println("✓ Added 6 stock batches");
        System.out.println("✓ System initialized successfully");
    }

    private void performInStoreTransaction() {
        System.out.println("Processing in-store sale at POS terminal...");
        System.out.println("\nCustomer Cart:");
        System.out.println("  - Basmati Rice (P001): 5 kg");
        System.out.println("  - Full Cream Milk (P002): 3 liters");
        System.out.println("  - Whole Wheat Bread (P003): 2 loaves");

        Map<String, Integer> cart = new HashMap<>();
        cart.put("P001", 5);
        cart.put("P002", 3);
        cart.put("P003", 2);

        CheckoutCommand checkout = new CheckoutCommand(
            productReadRepository,
            inventoryManager,
            billWriteRepository,
            billNumberService,
            billCalculationService,
            stockSelectionStrategy,
            inStoreDiscountStrategy,
            cart,
            Bill.SaleType.IN_STORE,
            null,
            null
        );

        checkout.execute();
        Bill bill = checkout.getGeneratedBill();

        billPrinter.print(bill);
    }

    private void performOnlineTransaction() {
        System.out.println("Processing online order...");
        System.out.println("\nCustomer: Rajesh Kumar");
        System.out.println("Delivery Address: 123 MG Road, Bangalore");
        System.out.println("\nOrder Items:");
        System.out.println("  - Sunflower Oil (P004): 2 liters");
        System.out.println("  - White Sugar (P005): 10 kg");
        System.out.println("  - Basmati Rice (P001): 15 kg");

        Map<String, Integer> cart = new HashMap<>();
        cart.put("P004", 2);
        cart.put("P005", 10);
        cart.put("P001", 15);

        CheckoutCommand checkout = new CheckoutCommand(
            productReadRepository,
            inventoryManager,
            billWriteRepository,
            billNumberService,
            billCalculationService,
            stockSelectionStrategy,
            onlineDiscountStrategy,
            cart,
            Bill.SaleType.ONLINE,
            "Rajesh Kumar",
            "123 MG Road, Bangalore"
        );

        checkout.execute();
        Bill bill = checkout.getGeneratedBill();

        billPrinter.print(bill);
    }

    private void generateReports() {
        System.out.println("Generating daily sales report...\n");
        System.out.println(dailySalesReport.generateReport());

        System.out.println("\n\nGenerating stock status report...\n");
        System.out.println(stockStatusReport.generateReport());
    }
}
