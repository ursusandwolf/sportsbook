package com.example.sportsbook.payment.service;

import com.example.sportsbook.payment.model.PaymentInstrument;
import com.example.sportsbook.payment.model.WithdrawalDestination;
import com.example.sportsbook.payment.repository.PaymentInstrumentRepository;
import com.example.sportsbook.payment.repository.WithdrawalDestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentInstrumentRepository paymentInstrumentRepository;
    private final WithdrawalDestinationRepository withdrawalDestinationRepository;

    @Transactional
    public PaymentInstrument registerPaymentInstrument(Long userId, String providerToken, String type, String rawData) {
        String fingerprint = generateFingerprint(rawData);
        PaymentInstrument instrument = PaymentInstrument.builder()
                .userId(userId)
                .providerToken(providerToken)
                .type(type)
                .fingerprint(fingerprint)
                .build();
        return paymentInstrumentRepository.save(instrument);
    }

    @Transactional
    public WithdrawalDestination registerWithdrawalDestination(Long userId, String type, String rawData) {
        String fingerprint = generateFingerprint(rawData);
        WithdrawalDestination destination = WithdrawalDestination.builder()
                .userId(userId)
                .type(type)
                .fingerprint(fingerprint)
                .build();
        return withdrawalDestinationRepository.save(destination);
    }

    private String generateFingerprint(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating fingerprint", e);
        }
    }
}
