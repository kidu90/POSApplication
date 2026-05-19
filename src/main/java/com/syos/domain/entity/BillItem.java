package com.syos.domain.entity;

import java.io.Serializable;
import java.util.Objects;

import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;

public class BillItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ProductId productId;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;
    private final BatchNumber batchNumber;
    private Money lineTotal;
    private Money discount;

    public BillItem(ProductId productId, String productName, int quantity,
                    Money unitPrice, BatchNumber batchNumber) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.batchNumber = batchNumber;
        this.lineTotal = unitPrice.multiply(quantity);
        this.discount = new Money(0);
    }

    public void applyDiscount(Money discountAmount) {
        if (discountAmount == null) {
            throw new IllegalArgumentException("Discount amount cannot be null");
        }
        this.discount = discountAmount;
        this.lineTotal = unitPrice.multiply(quantity).subtract(discountAmount);
    }

    public ProductId getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public BatchNumber getBatchNumber() {
        return batchNumber;
    }

    public Money getLineTotal() {
        return lineTotal;
    }

    public Money getDiscount() {
        return discount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillItem billItem = (BillItem) o;
        return productId.equals(billItem.productId) && batchNumber.equals(billItem.batchNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, batchNumber);
    }
}
