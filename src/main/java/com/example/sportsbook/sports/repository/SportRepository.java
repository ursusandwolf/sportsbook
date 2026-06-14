package com.example.sportsbook.sports.repository;

import com.example.sportsbook.sports.model.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SportRepository extends JpaRepository<Sport, Long> {
    Optional<Sport> findByName(String name);
}
