package com.example.sportsbook.fraud.service;

import com.example.sportsbook.fraud.model.FraudSignal;
import com.example.sportsbook.fraud.model.RiskAssessment;
import com.example.sportsbook.fraud.model.RiskDecision;
import com.example.sportsbook.fraud.repository.FraudSignalRepository;
import com.example.sportsbook.fraud.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiskAssessmentService {

    private final FraudSignalRepository fraudSignalRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;

    public RiskAssessment assess(Long userId) {
        // Simple weighted model: sum of weights of signals
        List<FraudSignal> signals = fraudSignalRepository.findBySubjectUserId(userId);
        int totalScore = signals.stream().mapToInt(FraudSignal::getWeight).sum();

        RiskDecision decision = determineDecision(totalScore);
        
        Map<String, Integer> triggeredRules = signals.stream()
                .collect(Collectors.toMap(FraudSignal::getRuleCode, FraudSignal::getWeight, Integer::sum));

        RiskAssessment assessment = RiskAssessment.builder()
                .subjectUserId(userId)
                .score(totalScore)
                .decision(decision)
                .policyVersion("1.0")
                .assessedAt(LocalDateTime.now())
                .triggeredRules(triggeredRules)
                .build();

        return riskAssessmentRepository.save(assessment);
    }

    private RiskDecision determineDecision(int score) {
        if (score >= 85) return RiskDecision.RESTRICT_ACCOUNT;
        if (score >= 70) return RiskDecision.MANUAL_REVIEW;
        if (score >= 50) return RiskDecision.REQUIRE_ADDITIONAL_VERIFICATION;
        if (score >= 30) return RiskDecision.ALLOW_WITH_MONITORING;
        return RiskDecision.ALLOW;
    }
}
