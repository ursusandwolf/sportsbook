package com.example.sportsbook.market.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateSelectionRequest(
    @NotNull Long marketId,
    @NotBlank String name,
    @NotNull @DecimalMin("1.0001") BigDecimal odds
) {}
