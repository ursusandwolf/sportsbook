package com.example.sportsbook.fraud.model;

public enum FraudCaseStatus {
    OPEN,
    IN_REVIEW,
    WAITING_FOR_INFORMATION,
    CONFIRMED_FRAUD,
    FALSE_POSITIVE,
    RESTRICTED,
    CLOSED
}
