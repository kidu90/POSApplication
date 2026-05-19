package com.syos.presentation.gui;

import java.util.LinkedHashMap;
import java.util.Map;

import com.syos.domain.valueobject.Money;

public class CartLogic {
    private final Map<String, CartItem> items = new LinkedHashMap<>();

    public void addItem(String productId, String productName, Money unitPrice, int quantity) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        CartItem existing = items.get(productId);
        if (existing == null) {
            items.put(productId, new CartItem(productId, productName, unitPrice, quantity));
        } else {
            existing.quantity += quantity;
        }
    }

    public boolean removeItem(String productId) {
        return items.remove(productId) != null;
    }

    public Money getTotal() {
        Money total = new Money(0);
        for (CartItem item : items.values()) {
            total = total.add(item.unitPrice.multiply(item.quantity));
        }
        return total;
    }

    public int getQuantity(String productId) {
        CartItem item = items.get(productId);
        return item == null ? 0 : item.quantity;
    }

    private static final class CartItem {
        private final String productId;
        private final String productName;
        private final Money unitPrice;
        private int quantity;

        private CartItem(String productId, String productName, Money unitPrice, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }
    }
}
