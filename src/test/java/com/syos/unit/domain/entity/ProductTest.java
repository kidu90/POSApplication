package com.syos.unit.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.syos.domain.entity.Product;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;

class ProductTest {

    @Test
    void shouldCreateProductWithValidData() {
        Product product = new Product(
            new ProductId("P001"),
            "Rice",
            "Grains",
            new Money(85.00),
            "kg"
        );

        assertEquals("P001", product.getId().getValue());
        assertEquals("Rice", product.getName());
        assertEquals("Grains", product.getCategory());
        assertEquals(new Money(85.00), product.getUnitPrice());
        assertEquals("kg", product.getUnit());
    }

    @Test
    void shouldThrowExceptionForNullName() {
        assertThrows(IllegalArgumentException.class, () ->
            new Product(
                new ProductId("P001"),
                null,
                "Grains",
                new Money(85.00),
                "kg"
            )
        );
    }

    @Test
    void shouldThrowExceptionForEmptyName() {
        assertThrows(IllegalArgumentException.class, () ->
            new Product(
                new ProductId("P001"),
                "",
                "Grains",
                new Money(85.00),
                "kg"
            )
        );
    }

    @Test
    void shouldCheckEqualityBasedOnId() {
        Product product1 = new Product(
            new ProductId("P001"),
            "Rice",
            "Grains",
            new Money(85.00),
            "kg"
        );

        Product product2 = new Product(
            new ProductId("P001"),
            "Different Name",
            "Different Category",
            new Money(100.00),
            "liter"
        );

        assertEquals(product1, product2);
    }
}
