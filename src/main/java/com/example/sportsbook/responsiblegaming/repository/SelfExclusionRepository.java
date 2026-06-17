package com.example.sportsbook.responsiblegaming.repository;

import com.example.sportsbook.responsiblegaming.model.SelfExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelfExclusionRepository extends JpaRepository<SelfExclusion, Long> {
    List<SelfExclusion> findAllByUserId(Long userId);
}
