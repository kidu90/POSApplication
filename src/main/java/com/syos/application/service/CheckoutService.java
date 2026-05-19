package com.syos.application.service;

import java.util.Map;

import com.syos.application.strategy.DiscountStrategy;
import com.syos.application.strategy.StockSelectionStrategy;
import com.syos.application.usecase.CheckoutCommand;
import com.syos.domain.entity.Bill;
import com.syos.domain.repository.BillWriteRepository;
import com.syos.domain.repository.ProductReadRepository;

public class CheckoutService {
    private final ProductReadRepository productRepository;
    private final InventoryManager inventoryManager;
    private final BillWriteRepository billRepository;
    private final BillNumberService billNumberService;
    private final BillCalculationService billCalculationService;

    public CheckoutService(ProductReadRepository productRepository,
                           InventoryManager inventoryManager,
                           BillWriteRepository billRepository,
                           BillNumberService billNumberService,
                           BillCalculationService billCalculationService) {
        this.productRepository = productRepository;
        this.inventoryManager = inventoryManager;
        this.billRepository = billRepository;
        this.billNumberService = billNumberService;
        this.billCalculationService = billCalculationService;
    }

    public Bill checkout(Map<String, Integer> cartItems,
                         Bill.SaleType saleType,
                         DiscountStrategy discountStrategy,
                         StockSelectionStrategy stockSelectionStrategy,
                         String customerName,
                         String customerAddress) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty");
        }

        CheckoutCommand command = new CheckoutCommand(
            productRepository,
            inventoryManager,
            billRepository,
            billNumberService,
            billCalculationService,
            stockSelectionStrategy,
            discountStrategy,
            cartItems,
            saleType,
            customerName,
            customerAddress
        );

        command.execute();
        return command.getGeneratedBill();
    }
}