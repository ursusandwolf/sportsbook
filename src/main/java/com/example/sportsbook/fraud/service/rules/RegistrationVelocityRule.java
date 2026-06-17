package com.example.sportsbook.fraud.service.rules;

import com.example.sportsbook.fraud.model.FraudOperation;
import com.example.sportsbook.fraud.model.FraudSignalType;
import com.example.sportsbook.fraud.service.FraudContext;
import com.example.sportsbook.fraud.service.FraudRule;
import com.example.sportsbook.fraud.service.FraudRuleResult;
import com.example.sportsbook.fraud.service.RedisVelocityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RegistrationVelocityRule implements FraudRule {

    private final RedisVelocityService redisVelocityService;
    private static final int MAX_REGISTRATIONS_PER_HOUR = 5;

    @Override
    public FraudRuleResult evaluate(FraudContext context) {
        String key = "reg_velocity:" + context.getIpAddress();
        long count = redisVelocityService.incrementAndGet(key, Duration.ofHours(1));

        if (count > MAX_REGISTRATIONS_PER_HOUR) {
            return new FraudRuleResult(true, FraudSignalType.HIGH_REGISTRATION_VELOCITY, 
                80, 80, "REG_VELOCITY_EXCEEDED", "Too many registrations from this IP: " + count);
        }
        
        return FraudRuleResult.notTriggered();
    }

    @Override
    public String code() {
        return "REG_VELOCITY_EXCEEDED";
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public FraudOperation operation() {
        return FraudOperation.REGISTRATION;
    }
}
