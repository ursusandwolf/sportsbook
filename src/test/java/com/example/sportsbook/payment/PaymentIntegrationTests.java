package com.example.sportsbook.payment;

import com.example.sportsbook.payment.model.PaymentInstrument;
import com.example.sportsbook.payment.repository.PaymentInstrumentRepository;
import com.example.sportsbook.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTests {
// ...

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentInstrumentRepository paymentInstrumentRepository;

    @Test
    void shouldRegisterAndFindPaymentInstrument() {
        Long userId = 1L;
        String rawData = "card-data-1234";
        String providerToken = "token-abc";
        String type = "CARD";

        paymentService.registerPaymentInstrument(userId, providerToken, type, rawData);

        List<PaymentInstrument> instruments = paymentInstrumentRepository.findAll();
        assertThat(instruments).hasSize(1);
        assertThat(instruments.get(0).getUserId()).isEqualTo(userId);
        assertThat(instruments.get(0).getFingerprint()).isNotEmpty();
        assertThat(instruments.get(0).getProviderToken()).isEqualTo(providerToken);
    }
}
