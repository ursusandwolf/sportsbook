package com.example.sportsbook.market.repository;

import com.example.sportsbook.market.model.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarketRepository extends JpaRepository<Market, Long> {
    List<Market> findByEventId(Long eventId);
}
