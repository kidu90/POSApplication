package com.syos.domain.entity;

import java.io.Serializable;
import java.util.Objects;

import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ProductId id;
    private final String name;
    private final String category;
    private final Money unitPrice;
    private final String unit;

    public Product(ProductId id, String name, String category, Money unitPrice, String unit) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Product category cannot be null or empty");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Unit cannot be null or empty");
        }

        this.id = id;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.unit = unit;
    }

    public ProductId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s per %s)", id, name, unitPrice, unit);
    }
}
