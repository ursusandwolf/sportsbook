package com.example.sportsbook.responsiblegaming.api.dto;

import java.math.BigDecimal;

public record UserLimitRequest(
    BigDecimal dailyDepositLimit,
    BigDecimal weeklyDepositLimit,
    BigDecimal monthlyDepositLimit,
    BigDecimal dailyStakeLimit,
    BigDecimal weeklyStakeLimit,
    BigDecimal monthlyStakeLimit
) {}
