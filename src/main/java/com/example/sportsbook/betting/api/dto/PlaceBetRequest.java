package com.example.sportsbook.betting.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PlaceBetRequest(
    @NotNull Long selectionId,
    @NotNull @DecimalMin("0.1") BigDecimal stake,
    @NotNull BigDecimal expectedOdds,
    @NotBlank @Size(max = 128) String idempotencyKey
) {}
