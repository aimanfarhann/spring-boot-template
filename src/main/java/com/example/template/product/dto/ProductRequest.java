package com.example.template.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Body for creating a product and for fully replacing one (PUT). Limits mirror the database columns.
 */
public record ProductRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description,

        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2)
        BigDecimal price) {}
