package com.cesar;

import com.cesar.controller.ProductController;
import com.cesar.dto.ProductRequestDTO;
import com.cesar.model.Product;
import com.cesar.service.ProductService;
import com.cesar.repository.ProductDiscountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductDiscountRepository discountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveRetornarListaDeProdutos() throws Exception {
        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Café");
        produto.setDescription("Café especial");
        produto.setPrice(BigDecimal.valueOf(20));
        produto.setStock(10);
        produto.setDeleted(false);

        when(productService.listarTodos()).thenReturn(List.of(produto));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
        // Dica: aqui você pode testar o conteúdo do JSON com jsonPath se quiser
    }

    @Test
    void deveCriarProdutoComSucesso() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Camiseta");
        request.setDescription("Tecido orgânico");
        request.setPrice(BigDecimal.valueOf(50));
        request.setStock(5);

        Product produtoSalvo = new Product();
        produtoSalvo.setId(1L);
        produtoSalvo.setName("Camiseta");
        produtoSalvo.setPrice(BigDecimal.valueOf(50));

        when(productService.salvar(org.mockito.ArgumentMatchers.any())).thenReturn(produtoSalvo);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Camiseta"))
                .andExpect(jsonPath("$.price").value(50));
    }
}
