package com.syos.domain.repository;

import java.util.List;
import java.util.Optional;

import com.syos.domain.entity.Product;
import com.syos.domain.valueobject.ProductId;

public interface ProductReadRepository {
    Optional<Product> findById(ProductId id);
    List<Product> findAll();
    List<Product> findByCategory(String category);
}
