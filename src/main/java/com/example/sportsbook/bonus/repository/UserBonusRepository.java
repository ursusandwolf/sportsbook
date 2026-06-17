package com.example.sportsbook.bonus.repository;

import com.example.sportsbook.bonus.model.UserBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBonusRepository extends JpaRepository<UserBonus, Long> {
    List<UserBonus> findAllByUserIdAndStatus(Long userId, String status);
}
