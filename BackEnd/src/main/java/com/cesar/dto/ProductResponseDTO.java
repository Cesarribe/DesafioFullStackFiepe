package com.cesar.dto;

import com.cesar.model.DiscountType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal priceWithDiscount;
    private Integer stock;
    private boolean deleted;
    private boolean hasDiscount;
    private DiscountType discountType;
    private Double discountPercent;
    private String couponCode;
    private List<String> badges;
}

