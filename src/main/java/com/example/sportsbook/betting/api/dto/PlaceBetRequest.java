package com.example.sportsbook.betting.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PlaceBetRequest(
    @NotNull Long selectionId,
    @NotNull @DecimalMin("0.1") BigDecimal stake,
    @NotNull BigDecimal expectedOdds
) {}
