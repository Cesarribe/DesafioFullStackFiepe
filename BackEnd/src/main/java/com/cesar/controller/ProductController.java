package com.cesar.controller;

import com.cesar.dto.ProductRequestDTO;
import com.cesar.dto.ProductResponseDTO;
import com.cesar.model.Product;
import com.cesar.model.ProductDiscount;
import com.cesar.repository.ProductDiscountRepository;
import com.cesar.service.ProductService;
import com.cesar.util.ProductConverter;
import com.github.fge.jsonpatch.JsonPatch;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;
    private final ProductDiscountRepository discountRepository;

    public ProductController(ProductService service, ProductDiscountRepository discountRepository) {
        this.service = service;
        this.discountRepository = discountRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> listarTodos() {
        List<Product> produtosAtivos = service.listarTodos()
                .stream()
                .filter(p -> !p.isDeleted())
                .toList();

        List<ProductResponseDTO> dtos = produtosAtivos.stream()
                .map(produto -> {
                    ProductDiscount desconto = discountRepository.findByProduct(produto).orElse(null);
                    return ProductConverter.toDTO(produto, desconto);
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> buscarPorId(@PathVariable Long id) {
        Product produto = service.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        ProductDiscount desconto = discountRepository.findByProduct(produto).orElse(null);
        ProductResponseDTO dto = ProductConverter.toDTO(produto, desconto);

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Product> criar(@RequestBody @Valid ProductRequestDTO dto) {
        Product produto = new Product();
        produto.setName(dto.getName());
        produto.setDescription(dto.getDescription());
        produto.setPrice(dto.getPrice());
        produto.setStock(dto.getStock());
        return ResponseEntity.ok(service.salvar(produto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> atualizar(@PathVariable Long id, @RequestBody @Valid ProductRequestDTO dto) {
        Product atualizacao = new Product();
        atualizacao.setName(dto.getName());
        atualizacao.setDescription(dto.getDescription());
        atualizacao.setPrice(dto.getPrice());
        atualizacao.setStock(dto.getStock());
        return ResponseEntity.ok(service.atualizar(id, atualizacao));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Product> patchProduto(@PathVariable Long id, @RequestBody JsonPatch patch) {
        try {
            Product original = service.buscarPorId(id).orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            Product atualizado = service.aplicarPatch(original, patch);
            return ResponseEntity.ok(service.atualizar(id, atualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Product> restaurar(@PathVariable Long id) {
        return ResponseEntity.ok(service.restaurar(id));
    }

    @PostMapping("/{id}/discount/percent")
    public ResponseEntity<Product> aplicarDescontoPercentual(@PathVariable Long id, @RequestParam("percent") double percent) {
        return ResponseEntity.ok(service.aplicarDescontoPercentual(id, percent));
    }

    @PostMapping("/{id}/discount/coupon")
    public ResponseEntity<Product> aplicarCupom(@PathVariable Long id, @RequestParam("code") String codigoCupom) {
        return ResponseEntity.ok(service.aplicarCupom(id, codigoCupom));
    }

    @DeleteMapping("/{id}/discount")
    public ResponseEntity<Void> removerDesconto(@PathVariable Long id) {
        service.removerDesconto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<?> listarComFiltros(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean hasDiscount,
            @RequestParam(required = false) Boolean includeDeleted,
            @RequestParam(required = false) Boolean onlyOutOfStock,
            @RequestParam(required = false) Boolean withCouponApplied,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        var pagina = service.listarComFiltros(
                page, limit, search, minPrice, maxPrice, hasDiscount,
                includeDeleted, onlyOutOfStock, withCouponApplied,
                sortBy, sortOrder
        ).map(p -> {
            ProductDiscount desconto = discountRepository.findByProduct(p).orElse(null);
            return ProductConverter.toDTO(p, desconto);
        });

        return ResponseEntity.ok(pagina);
    }

}
