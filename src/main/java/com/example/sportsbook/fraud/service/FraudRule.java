package com.example.sportsbook.fraud.service;

import com.example.sportsbook.fraud.model.FraudOperation;

public interface FraudRule {
    FraudRuleResult evaluate(FraudContext context);
    String code();
    int priority();
    FraudOperation operation();
}
