package com.cesar.util;

import com.cesar.dto.ProductResponseDTO;
import com.cesar.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductConverter {

    public static ProductResponseDTO toDTO(Product produto, ProductDiscount desconto) {
        BigDecimal precoOriginal = produto.getPrice();
        BigDecimal precoComDesconto = precoOriginal;
        boolean temDesconto = desconto != null;
        DiscountType tipo = null;
        Double percentual = null;
        String codigoCupom = null;

        if (temDesconto && desconto.getDiscountType() != null) {
            tipo = desconto.getDiscountType();

            switch (tipo) {
                case PERCENT:
                    percentual = desconto.getPercentValue();
                    precoComDesconto = precoOriginal.subtract(
                            precoOriginal.multiply(BigDecimal.valueOf(percentual)).divide(BigDecimal.valueOf(100))
                    );
                    break;

                case COUPON:
                    Coupon cupom = desconto.getCoupon();
                    if (cupom != null) {
                        codigoCupom = cupom.getCode();
                        if (cupom.getType() == CouponType.PERCENT) {
                            precoComDesconto = precoOriginal.subtract(
                                    precoOriginal.multiply(cupom.getValue()).divide(BigDecimal.valueOf(100))
                            );
                        } else if (cupom.getType() == CouponType.FIXED) {
                            precoComDesconto = precoOriginal.subtract(cupom.getValue());
                        }
                    }
                    break;
            }
        }
        List<String> badges = new ArrayList<>();
        if (produto.isDeleted()) {
            badges.add("inativo");
        }
        if (produto.getStock() == 0) {
            badges.add("esgotado");
        }
        if (temDesconto) {
            badges.add("com desconto");
        }


        return ProductResponseDTO.builder()
                .id(produto.getId())
                .name(produto.getName())
                .description(produto.getDescription())
                .price(precoOriginal)
                .priceWithDiscount(precoComDesconto)
                .stock(produto.getStock())
                .deleted(produto.isDeleted())
                .hasDiscount(temDesconto)
                .discountType(tipo)
                .discountPercent(percentual)
                .couponCode(codigoCupom)
                .badges(badges)
                .build();
    }
}

