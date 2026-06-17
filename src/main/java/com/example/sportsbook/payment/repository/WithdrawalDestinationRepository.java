package com.example.sportsbook.payment.repository;

import com.example.sportsbook.payment.model.WithdrawalDestination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalDestinationRepository extends JpaRepository<WithdrawalDestination, Long> {
    List<WithdrawalDestination> findByFingerprint(String fingerprint);
}
