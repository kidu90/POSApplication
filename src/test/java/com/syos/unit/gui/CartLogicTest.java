package com.syos.unit.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.syos.domain.valueobject.Money;
import com.syos.presentation.gui.CartLogic;

/**
 * Verifies pure cart logic because this behavior should remain testable without opening Swing frames.
 */
class CartLogicTest {
    private CartLogic cart;

    @BeforeEach
    void setUp() {
        cart = new CartLogic();
    }

    @Test
    void shouldAccumulateQuantityForSameProductId() {
        cart.addItem("P001", "Rice", new Money(10), 1);
        cart.addItem("P001", "Rice", new Money(10), 2);

        assertEquals(3, cart.getQuantity("P001"));
    }

    @Test
    void shouldThrowWhenQuantityIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> cart.addItem("P001", "Rice", new Money(10), 0));
        assertThrows(IllegalArgumentException.class, () -> cart.addItem("P001", "Rice", new Money(10), -1));
    }

    @Test
    void shouldRemoveProductFromCart() {
        cart.addItem("P001", "Rice", new Money(10), 1);

        assertTrue(cart.removeItem("P001"));
    }

    @Test
    void shouldReturnFalseWhenRemovingMissingProduct() {
        assertFalse(cart.removeItem("P001"));
    }

    @Test
    void shouldCalculateTotalAsPriceTimesQuantity() {
        cart.addItem("P001", "Rice", new Money(10), 2);
        cart.addItem("P002", "Tea", new Money(5), 3);

        assertEquals(new Money(35), cart.getTotal());
    }
}
