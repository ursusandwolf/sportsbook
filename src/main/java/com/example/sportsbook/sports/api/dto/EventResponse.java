package com.example.sportsbook.sports.api.dto;

import com.example.sportsbook.market.api.dto.MarketResponse;
import com.example.sportsbook.sports.model.EventStatus;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record EventResponse(
    Long id,
    String sportName,
    String name,
    String homeParticipant,
    String awayParticipant,
    OffsetDateTime startsAt,
    EventStatus status,
    List<MarketResponse> markets
) {}
