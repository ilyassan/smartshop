package com.smartshop.repository;

import com.smartshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByDeletedFalse(Pageable pageable);

    Optional<Product> findByIdAndDeletedFalse(Long id);

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);
}
