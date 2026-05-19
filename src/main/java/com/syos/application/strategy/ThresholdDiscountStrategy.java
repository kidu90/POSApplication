package com.syos.application.strategy;

import com.syos.domain.entity.Bill;
import com.syos.domain.valueobject.Money;

public class ThresholdDiscountStrategy implements DiscountStrategy {
    private final Money threshold;
    private final Money discountAmount;

    public ThresholdDiscountStrategy(Money threshold, Money discountAmount) {
        if (threshold == null || discountAmount == null) {
            throw new IllegalArgumentException("Threshold and discount amount cannot be null");
        }
        this.threshold = threshold;
        this.discountAmount = discountAmount;
    }

    @Override
    public Money calculateDiscount(Bill bill) {
        if (bill.getSubtotal().isGreaterThan(threshold) || bill.getSubtotal().equals(threshold)) {
            return discountAmount;
        }
        return new Money(0);
    }

    @Override
    public String getDescription() {
        return String.format("Spend %s, save %s", threshold, discountAmount);
    }
}
