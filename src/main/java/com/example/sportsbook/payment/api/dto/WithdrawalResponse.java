package com.example.sportsbook.payment.api.dto;

import com.example.sportsbook.payment.model.WithdrawalStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WithdrawalResponse(Long id, BigDecimal amount, WithdrawalStatus status,
                                 OffsetDateTime createdAt) {}
