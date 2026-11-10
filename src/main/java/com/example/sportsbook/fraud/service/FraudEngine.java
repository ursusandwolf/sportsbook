package com.example.sportsbook.fraud.service;

import com.example.sportsbook.fraud.model.FraudSignal;
import com.example.sportsbook.fraud.repository.FraudSignalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudEngine {

    private final List<FraudRule> rules;
    private final FraudSignalRepository fraudSignalRepository;

    public List<FraudRuleResult> run(FraudContext context) {
        List<FraudRuleResult> triggered = rules.stream()
                .filter(rule -> rule.operation() == context.getOperation())
                .sorted((r1, r2) -> Integer.compare(r2.priority(), r1.priority()))
                .map(rule -> rule.evaluate(context))
                .filter(FraudRuleResult::triggered)
                .toList();

        if (context.getUserId() != null) {
            triggered.forEach(result -> persistSignal(context.getUserId(), result));
        }

        return triggered;
    }

    private void persistSignal(Long userId, FraudRuleResult result) {
        fraudSignalRepository.save(FraudSignal.builder()
                .subjectUserId(userId)
                .type(result.type())
                .severity(result.severity())
                .weight(result.weight())
                .ruleCode(result.ruleCode())
                .evidenceReference(result.evidenceReference())
                .detectedAt(LocalDateTime.now())
                .build());
    }
}
