package com.example.sportsbook.payment;

import com.example.sportsbook.fraud.model.*;
import com.example.sportsbook.fraud.repository.FraudSignalRepository;
import com.example.sportsbook.payment.model.WithdrawalStatus;
import com.example.sportsbook.payment.repository.WithdrawalRepository;
import com.example.sportsbook.security.SecurityUser;
import com.example.sportsbook.security.jwt.JwtUtils;
import com.example.sportsbook.user.model.*;
import com.example.sportsbook.user.repository.RoleRepository;
import com.example.sportsbook.user.repository.UserRepository;
import com.example.sportsbook.wallet.model.WalletEntryType;
import com.example.sportsbook.wallet.repository.WalletEntryRepository;
import com.example.sportsbook.wallet.repository.WalletRepository;
import com.example.sportsbook.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WithdrawalIntegrationTests {
    @Autowired private MockMvc mvc;
    @Autowired private JwtUtils jwt;
    @Autowired private UserRepository users;
    @Autowired private RoleRepository roles;
    @Autowired private WalletService walletService;
    @Autowired private WalletRepository wallets;
    @Autowired private WalletEntryRepository entries;
    @Autowired private WithdrawalRepository withdrawals;
    @Autowired private FraudSignalRepository signals;
    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = users.saveAndFlush(User.builder()
                .email(UUID.randomUUID() + "@example.com").passwordHash("sensitive-password-hash")
                .status(UserStatus.ACTIVE).kycStatus(KycStatus.VERIFIED)
                .roles(new HashSet<>(Set.of(roles.findByName(RoleName.ROLE_PLAYER).orElseThrow()))).build());
        walletService.createWallet(user);
        walletService.deposit(user.getId(), new BigDecimal("100.00"), UUID.randomUUID().toString());
        token = jwt.generateToken(new SecurityUser(user));
    }

    @Test
    void unverifiedKycCannotReserveMoney() throws Exception {
        user.setKycStatus(KycStatus.NOT_STARTED);
        users.saveAndFlush(user);
        mvc.perform(post("/api/withdrawals").header("Authorization", "Bearer " + token)
                        .param("amount", "40").param("destinationFingerprint", "destination"))
                .andExpect(status().isUnprocessableEntity());
        assertBalance("100.00", "0.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-40", "0", "0.001", "40.001"})
    void invalidAmountCannotCreateWithdrawalOrChangeBalance(String amount) throws Exception {
        long initialCount = withdrawals.count();
        mvc.perform(post("/api/withdrawals").header("Authorization", "Bearer " + token)
                        .param("amount", amount).param("destinationFingerprint", "destination"))
                .andExpect(status().isBadRequest());
        assertThat(withdrawals.count()).isEqualTo(initialCount);
        assertBalance("100.00", "0.00");
    }

    @Test
    void insufficientFundsCannotCreateWithdrawal() throws Exception {
        mvc.perform(post("/api/withdrawals").header("Authorization", "Bearer " + token)
                        .param("amount", "101.00").param("destinationFingerprint", "destination"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
        assertBalance("100.00", "0.00");
    }

    @ParameterizedTest
    @CsvSource({"0,PENDING", "30,PENDING", "50,HELD_FOR_REVIEW", "70,HELD_FOR_REVIEW"})
    void withdrawalUsesRiskDecisionAndDoesNotExposeUserSecrets(int weight, WithdrawalStatus expected) throws Exception {
        addRiskSignal(weight);
        mvc.perform(post("/api/withdrawals").header("Authorization", "Bearer " + token)
                        .param("amount", "40.00").param("destinationFingerprint", "destination"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(expected.name()))
                .andExpect(jsonPath("$.amount").value(40))
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        assertBalance("60.00", "40.00");
    }

    @Test
    void restrictedRiskCannotReserveMoney() throws Exception {
        addRiskSignal(85);
        mvc.perform(post("/api/withdrawals").header("Authorization", "Bearer " + token)
                        .param("amount", "40").param("destinationFingerprint", "destination"))
                .andExpect(status().isUnprocessableEntity());
        assertBalance("100.00", "0.00");
    }

    @Test
    void selfExcludedUserCanWithdrawWithoutCreatingBetReservation() throws Exception {
        user.setStatus(UserStatus.SELF_EXCLUDED);
        users.saveAndFlush(user);
        mvc.perform(post("/api/withdrawals").header("Authorization", "Bearer " + token)
                        .param("amount", "40").param("destinationFingerprint", "destination"))
                .andExpect(status().isOk());
        assertBalance("60.00", "40.00");
        assertThat(entries.findAll().stream().filter(e -> e.getWallet().getUser().getId().equals(user.getId())))
                .anyMatch(e -> e.getType().name().equals("WITHDRAWAL_RESERVE"))
                .noneMatch(e -> e.getType() == WalletEntryType.BET_RESERVE);
    }

    private void addRiskSignal(int weight) {
        if (weight == 0) return;
        signals.saveAndFlush(FraudSignal.builder().subjectUserId(user.getId())
                .type(FraudSignalType.HIGH_REGISTRATION_VELOCITY).severity(weight).weight(weight)
                .ruleCode("TEST_RISK").detectedAt(LocalDateTime.now()).build());
    }

    private void assertBalance(String available, String reserved) {
        var wallet = wallets.findByUserId(user.getId()).orElseThrow();
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo(available);
        assertThat(wallet.getReservedBalance()).isEqualByComparingTo(reserved);
    }
}
