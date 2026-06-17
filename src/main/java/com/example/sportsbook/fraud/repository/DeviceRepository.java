package com.example.sportsbook.fraud.repository;

import com.example.sportsbook.fraud.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByFingerprintHash(String fingerprintHash);
}
