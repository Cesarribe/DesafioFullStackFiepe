package com.cesar.controller;

import com.cesar.dto.ProductResponseDTO;
import com.cesar.model.Product;
import com.cesar.model.ProductDiscount;
import com.cesar.repository.ProductDiscountRepository;
import com.cesar.service.ProductService;
import com.cesar.util.ProductConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final ProductDiscountRepository discountRepository;


    // GET /products – Listagem simples
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> listarTodos() {
        List<Product> produtos = service.listarTodos();

        List<ProductResponseDTO> dtos = produtos.stream()
                .map(produto -> {
                    ProductDiscount desconto = discountRepository.findByProduct(produto).orElse(null);
                    return ProductConverter.toDTO(produto, desconto);
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // GET /products/{id} – Detalhes de um produto
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> buscarPorId(@PathVariable Long id) {
        Product produto = service.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        ProductDiscount desconto = discountRepository.findByProduct(produto).orElse(null);
        ProductResponseDTO dto = ProductConverter.toDTO(produto, desconto);

        return ResponseEntity.ok(dto);
    }

    // POST /products – Criação
    @PostMapping
    public ResponseEntity<Product> criar(@Valid @RequestBody Product produto) {
        Product criado = service.salvar(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    // PATCH /products/{id} – Atualização parcial
    @PatchMapping("/{id}")
    public ResponseEntity<Void> patch(@PathVariable Long id, @RequestBody String patch) {
        // Placeholder: vamos implementar com JSON Patch depois
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    // DELETE /products/{id} – Inativa produto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    // POST /products/{id}/restore – Restaura produto inativo
    @PostMapping("/{id}/restore")
    public ResponseEntity<Product> restaurar(@PathVariable Long id) {
        Product restaurado = service.restaurar(id);
        return ResponseEntity.ok(restaurado);
    }

    // POST /products/{id}/discount/percent – Aplica desconto percentual
    @PostMapping("/{id}/discount/percent")
    public ResponseEntity<Product> aplicarDescontoPercentual(
            @PathVariable Long id,
            @RequestParam("percent") double percent
    ) {
        Product atualizado = service.aplicarDescontoPercentual(id, percent);
        return ResponseEntity.ok(atualizado);
    }

    // POST /products/{id}/discount/coupon – Aplica cupom promocional
    @PostMapping("/{id}/discount/coupon")
    public ResponseEntity<Product> aplicarCupom(
            @PathVariable Long id,
            @RequestParam("code") String codigoCupom
    ) {
        Product atualizado = service.aplicarCupom(id, codigoCupom);
        return ResponseEntity.ok(atualizado);
    }

    // DELETE /products/{id}/discount – Remove desconto
    @DeleteMapping("/{id}/discount")
    public ResponseEntity<Void> removerDesconto(@PathVariable Long id) {
        service.removerDesconto(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> listarComFiltros(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "false") boolean withDiscount,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "false") boolean includeOutOfStock
    ) {
        Page<Product> produtos = service.listarComFiltros(page, size, sort, withDiscount, includeInactive, includeOutOfStock);

        Page<ProductResponseDTO> dtoPage = produtos.map(produto -> {
            ProductDiscount desconto = discountRepository.findByProduct(produto).orElse(null);
            return ProductConverter.toDTO(produto, desconto);
        });

        return ResponseEntity.ok(dtoPage);
    }

}
