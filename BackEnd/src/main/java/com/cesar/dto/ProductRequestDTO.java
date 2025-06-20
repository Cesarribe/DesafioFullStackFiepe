package com.cesar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 100, message = "Nome pode ter no máximo 100 caracteres.")
    private String name;

    @NotBlank(message = "Descrição é obrigatória.")
    @Size(max = 255, message = "Descrição pode ter no máximo 255 caracteres.")
    private String description;

    @NotNull(message = "Preço é obrigatório.")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que R$ 0,01.")
    private BigDecimal price;

    @Min(value = 0, message = "Estoque não pode ser negativo.")
    private int stock;
}

