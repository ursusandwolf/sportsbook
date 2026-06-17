package com.example.sportsbook.responsiblegaming.api.dto;

import java.time.OffsetDateTime;

public record SelfExclusionRequest(
    OffsetDateTime endAt,
    String reason
) {}
