package com.syos.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;

public final class ProductId implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String value;

    public ProductId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        this.value = value.trim();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId productId = (ProductId) o;
        return value.equals(productId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
