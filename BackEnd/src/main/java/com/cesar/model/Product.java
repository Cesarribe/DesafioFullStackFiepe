package com.cesar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(columnNames = "normalizedName")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 100)
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String normalizedName;

    @Size(max = 300)
    private String description;

    @Min(0)
    @Max(999999)
    @Column(nullable = false)
    private int stock;

    @DecimalMin("0.01")
    @DecimalMax("1000000.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.normalizedName = normalize(this.name);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.normalizedName = normalize(this.name);
    }

    private String normalize(String original)  {
        if (original == null) return null;
        return original
                .toLowerCase()
                .replaceAll("[\\s]+", " ")
                .trim()
                .replaceAll("[áàâãä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[óòôõö]", "o")
                .replaceAll("[úùûü]", "u")
                .replaceAll("[ç]", "c");
    } // evitar problemas com acentuações e duplicidades, garantir um BD com unicidade.
}
