package com.cesar.repository;


import com.cesar.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByNormalizedName(String normalizedName);

    boolean existsByNormalizedName(String normalizedName);
}

