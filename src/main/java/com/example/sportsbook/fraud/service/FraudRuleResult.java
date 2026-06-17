package com.example.sportsbook.fraud.service;

import com.example.sportsbook.fraud.model.FraudSignalType;

public record FraudRuleResult(
        boolean triggered,
        FraudSignalType type,
        int severity,
        int weight,
        String ruleCode,
        String evidenceReference
) {
    public static FraudRuleResult notTriggered() {
        return new FraudRuleResult(false, null, 0, 0, null, null);
    }
}
