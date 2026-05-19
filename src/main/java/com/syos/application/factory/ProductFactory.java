package com.syos.application.factory;

import com.syos.domain.entity.Product;
import com.syos.domain.valueobject.Money;
import com.syos.domain.valueobject.ProductId;

public class ProductFactory {
    public Product createProduct(String id, String name, String category, double price, String unit) {
        ProductId productId = new ProductId(id);
        Money unitPrice = new Money(price);
        return new Product(productId, name, category, unitPrice, unit);
    }

    public Product createProduct(String id, String name, String category, Money price, String unit) {
        ProductId productId = new ProductId(id);
        return new Product(productId, name, category, price, unit);
    }
}
