package com.syos.application.strategy;

import com.syos.domain.entity.Bill;
import com.syos.domain.valueobject.Money;

public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public Money calculateDiscount(Bill bill) {
        return new Money(0);
    }

    @Override
    public String getDescription() {
        return "No discount applied";
    }
}
