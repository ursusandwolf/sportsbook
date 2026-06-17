package com.example.sportsbook.fraud.model;

public enum RiskDecision {
    ALLOW,
    ALLOW_WITH_MONITORING,
    BLOCK_BONUS,
    REQUIRE_ADDITIONAL_VERIFICATION,
    HOLD_WITHDRAWAL,
    MANUAL_REVIEW,
    RESTRICT_ACCOUNT,
    REJECT
}
