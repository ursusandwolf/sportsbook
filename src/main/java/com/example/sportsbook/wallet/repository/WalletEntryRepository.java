package com.example.sportsbook.wallet.repository;

import com.example.sportsbook.wallet.model.WalletEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletEntryRepository extends JpaRepository<WalletEntry, Long> {
    Optional<WalletEntry> findByIdempotencyKey(String idempotencyKey);
}
