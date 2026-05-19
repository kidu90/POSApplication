package com.syos.domain.repository;

import com.syos.domain.entity.Product;
import com.syos.domain.valueobject.ProductId;

public interface ProductWriteRepository {
    void save(Product product);
    void delete(ProductId id);
}
