package com.syos.application.strategy;

import com.syos.domain.entity.Bill;
import com.syos.domain.valueobject.Money;

import java.math.BigDecimal;

public class PercentageDiscountStrategy implements DiscountStrategy {
    private final BigDecimal percentage;

    public PercentageDiscountStrategy(BigDecimal percentage) {
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) < 0 ||
            percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        this.percentage = percentage;
    }

    @Override
    public Money calculateDiscount(Bill bill) {
        BigDecimal discountAmount = bill.getSubtotal().getAmount()
            .multiply(percentage)
            .divide(BigDecimal.valueOf(100));
        return new Money(discountAmount);
    }

    @Override
    public String getDescription() {
        return String.format("%.2f%% discount", percentage);
    }
}
