package com.syos.application.usecase;

import com.syos.domain.entity.Product;
import com.syos.domain.repository.ProductWriteRepository;

public class AddProductUseCase {
    private final ProductWriteRepository productRepository;

    public AddProductUseCase(ProductWriteRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void execute(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        productRepository.save(product);
    }
}
