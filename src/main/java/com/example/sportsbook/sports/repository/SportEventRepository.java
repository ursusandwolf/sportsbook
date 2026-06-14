package com.example.sportsbook.sports.repository;

import com.example.sportsbook.sports.model.SportEvent;
import com.example.sportsbook.sports.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SportEventRepository extends JpaRepository<SportEvent, Long> {
    List<SportEvent> findByStatus(EventStatus status);

    @Query("SELECT e FROM SportEvent e JOIN FETCH e.sport WHERE e.status = :status ORDER BY e.startsAt ASC")
    List<SportEvent> findAllWithSportByStatus(EventStatus status);
}
