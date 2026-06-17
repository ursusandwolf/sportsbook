package com.example.sportsbook.fraud.repository;

import com.example.sportsbook.fraud.model.FraudCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudCaseRepository extends JpaRepository<FraudCase, Long> {
}
