package com.example.sportsbook.fraud.service;

import com.example.sportsbook.fraud.model.FraudOperation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FraudContext {
    private final Long userId;
    private final FraudOperation operation;
    private final LocalDateTime timestamp;
    // Snapshot data (not entities)
    private final String deviceId;
    private final String ipAddress;
    private final String paymentInstrumentFingerprint;
}
