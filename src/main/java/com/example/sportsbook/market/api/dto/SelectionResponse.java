package com.example.sportsbook.market.api.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record SelectionResponse(
    Long id,
    String name,
    BigDecimal odds
) {}
