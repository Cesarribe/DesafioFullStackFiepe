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
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
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

        when(productService.salvar(any())).thenReturn(produtoSalvo);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Camiseta"))
                .andExpect(jsonPath("$.price").value(50));
    }

    @Test
    void deveAplicarDescontoPercentualComSucesso() throws Exception {
        Long idProduto = 1L;

        Product produto = new Product();
        produto.setId(idProduto);
        produto.setName("Tênis");
        produto.setPrice(BigDecimal.valueOf(200));

        when(productService.aplicarDescontoPercentual(idProduto, 10)).thenReturn(produto);

        mockMvc.perform(post("/products/{id}/discount/percent", idProduto)
                        .param("percent", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tênis"))
                .andExpect(jsonPath("$.price").value(200));
    }
    @Test
    void deveRemoverDescontoComSucesso() throws Exception {
        Long idProduto = 1L;

        mockMvc.perform(delete("/products/{id}/discount", idProduto))
                .andExpect(status().isNoContent());
    }
    @Test
    void deveRetornarProdutosComFiltroDeBusca() throws Exception {
        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Caneca Nerd");
        produto.setDescription("Caneca preta com estampa de código");
        produto.setPrice(BigDecimal.valueOf(39.99));
        produto.setStock(8);

        when(productService.listarComFiltros(
                anyInt(), anyInt(),
                anyString(), any(), any(),
                any(), any(), any(), any(),
                anyString(), anyString()
        )).thenReturn(new PageImpl<>(List.of(produto)));

        when(discountRepository.findByProduct(produto)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/filter")
                        .param("search", "caneca")
                        .param("page", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Caneca Nerd"));
    }

}
