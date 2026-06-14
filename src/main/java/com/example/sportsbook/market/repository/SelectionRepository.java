package com.example.sportsbook.market.repository;

import com.example.sportsbook.market.model.Selection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SelectionRepository extends JpaRepository<Selection, Long> {
    List<Selection> findByMarketId(Long marketId);
}
