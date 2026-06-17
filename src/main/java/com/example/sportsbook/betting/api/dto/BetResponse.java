package com.example.sportsbook.betting.api.dto;

import com.example.sportsbook.betting.model.BetStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BetResponse(
    Long id,
    Long selectionId,
    String selectionName,
    BigDecimal stake,
    BigDecimal oddsAtPlacement,
    BigDecimal potentialPayout,
    BetStatus status,
    OffsetDateTime placedAt
) {}
