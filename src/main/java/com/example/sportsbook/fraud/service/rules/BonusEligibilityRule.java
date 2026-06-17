package com.example.sportsbook.fraud.service.rules;

import com.example.sportsbook.fraud.model.FraudOperation;
import com.example.sportsbook.fraud.model.FraudSignalType;
import com.example.sportsbook.fraud.repository.FraudLinkRepository;
import com.example.sportsbook.fraud.repository.FraudSignalRepository;
import com.example.sportsbook.fraud.service.FraudContext;
import com.example.sportsbook.fraud.service.FraudRule;
import com.example.sportsbook.fraud.service.FraudRuleResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BonusEligibilityRule implements FraudRule {

    private final FraudLinkRepository fraudLinkRepository;
    private final FraudSignalRepository fraudSignalRepository;

    @Override
    public FraudRuleResult evaluate(FraudContext context) {
        // Logic to check if user has linked accounts or existing bonus claims
        // Simplified for now: just check if any links exist
        boolean hasLinks = !fraudLinkRepository.findByLeftUserId(context.getUserId()).isEmpty() 
            || !fraudLinkRepository.findByRightUserId(context.getUserId()).isEmpty();
        
        if (hasLinks) {
            return new FraudRuleResult(true, FraudSignalType.BONUS_PATTERN_MATCH, 
                50, 50, "BONUS_LINKED_ACCOUNT", "User has linked accounts");
        }
        
        return FraudRuleResult.notTriggered();
    }

    @Override
    public String code() {
        return "BONUS_LINKED_ACCOUNT";
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public FraudOperation operation() {
        return FraudOperation.BONUS_CLAIM;
    }
}
