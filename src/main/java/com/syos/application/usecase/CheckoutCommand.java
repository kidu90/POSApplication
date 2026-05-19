package com.syos.application.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.syos.application.builder.BillBuilder;
import com.syos.application.service.BillCalculationService;
import com.syos.application.service.BillNumberService;
import com.syos.application.service.InventoryManager;
import com.syos.application.strategy.DiscountStrategy;
import com.syos.application.strategy.StockSelectionStrategy;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.domain.entity.Product;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.BillWriteRepository;
import com.syos.domain.repository.ProductReadRepository;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.ProductId;

public class CheckoutCommand implements Command {
    private final ProductReadRepository productRepository;
    private final InventoryManager inventoryManager;
    private final BillWriteRepository billRepository;
    private final BillNumberService billNumberService;
    private final BillCalculationService billCalculationService;
    private final StockSelectionStrategy stockSelectionStrategy;
    private final DiscountStrategy discountStrategy;
    private final Map<String, Integer> cartItems;
    private final Bill.SaleType saleType;
    private final String customerName;
    private final String customerAddress;
    private Bill generatedBill;

    public CheckoutCommand(ProductReadRepository productRepository,
                          InventoryManager inventoryManager,
                          BillWriteRepository billRepository,
                          BillNumberService billNumberService,
                          BillCalculationService billCalculationService,
                          StockSelectionStrategy stockSelectionStrategy,
                          DiscountStrategy discountStrategy,
                          Map<String, Integer> cartItems,
                          Bill.SaleType saleType,
                          String customerName,
                          String customerAddress) {
        this.productRepository = productRepository;
        this.inventoryManager = inventoryManager;
        this.billRepository = billRepository;
        this.billNumberService = billNumberService;
        this.billCalculationService = billCalculationService;
        this.stockSelectionStrategy = stockSelectionStrategy;
        this.discountStrategy = discountStrategy;
        this.cartItems = new HashMap<>(cartItems);
        this.saleType = saleType;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
    }

    @Override
    public void execute() {
        String prefix = saleType == Bill.SaleType.ONLINE ? "ONL" : "POS";
        String billNumber = billNumberService.generateBillNumber(prefix);

        BillBuilder builder = new BillBuilder()
            .withBillNumber(billNumber)
            .withSaleType(saleType);

        if (saleType == Bill.SaleType.ONLINE) {
            builder.forOnline(customerName, customerAddress);
        }

        Bill bill = builder.build();

        for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
            String productIdStr = entry.getKey();
            int quantity = entry.getValue();

            ProductId productId = new ProductId(productIdStr);
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productIdStr));

            InventoryChannel channel = saleType == Bill.SaleType.ONLINE ? InventoryChannel.ONLINE : InventoryChannel.STORE;
            List<StockBatch> availableBatches = inventoryManager.getAvailableBatches(productId, channel);
            List<StockBatch> selectedBatches = stockSelectionStrategy.selectBatches(availableBatches, quantity);

            int remainingQuantity = quantity;
            for (StockBatch batch : selectedBatches) {
                int quantityToTake = Math.min(remainingQuantity, batch.getQuantity());
                
                BillItem item = new BillItem(
                    productId,
                    product.getName(),
                    quantityToTake,
                    product.getUnitPrice(),
                    batch.getBatchNumber()
                );
                
                bill.addItem(item);
                batch.reduceQuantity(quantityToTake);
                inventoryManager.updateStock(batch);
                
                remainingQuantity -= quantityToTake;
            }
        }

        billCalculationService.applyDiscounts(bill, discountStrategy);

        billRepository.save(bill);
        this.generatedBill = bill;
    }

    public Bill getGeneratedBill() {
        return generatedBill;
    }
}
