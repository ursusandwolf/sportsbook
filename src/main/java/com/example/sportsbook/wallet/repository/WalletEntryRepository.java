package com.example.sportsbook.wallet.repository;

import com.example.sportsbook.wallet.model.WalletEntry;
import com.example.sportsbook.wallet.model.WalletEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface WalletEntryRepository extends JpaRepository<WalletEntry, Long> {
    Optional<WalletEntry> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT SUM(e.amount) FROM WalletEntry e WHERE e.wallet.user.id = :userId AND e.type = :type AND e.createdAt >= :since")
    BigDecimal sumAmountByUserIdAndTypeSince(@Param("userId") Long userId, 
                                            @Param("type") WalletEntryType type, 
                                            @Param("since") OffsetDateTime since);
}
