package com.syos.unit.domain.valueobject;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.syos.domain.valueobject.Money;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithValidAmount() {
        Money money = new Money(100.50);
        assertEquals(0, money.getAmount().compareTo(BigDecimal.valueOf(100.50)));
    }

    @Test
    void shouldThrowExceptionForNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> new Money(-10.0));
    }

    @Test
    void shouldAddMoneyCorrectly() {
        Money money1 = new Money(100.00);
        Money money2 = new Money(50.00);
        Money result = money1.add(money2);

        assertEquals(0, result.getAmount().compareTo(BigDecimal.valueOf(150.00)));
    }

    @Test
    void shouldSubtractMoneyCorrectly() {
        Money money1 = new Money(100.00);
        Money money2 = new Money(30.00);
        Money result = money1.subtract(money2);

        assertEquals(0, result.getAmount().compareTo(BigDecimal.valueOf(70.00)));
    }

    @Test
    void shouldMultiplyByQuantity() {
        Money money = new Money(50.00);
        Money result = money.multiply(3);

        assertEquals(0, result.getAmount().compareTo(BigDecimal.valueOf(150.00)));
    }

    @Test
    void shouldApplyDiscountCorrectly() {
        Money money = new Money(100.00);
        Money result = money.applyDiscount(BigDecimal.valueOf(10));

        assertEquals(0, result.getAmount().compareTo(BigDecimal.valueOf(90.00)));
    }

    @Test
    void shouldCompareMoneyCorrectly() {
        Money money1 = new Money(100.00);
        Money money2 = new Money(50.00);

        assertTrue(money1.isGreaterThan(money2));
        assertTrue(money2.isLessThan(money1));
    }

    @Test
    void shouldCheckEqualityCorrectly() {
        Money money1 = new Money(100.00);
        Money money2 = new Money(100.00);
        Money money3 = new Money(50.00);

        assertEquals(money1, money2);
        assertNotEquals(money1, money3);
    }
}
