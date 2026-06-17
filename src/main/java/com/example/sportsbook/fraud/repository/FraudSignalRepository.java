package com.example.sportsbook.fraud.repository;

import com.example.sportsbook.fraud.model.FraudSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FraudSignalRepository extends JpaRepository<FraudSignal, Long> {
    List<FraudSignal> findBySubjectUserId(Long subjectUserId);
}
