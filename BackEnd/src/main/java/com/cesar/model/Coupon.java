package com.cesar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons", uniqueConstraints = {
        @UniqueConstraint(columnNames = "normalizedCode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 4, max = 20)
    @Column(nullable = false)
    private String code;

    @Column(nullable = false, unique = true)
    private String normalizedCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type; // FIXED ou PERCENT

    @DecimalMin("0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(nullable = false)
    private boolean oneShot;

    @NotNull
    private LocalDateTime validFrom;

    @NotNull
    private LocalDateTime validUntil;


    @PrePersist
    @PreUpdate
    private void normalize() {
        this.normalizedCode = normalizeCode(this.code);
    }

    private String normalizeCode(String original) {
        if (original == null) return null;
        return original
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "")
                .replaceAll("[áàâãä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[óòôõö]", "o")
                .replaceAll("[úùûü]", "u")
                .replaceAll("ç", "c");
    }

    public boolean isValidoAgora() {
        LocalDateTime agora = LocalDateTime.now();
        return agora.isAfter(validFrom) && agora.isBefore(validUntil);
    }
}

