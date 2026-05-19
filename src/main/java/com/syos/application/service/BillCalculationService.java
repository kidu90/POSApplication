package com.syos.application.service;

import com.syos.application.strategy.DiscountStrategy;
import com.syos.domain.entity.Bill;
import com.syos.domain.valueobject.Money;

public class BillCalculationService {
    public Money applyDiscounts(Bill bill, DiscountStrategy discountStrategy) {
        Money discount = discountStrategy.calculateDiscount(bill);
        bill.applyDiscount(discount);
        return discount;
    }
}
