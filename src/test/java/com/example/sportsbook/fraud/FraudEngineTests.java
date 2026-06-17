package com.example.sportsbook.fraud.service;

import com.example.sportsbook.fraud.model.FraudOperation;
import com.example.sportsbook.fraud.model.FraudSignalType;
import com.example.sportsbook.fraud.repository.FraudSignalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudEngineTests {

    @Mock
    private FraudSignalRepository fraudSignalRepository;

    @Mock
    private FraudRule rule;

    private FraudEngine fraudEngine;

    @Test
    void shouldRunRulesAndSaveSignals() {
        FraudContext context = FraudContext.builder()
                .userId(1L)
                .operation(FraudOperation.LOGIN)
                .timestamp(LocalDateTime.now())
                .build();

        FraudRule rule1 = mock(FraudRule.class);
        when(rule1.operation()).thenReturn(FraudOperation.LOGIN);
        when(rule1.priority()).thenReturn(1);
        when(rule1.evaluate(context)).thenReturn(new FraudRuleResult(true, FraudSignalType.HIGH_LOGIN_VELOCITY, 5, 10, "RULE_001", "ref"));

        FraudRule rule2 = mock(FraudRule.class);
        when(rule2.operation()).thenReturn(FraudOperation.LOGIN);
        when(rule2.priority()).thenReturn(2);
        when(rule2.evaluate(context)).thenReturn(FraudRuleResult.notTriggered());

        fraudEngine = new FraudEngine(List.of(rule1, rule2), fraudSignalRepository);
        fraudEngine.run(context);

        verify(fraudSignalRepository, times(1)).save(any());
    }
}
