package com.ingemark.product_app.repository;

import com.ingemark.product_app.model.Product;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);

    @Query("SELECT p FROM Product p WHERE (:isAvailable is null or p.isAvailable = :isAvailable)")
    List<Product> findByIsAvailable(Boolean isAvailable, Limit limit);
}
