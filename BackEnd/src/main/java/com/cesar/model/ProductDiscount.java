package com.cesar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_discounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Associação com Produto
    @OneToOne
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    // Associação com cupom promocional (opcional)
    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // PERCENT ou COUPON

    // Valor percentual (1% a 80%) se for desconto direto
    @DecimalMin("1.0")
    @DecimalMax("80.0")
    private Double percentValue;

    @Column(nullable = false)
    private LocalDateTime appliedAt;
}

