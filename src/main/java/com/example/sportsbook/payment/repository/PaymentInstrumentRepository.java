package com.example.sportsbook.payment.repository;

import com.example.sportsbook.payment.model.PaymentInstrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentInstrumentRepository extends JpaRepository<PaymentInstrument, Long> {
    List<PaymentInstrument> findByFingerprint(String fingerprint);
}
