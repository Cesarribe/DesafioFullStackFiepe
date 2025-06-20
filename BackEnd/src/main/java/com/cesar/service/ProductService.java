package com.cesar.service;

import com.cesar.model.*;
import com.cesar.repository.CouponRepository;
import com.cesar.repository.ProductDiscountRepository;
import com.cesar.repository.ProductRepository;
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

    public void removerDesconto(Long id) {
        Product produto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        if (!temDescontoAtivo(produto)) {
            throw new RuntimeException("Produto não possui desconto ativo.");
        }

        discountRepository.deleteByProduct(produto);
    }
    public Page<Product> listarComFiltros(int page, int size, String sortField,
                                          boolean withDiscount, boolean includeInactive, boolean includeOutOfStock) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortField));

        List<Product> todos = repository.findAll(); // ou um método com filtro mais específico
        Stream<Product> stream = todos.stream();

        if (!includeInactive) {
            stream = stream.filter(p -> !p.isDeleted());
        }

        if (!includeOutOfStock) {
            stream = stream.filter(p -> p.getStock() > 0);
        }

        if (withDiscount) {
            stream = stream.filter(p -> discountRepository.existsByProduct(p));
        }

        List<Product> filtrados = stream.toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtrados.size());

        return new PageImpl<>(filtrados.subList(start, end), pageable, filtrados.size());
    }

}
