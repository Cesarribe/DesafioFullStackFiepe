package com.cesar;

import com.cesar.controller.ProductController;
import com.cesar.dto.ProductRequestDTO;
import com.cesar.exception.NotFoundException;
import com.cesar.model.Product;
import com.cesar.model.ProductDiscount;
import com.cesar.service.ProductService;
import com.cesar.repository.ProductDiscountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.fge.jsonpatch.JsonPatch;
import org.junit.jupiter.api.Disabled;
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
import static org.mockito.Mockito.verify;
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
    @Test
    void deveRetornarProdutoPorIdComSucesso() throws Exception {
        Long id = 1L;

        Product produto = new Product();
        produto.setId(id);
        produto.setName("Mouse Gamer");
        produto.setDescription("Mouse RGB com 6 botões");
        produto.setPrice(BigDecimal.valueOf(120));
        produto.setStock(12);

        // Simula produto encontrado no service
        when(productService.buscarPorIdOuErro(id)).thenReturn(produto);
        // Simula ausência de desconto (é necessário?)
        when(discountRepository.findByProduct(produto)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mouse Gamer"))
                .andExpect(jsonPath("$.price").value(120));
    }
    @Test
    void deveRetornarErroQuandoProdutoNaoEncontrado() throws Exception {
        Long idInexistente = 999L;

        when(productService.buscarPorIdOuErro(idInexistente))
                .thenThrow(new NotFoundException("Produto não encontrado"));

        mockMvc.perform(get("/products/{id}", idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Produto não encontrado"));
    }
    @Test
    void deveRetornar404NoPatchQuandoProdutoNaoExiste() throws Exception {
        Long idInexistente = 888L;

        JsonPatch patch = JsonPatch.fromJson(
                new ObjectMapper().readTree("""
            [
              { "op": "replace", "path": "/name", "value": "Novo nome" }
            ]
        """)
        );

        when(productService.buscarPorIdOuErro(idInexistente))
                .thenThrow(new NotFoundException("Produto não encontrado"));

        mockMvc.perform(patch("/products/{id}", idInexistente)
                        .contentType("application/json-patch+json")
                        .content(new ObjectMapper().writeValueAsBytes(patch)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Produto não encontrado"));
    }
    @Test
    void deveInativarProdutoComSucesso() throws Exception {
        Long id = 5L;

        // Aqui não precisa de when(...), pois o método da service é void
        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNoContent());

        verify(productService).inativar(id);
    }
    @Test
    void deveRestaurarProdutoComSucesso() throws Exception {
        Long id = 10L;

        Product produtoRestaurado = new Product();
        produtoRestaurado.setId(id);
        produtoRestaurado.setName("Caneta Azul");

        when(productService.restaurar(id)).thenReturn(produtoRestaurado);

        mockMvc.perform(post("/products/{id}/restore", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Caneta Azul"));
    }
    @Test
    void deveRetornar404AoRestaurarProdutoInexistente() throws Exception {
        Long idInexistente = 999L;

        when(productService.restaurar(idInexistente))
                .thenThrow(new NotFoundException("Produto não encontrado"));

        mockMvc.perform(post("/products/{id}/restore", idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Produto não encontrado"));
    }
    @Test
    void deveAplicarCupomComSucesso() throws Exception {
        Long id = 1L;

        Product produtoComDesconto = new Product();
        produtoComDesconto.setId(id);
        produtoComDesconto.setName("Notebook");
        produtoComDesconto.setPrice(BigDecimal.valueOf(3000));

        when(productService.aplicarCupom(id, "PROMO10")).thenReturn(produtoComDesconto);

        mockMvc.perform(post("/products/{id}/discount/coupon", id)
                        .param("code", "PROMO10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook"))
                .andExpect(jsonPath("$.price").value(3000));
    }
    @Test
    void deveRetornar404AoAplicarCupomEmProdutoInexistente() throws Exception {
        Long idInexistente = 999L;

        when(productService.aplicarCupom(idInexistente, "PROMO10"))
                .thenThrow(new NotFoundException("Produto não encontrado"));

        mockMvc.perform(post("/products/{id}/discount/coupon", idInexistente)
                        .param("code", "PROMO10"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Produto não encontrado"));
    }
    @Disabled //("erro 505 ainda nao corrijido)
    @Test
    void deveAplicarPatchComSucesso() throws Exception {
        Long id = 1L;

        Product original = new Product();
        original.setId(id);
        original.setName("Notebook");
        original.setDescription("Antiga desc");
        original.setPrice(BigDecimal.valueOf(2000));

        Product atualizado = new Product();
        atualizado.setId(id);
        atualizado.setName("Notebook Gamer");
        atualizado.setDescription("Antiga desc");
        atualizado.setPrice(BigDecimal.valueOf(2000));

        JsonPatch patch = JsonPatch.fromJson(
                new ObjectMapper().readTree("""
            [
              { "op": "replace", "path": "/name", "value": "Notebook Gamer" }
            ]
        """)
        );

        when(productService.buscarPorIdOuErro(id)).thenReturn(original);
        when(productService.aplicarPatch(original, patch)).thenReturn(atualizado);
        when(productService.atualizar(id, atualizado)).thenReturn(atualizado);
        when(discountRepository.findByProduct(atualizado)).thenReturn(Optional.empty());



        mockMvc.perform(patch("/products/{id}", id)
                        .contentType("application/json-patch+json")
                        .accept("application/json")
                        .content(new ObjectMapper().writeValueAsBytes(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook Gamer"));

    }
    @Test
    void deveFiltrarProdutosComDesconto() throws Exception {
        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Cafeteira");
        produto.setPrice(BigDecimal.valueOf(150));

        when(productService.listarComFiltros(
                anyInt(), anyInt(),
                any(), any(), any(),
                eq(true), any(), any(), any(),
                any(), any()
        )).thenReturn(new PageImpl<>(List.of(produto)));

        when(discountRepository.findByProduct(produto)).thenReturn(Optional.of(new ProductDiscount()));

        mockMvc.perform(get("/products/filter")
                        .param("hasDiscount", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Cafeteira"));
    }
    @Test
    void deveRetornar400QuandoDescontoDoCupomEhMaiorQueValorDoProduto() throws Exception {
        Long id = 3L;

        when(productService.aplicarCupom(id, "EXAGGERADO50"))
                .thenThrow(new IllegalArgumentException("Desconto maior que valor do produto"));

        mockMvc.perform(post("/products/{id}/discount/coupon", id)
                        .param("code", "EXAGGERADO50"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Desconto maior que valor do produto"));
    }
    @Test
    void deveRetornar404AoAtualizarProdutoInexistente() throws Exception {
        Long id = 42L;

        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Produto Novo");
        dto.setDescription("Descrição");
        dto.setPrice(BigDecimal.valueOf(100));
        dto.setStock(5);

        Product atualizacao = new Product();
        atualizacao.setName(dto.getName());
        atualizacao.setDescription(dto.getDescription());
        atualizacao.setPrice(dto.getPrice());
        atualizacao.setStock(dto.getStock());

        when(productService.atualizar(eq(id), any())).thenThrow(new NotFoundException("Produto não encontrado"));

        mockMvc.perform(put("/products/{id}", id)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Produto não encontrado"));
    }

}
