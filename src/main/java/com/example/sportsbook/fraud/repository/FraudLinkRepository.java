package com.example.sportsbook.fraud.repository;

import com.example.sportsbook.fraud.model.FraudLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FraudLinkRepository extends JpaRepository<FraudLink, Long> {
    Optional<FraudLink> findByLeftUserIdAndRightUserIdAndLinkType(Long leftUserId, Long rightUserId, com.example.sportsbook.fraud.model.FraudLinkType linkType);
    List<FraudLink> findByLeftUserId(Long leftUserId);
    List<FraudLink> findByRightUserId(Long rightUserId);
}
