package com.syos.domain.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import com.syos.domain.valueobject.BatchNumber;
import com.syos.domain.valueobject.InventoryChannel;
import com.syos.domain.valueobject.ProductId;

public class StockBatch implements Serializable {
    private static final long serialVersionUID = 1L;

    private final BatchNumber batchNumber;
    private final ProductId productId;
    private final InventoryChannel inventoryChannel;
    private int quantity;
    private final LocalDate expiryDate;
    private final LocalDate receivedDate;

    public StockBatch(BatchNumber batchNumber, ProductId productId, InventoryChannel inventoryChannel, int quantity,
                      LocalDate expiryDate, LocalDate receivedDate) {
        if (inventoryChannel == null) {
            throw new IllegalArgumentException("Inventory channel cannot be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date cannot be null");
        }
        if (receivedDate == null) {
            throw new IllegalArgumentException("Received date cannot be null");
        }

        this.batchNumber = batchNumber;
        this.productId = productId;
        this.inventoryChannel = inventoryChannel;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.receivedDate = receivedDate;
    }

    public void reduceQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to reduce cannot be negative");
        }
        if (amount > quantity) {
            throw new IllegalArgumentException("Insufficient stock in batch");
        }
        this.quantity -= amount;
    }

    public void addQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to add cannot be negative");
        }
        this.quantity += amount;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isExpiringSoon(int daysThreshold) {
        return LocalDate.now().plusDays(daysThreshold).isAfter(expiryDate) && !isExpired();
    }

    public boolean hasStock() {
        return quantity > 0;
    }

    public BatchNumber getBatchNumber() {
        return batchNumber;
    }

    public ProductId getProductId() {
        return productId;
    }

    public InventoryChannel getInventoryChannel() {
        return inventoryChannel;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockBatch that = (StockBatch) o;
        return batchNumber.equals(that.batchNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(batchNumber);
    }
}
