package com.cesar.service;

import com.cesar.model.*;
import com.cesar.repository.CouponRepository;
import com.cesar.repository.ProductDiscountRepository;
import com.cesar.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private ProductDiscountRepository discountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Product> listarTodos() {
        return repository.findAll();
    }

    public Optional<Product> buscarPorId(Long id) {
        return repository.findById(id);
    }

    private boolean temDescontoAtivo(Product produto) {
        return discountRepository.existsByProduct(produto);
    }

    // CRUD
    public Product salvar(Product produto) {
        if (repository.existsByNormalizedName(produto.getNormalizedName())) {
            throw new RuntimeException("Nome de produto já cadastrado.");
        }
        return repository.save(produto);
    }

    public Product atualizar(Long id, Product atualizacao) {
        Product existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        existente.setName(atualizacao.getName());
        existente.setDescription(atualizacao.getDescription());
        existente.setPrice(atualizacao.getPrice());
        existente.setStock(atualizacao.getStock());

        return repository.save(existente);
    }

    @Transactional
    public void inativar(Long id) {
        Product produto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));
        produto.setDeleted(true);
        repository.save(produto);
    }

    public Product restaurar(Long id) {
        Product produto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));
        if (!produto.isDeleted()) {
            throw new RuntimeException("Produto já está ativo.");
        }
        produto.setDeleted(false);
        return repository.save(produto);
    }

    public Product aplicarDescontoPercentual(Long id, double percent) {
        if (percent < 1 || percent > 80) {
            throw new IllegalArgumentException("Percentual deve estar entre 1% e 80%.");
        }

        Product produto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        if (temDescontoAtivo(produto)) {
            throw new RuntimeException("Produto já possui desconto ativo.");
        }

        BigDecimal desconto = produto.getPrice()
                .multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100));

        BigDecimal precoFinal = produto.getPrice().subtract(desconto);

        if (precoFinal.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            throw new RuntimeException("Preço final não pode ser inferior a R$ 0,01.");
        }

        ProductDiscount descontoAtivo = ProductDiscount.builder()
                .product(produto)
                .discountType(DiscountType.PERCENT)
                .percentValue(percent)
                .appliedAt(LocalDateTime.now())
                .build();

        discountRepository.save(descontoAtivo);

        return produto;
    }

    public Product aplicarCupom(Long id, String codigoCupom) {
        Product produto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        if (temDescontoAtivo(produto)) {
            throw new RuntimeException("Produto já possui desconto ativo.");
        }

        Coupon cupom = couponRepository.findByNormalizedCode(codigoCupom.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Cupom não encontrado."));

        if (!cupom.isValidoAgora()) {
            throw new RuntimeException("Cupom fora do período de validade.");
        }

        BigDecimal desconto;

        if (cupom.getType() == CouponType.PERCENT) {
            desconto = produto.getPrice()
                    .multiply(cupom.getValue()).divide(BigDecimal.valueOf(100));
        } else if (cupom.getType() == CouponType.FIXED) {
            desconto = cupom.getValue();
        } else {
            throw new RuntimeException("Tipo de cupom inválido.");
        }

        BigDecimal precoFinal = produto.getPrice().subtract(desconto);

        if (precoFinal.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            throw new RuntimeException("Preço final não pode ser menor que R$ 0,01.");
        }

        ProductDiscount descontoAtivo = ProductDiscount.builder()
                .product(produto)
                .coupon(cupom)
                .discountType(DiscountType.COUPON)
                .appliedAt(LocalDateTime.now())
                .build();

        discountRepository.save(descontoAtivo);

        return produto;
    }

    @Transactional
    public void removerDesconto(Long id) {
        Product produto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        if (!temDescontoAtivo(produto)) {
            throw new RuntimeException("Produto não possui desconto ativo.");
        }

        discountRepository.deleteByProduct(produto);
    }
    public Page<Product> listarComFiltros(
            int page, int limit,
            String search,
            Double minPrice, Double maxPrice,
            Boolean hasDiscount,
            Boolean includeDeleted,
            Boolean onlyOutOfStock,
            Boolean withCouponApplied,
            String sortBy, String sortOrder
    ) {
        int pageIndex = Math.max(0, page - 1);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageIndex, limit, Sort.by(direction, sortBy));

        List<Product> todos = repository.findAll();
        Stream<Product> stream = todos.stream();

        if (Boolean.FALSE.equals(includeDeleted)) {
            stream = stream.filter(p -> !p.isDeleted());
        }

        if (Boolean.TRUE.equals(onlyOutOfStock)) {
            stream = stream.filter(p -> p.getStock() == 0);
        }

        if (Boolean.TRUE.equals(hasDiscount)) {
            stream = stream.filter(p -> discountRepository.existsByProduct(p));
        }

        if (search != null && !search.trim().isEmpty()) {
            String termo = search.trim().toLowerCase();
            stream = stream.filter(p ->
                    p.getName().toLowerCase().contains(termo)
                            || p.getDescription().toLowerCase().contains(termo)
            );
        }

        if (minPrice != null) {
            stream = stream.filter(p -> p.getPrice().compareTo(BigDecimal.valueOf(minPrice)) >= 0);
        }

        if (maxPrice != null) {
            stream = stream.filter(p -> p.getPrice().compareTo(BigDecimal.valueOf(maxPrice)) <= 0);
        }

        if (Boolean.TRUE.equals(withCouponApplied)) {
            stream = stream.filter(p ->
                    discountRepository.findByProduct(p)
                            .map(desc -> desc.getDiscountType() == DiscountType.COUPON)
                            .orElse(false)
            );
        }

        List<Product> filtrados = stream.toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtrados.size());
        List<Product> sublista = start > filtrados.size() ? List.of() : filtrados.subList(start, end);

        return new PageImpl<>(sublista, pageable, filtrados.size());
    }

    public Product aplicarPatch(Product produtoOriginal, JsonPatch patch) {
        try {
            JsonNode produtoNode = objectMapper.valueToTree(produtoOriginal);
            JsonNode patchedNode = patch.apply(produtoNode);
            return objectMapper.treeToValue(patchedNode, Product.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao aplicar JSON Patch: " + e.getMessage());
        }
    }
}
