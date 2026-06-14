package com.example.sportsbook.market.api.dto;

import com.example.sportsbook.market.model.MarketStatus;
import com.example.sportsbook.market.model.MarketType;
import lombok.Builder;
import java.util.List;

@Builder
public record MarketResponse(
    Long id,
    MarketType marketType,
    MarketStatus status,
    List<SelectionResponse> selections
) {}
