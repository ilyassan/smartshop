package com.smartshop.service.impl;

import com.smartshop.entity.Product;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.ProductRepository;
import com.smartshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }

        product.setDeleted(false);
        Product savedProduct = productRepository.save(product);
        log.info("Created new product with SKU: {}", savedProduct.getSku());

        return savedProduct;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByDeletedFalse(pageable);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existingProduct = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        if (product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }
        if (product.getUnitPrice() != null) {
            existingProduct.setUnitPrice(product.getUnitPrice());
        }
        if (product.getStock() != null) {
            existingProduct.setStock(product.getStock());
        }
        if (product.getCategory() != null) {
            existingProduct.setCategory(product.getCategory());
        }

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Updated product with id: {}", updatedProduct.getId());

        return updatedProduct;
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setDeleted(true);
        productRepository.save(product);
        log.info("Soft deleted product with id: {}", id);
    }
}
