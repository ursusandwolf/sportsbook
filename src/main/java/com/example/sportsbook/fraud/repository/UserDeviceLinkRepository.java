package com.example.sportsbook.fraud.repository;

import com.example.sportsbook.fraud.model.UserDeviceLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDeviceLinkRepository extends JpaRepository<UserDeviceLink, Long> {
    Optional<UserDeviceLink> findByUserIdAndDeviceId(Long userId, Long deviceId);
}
