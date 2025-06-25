package com.cesar;

import com.cesar.model.*;
import com.cesar.repository.*;
import com.cesar.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductDiscountRepository discountRepository;

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void deveAplicarDescontoPercentualComSucesso() {
        // Arrange
        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Notebook");
        produto.setPrice(BigDecimal.valueOf(1000));

        when(productRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(discountRepository.existsByProduct(produto)).thenReturn(false);

        // Act
        Product resultado = productService.aplicarDescontoPercentual(1L, 20);

        // Assert
        assertEquals("Notebook", resultado.getName());
        verify(discountRepository).save(any(ProductDiscount.class));
    }

    @Test
    void deveLancarExcecaoParaPercentualInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            productService.aplicarDescontoPercentual(1L, 0.5); // menor que 1%
        });

        assertThrows(IllegalArgumentException.class, () -> {
            productService.aplicarDescontoPercentual(1L, 85); // maior que 80%
        });
    }

    @Test
    void deveLancarExcecaoSeProdutoNaoExiste() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            productService.aplicarDescontoPercentual(42L, 10);
        });

        assertEquals("Produto não encontrado.", e.getMessage());
    }
}
