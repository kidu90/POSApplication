package com.syos.server;

import java.time.LocalDate;
import java.util.Map;

import com.syos.application.factory.ProductFactory;
import com.syos.application.factory.StockBatchFactory;
import com.syos.application.report.DailySalesReport;
import com.syos.application.report.IReportGenerator;
import com.syos.application.report.StockStatusReport;
import com.syos.application.service.BillCalculationService;
import com.syos.application.service.BillNumberService;
import com.syos.application.service.CheckoutService;
import com.syos.application.service.InventoryManager;
import com.syos.application.strategy.FEFOStockSelectionStrategy;
import com.syos.application.strategy.NoDiscountStrategy;
import com.syos.application.strategy.StockSelectionStrategy;
import com.syos.application.strategy.ThresholdDiscountStrategy;
import com.syos.application.usecase.AddProductUseCase;
import com.syos.application.usecase.AddStockUseCase;
import com.syos.application.usecase.ListProductsUseCase;
import com.syos.application.usecase.ListStockBatchesUseCase;
import com.syos.application.usecase.LoginUseCase;
import com.syos.application.usecase.RegisterUseCase;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.Product;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.entity.User;
import com.syos.domain.repository.BillRepository;
import com.syos.domain.repository.ProductRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.repository.UserRepository;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.Money;
import com.syos.shared.Request;
import com.syos.shared.Response;

/**
 * Dispatches wire requests to application-layer use cases and returns serializable responses.
 */
public class ServerRequestHandler {
    private final AddProductUseCase addProductUseCase;
    private final AddStockUseCase addStockUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final ListStockBatchesUseCase listStockBatchesUseCase;
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final CheckoutService checkoutService;
    private final IReportGenerator dailySalesReport;
    private final IReportGenerator stockStatusReport;
    private final ProductFactory productFactory;
    private final StockBatchFactory stockBatchFactory;
    private final com.syos.server.push.PushNotificationService pushService;

    public ServerRequestHandler(ProductRepository productRepository,
                                StockRepository stockRepository,
                                BillRepository billRepository,
                                UserRepository userRepository,
                                ProductFactory productFactory,
                                StockBatchFactory stockBatchFactory,
                                BillNumberService billNumberService,
                                BillCalculationService billCalculationService,
                                InventoryManager inventoryManager,
                                com.syos.server.push.PushNotificationService pushService) {
        this.addProductUseCase = new AddProductUseCase(productRepository);
        this.addStockUseCase = new AddStockUseCase(stockRepository);
        this.listProductsUseCase = new ListProductsUseCase(productRepository);
        this.listStockBatchesUseCase = new ListStockBatchesUseCase(stockRepository);
        this.loginUseCase = new LoginUseCase(userRepository);
        this.registerUseCase = new RegisterUseCase(userRepository);
        this.checkoutService = new CheckoutService(productRepository, inventoryManager, billRepository, billNumberService, billCalculationService);
        this.dailySalesReport = new DailySalesReport(billRepository, LocalDate.now());
        this.stockStatusReport = new StockStatusReport(stockRepository, productRepository);
        this.productFactory = productFactory;
        this.stockBatchFactory = stockBatchFactory;
        this.pushService = pushService;
    }

    public Response handle(Request request) {
        try {
            Object data = switch (request.getAction()) {
                case "LOGIN" -> login(request.getParams());
                case "REGISTER" -> register(request.getParams());
                case "GET_PRODUCTS" -> listProductsUseCase.execute();
                case "GET_STOCK" -> listStockBatchesUseCase.execute();
                case "ADD_PRODUCT" -> {
                    addProduct(request.getParams());
                    yield null;
                }
                case "ADD_STOCK" -> {
                    addStock(request.getParams());
                    yield null;
                }
                case "CHECKOUT_INSTORE" -> checkout(request.getParams(), Bill.SaleType.IN_STORE);
                case "CHECKOUT_ONLINE" -> checkout(request.getParams(), Bill.SaleType.ONLINE);
                case "GET_DAILY_REPORT" -> dailySalesReport.generateReport();
                case "GET_STOCK_REPORT" -> stockStatusReport.generateReport();
                default -> throw new IllegalArgumentException("Unknown action: " + request.getAction());
            };
            return new Response(true, "OK", data);
        } catch (Exception ex) {
            return new Response(false, ex.getMessage(), null);
        }
    }

    private User login(Map<String, Object> params) {
        return loginUseCase.execute((String) params.get("username"), (String) params.get("password"));
    }

    private User register(Map<String, Object> params) {
        return registerUseCase.execute(
            (String) params.get("fullName"),
            (String) params.get("address"),
            (String) params.get("username"),
            (String) params.get("password")
        );
    }

    private void addProduct(Map<String, Object> params) {
        Product product = productFactory.createProduct(
            (String) params.get("id"),
            (String) params.get("name"),
            (String) params.get("category"),
            Double.parseDouble(String.valueOf(params.get("price"))),
            (String) params.get("unit")
        );
        addProductUseCase.execute(product);
        if (pushService != null) {
            pushService.broadcastProductChange();
        }
    }

    private void addStock(Map<String, Object> params) {
        StockBatch batch = stockBatchFactory.createBatch(
            (String) params.get("batchNumber"),
            (String) params.get("productId"),
            InventoryChannel.valueOf((String) params.get("channel")),
            Integer.parseInt(String.valueOf(params.get("quantity"))),
            LocalDate.parse((String) params.get("expiryDate")),
            LocalDate.parse((String) params.get("receivedDate"))
        );
        addStockUseCase.execute(batch);
        if (pushService != null) {
            pushService.broadcastStockChange();
        }
    }

    private Bill checkout(Map<String, Object> params, Bill.SaleType saleType) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> cart = (Map<String, Integer>) params.get("cart");

        StockSelectionStrategy stockSelectionStrategy = new FEFOStockSelectionStrategy();
        var discountStrategy = saleType == Bill.SaleType.ONLINE
            ? new ThresholdDiscountStrategy(new Money(1000), new Money(50))
            : new NoDiscountStrategy();

        return checkoutService.checkout(
            cart,
            saleType,
            discountStrategy,
            stockSelectionStrategy,
            (String) params.get("customerName"),
            (String) params.get("customerAddress")
        );
    }
}
