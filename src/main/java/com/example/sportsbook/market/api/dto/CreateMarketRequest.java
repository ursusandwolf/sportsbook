package com.example.sportsbook.market.api.dto;

import com.example.sportsbook.market.model.MarketStatus;
import com.example.sportsbook.market.model.MarketType;
import jakarta.validation.constraints.NotNull;

public record CreateMarketRequest(
    @NotNull Long eventId,
    @NotNull MarketType marketType,
    @NotNull MarketStatus status
) {}
