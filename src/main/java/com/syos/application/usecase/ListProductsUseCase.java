package com.syos.application.usecase;

import java.util.List;

import com.syos.domain.entity.Product;
import com.syos.domain.repository.ProductReadRepository;

public class ListProductsUseCase {
    private final ProductReadRepository productRepository;

    public ListProductsUseCase(ProductReadRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> execute() {
        return productRepository.findAll();
    }
}