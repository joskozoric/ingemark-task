package com.ingemark.product_app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 10)
    @NotNull(message = "Code is mandatory")
    @Size(min = 10, max = 10, message = "Code must be exactly 10 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]{10}$", message = "Code must be alphanumeric and exactly 10 characters")
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    @NotNull(message = "Name is mandatory")
    private String name;

    @Column(name = "price_eur", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price (EUR) is mandatory")
    @DecimalMin(value = "0.00", inclusive = true, message = "Price (EUR) must be non-negative")
    private BigDecimal priceEur;

    @Transient
    private BigDecimal priceUsd;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Product() {}
}