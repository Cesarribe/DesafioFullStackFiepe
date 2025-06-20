package com.cesar.repository;

import com.cesar.model.Product;

import com.cesar.model.ProductDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, Long> {

    Optional<ProductDiscount> findByProduct(Product product);

    boolean existsByProduct(Product product);

    void deleteByProduct(Product product);
}
