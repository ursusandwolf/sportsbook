package com.example.sportsbook.bonus.repository;

import com.example.sportsbook.bonus.model.BonusCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BonusCampaignRepository extends JpaRepository<BonusCampaign, Long> {
}
