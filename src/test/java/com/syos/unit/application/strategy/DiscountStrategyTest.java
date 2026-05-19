package com.syos.unit.application.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.syos.application.strategy.DiscountStrategy;
import com.syos.application.strategy.NoDiscountStrategy;
import com.syos.application.strategy.PercentageDiscountStrategy;
import com.syos.application.strategy.ThresholdDiscountStrategy;
import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.BillNumber;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;

class DiscountStrategyTest {

    @Test
    void noDiscountStrategyShouldReturnZero() {
        Bill bill = createSampleBill(500.00);
        DiscountStrategy strategy = new NoDiscountStrategy();

        Money discount = strategy.calculateDiscount(bill);

        assertEquals(new Money(0), discount);
    }

    @Test
    void percentageDiscountStrategyShouldCalculateCorrectly() {
        Bill bill = createSampleBill(500.00);
        DiscountStrategy strategy = new PercentageDiscountStrategy(BigDecimal.valueOf(10));

        Money discount = strategy.calculateDiscount(bill);

        assertEquals(new Money(50.00), discount);
    }

    @Test
    void thresholdDiscountStrategyShouldApplyWhenThresholdMet() {
        Bill bill = createSampleBill(1500.00);
        DiscountStrategy strategy = new ThresholdDiscountStrategy(
            new Money(1000.00),
            new Money(100.00)
        );

        Money discount = strategy.calculateDiscount(bill);

        assertEquals(new Money(100.00), discount);
    }

    @Test
    void thresholdDiscountStrategyShouldNotApplyWhenThresholdNotMet() {
        Bill bill = createSampleBill(500.00);
        DiscountStrategy strategy = new ThresholdDiscountStrategy(
            new Money(1000.00),
            new Money(100.00)
        );

        Money discount = strategy.calculateDiscount(bill);

        assertEquals(new Money(0), discount);
    }

    private Bill createSampleBill(double totalAmount) {
        Bill bill = new Bill(
            new BillNumber("TEST-001"),
            LocalDateTime.now(),
            Bill.SaleType.IN_STORE
        );

        BillItem item = new BillItem(
            new ProductId("P001"),
            "Test Product",
            1,
            new Money(totalAmount),
            new BatchNumber("B001")
        );

        bill.addItem(item);
        return bill;
    }
}
