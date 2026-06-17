package com.example.sportsbook.responsiblegaming.model;

import com.example.sportsbook.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_limits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "daily_deposit_limit")
    private BigDecimal dailyDepositLimit;

    @Column(name = "weekly_deposit_limit")
    private BigDecimal weeklyDepositLimit;

    @Column(name = "monthly_deposit_limit")
    private BigDecimal monthlyDepositLimit;

    @Column(name = "daily_stake_limit")
    private BigDecimal dailyStakeLimit;

    @Column(name = "weekly_stake_limit")
    private BigDecimal weeklyStakeLimit;

    @Column(name = "monthly_stake_limit")
    private BigDecimal monthlyStakeLimit;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
