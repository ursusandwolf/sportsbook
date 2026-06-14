package com.example.sportsbook.sports.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateEventRequest(
    @NotNull Long sportId,
    @NotBlank String name,
    @NotBlank String homeParticipant,
    @NotBlank String awayParticipant,
    @NotNull @Future OffsetDateTime startsAt
) {}
