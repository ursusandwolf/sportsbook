package com.example.sportsbook.fraud.service;

import com.example.sportsbook.fraud.model.FraudSignal;
import com.example.sportsbook.fraud.model.FraudSignalType;
import com.example.sportsbook.fraud.model.RiskAssessment;
import com.example.sportsbook.fraud.model.RiskDecision;
import com.example.sportsbook.fraud.repository.FraudSignalRepository;
import com.example.sportsbook.fraud.repository.RiskAssessmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceTests {

    @Mock
    private FraudSignalRepository fraudSignalRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @InjectMocks
    private RiskAssessmentService riskAssessmentService;

    @Test
    void shouldCalculateScoreAndDetermineDecision() {
        Long userId = 1L;
        List<FraudSignal> signals = List.of(
                FraudSignal.builder().weight(20).ruleCode("RULE_1").type(FraudSignalType.SHARED_DEVICE).build(),
                FraudSignal.builder().weight(40).ruleCode("RULE_2").type(FraudSignalType.VPN_OR_PROXY).build()
        );

        when(fraudSignalRepository.findBySubjectUserId(userId)).thenReturn(signals);
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(i -> i.getArguments()[0]);

        RiskAssessment assessment = riskAssessmentService.assess(userId);

        assertEquals(60, assessment.getScore());
        assertEquals(RiskDecision.REQUIRE_ADDITIONAL_VERIFICATION, assessment.getDecision());
    }
}
