package com.example.sportsbook.security;

import com.example.sportsbook.security.jwt.JwtUtils;
import com.example.sportsbook.user.model.*;
import com.example.sportsbook.user.repository.RoleRepository;
import com.example.sportsbook.user.repository.UserRepository;
import com.example.sportsbook.wallet.service.WalletService;
import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityRegressionTests {
    @Autowired private MockMvc mvc;
    @Autowired private JwtUtils jwt;
    @Autowired private UserRepository users;
    @Autowired private RoleRepository roles;
    @Autowired private WalletService wallets;

    private User user;

    @BeforeEach
    void setUp() {
        user = users.saveAndFlush(User.builder()
                .email(UUID.randomUUID() + "@example.com").passwordHash("unused")
                .status(UserStatus.ACTIVE).kycStatus(KycStatus.VERIFIED)
                .mfaEnabled(true).mfaSecret("JBSWY3DPEHPK3PXP")
                .roles(new HashSet<>(Set.of(roles.findByName(RoleName.ROLE_PLAYER).orElseThrow()))).build());
        wallets.createWallet(user);
    }

    @Test
    void mfaChallengeMustNotAuthenticateProtectedRequests() throws Exception {
        mvc.perform(get("/api/wallets/me")
                        .header("Authorization", "Bearer " + jwt.generateMfaToken(user.getEmail())))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/mfa/setup", "/api/auth/mfa/enable"})
    void mfaConfigurationRequiresAuthentication(String path) throws Exception {
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessTokenStillAuthenticatesProtectedRequests() throws Exception {
        mvc.perform(get("/api/wallets/me")
                        .header("Authorization", "Bearer " + jwt.generateToken(new SecurityUser(user))))
                .andExpect(status().isOk());
    }

    @Test
    void mfaVerificationMustRecheckAccountStatus() throws Exception {
        String challenge = jwt.generateMfaToken(user.getEmail());
        user.setStatus(UserStatus.SUSPENDED);
        users.saveAndFlush(user);
        mvc.perform(post("/api/auth/verify-2fa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mfaToken\":\"" + challenge + "\",\"code\":\""
                                + new Totp(user.getMfaSecret()).now() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessTokenCannotBeUsedAsMfaChallenge() throws Exception {
        mvc.perform(post("/api/auth/verify-2fa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mfaToken\":\"" + jwt.generateToken(new SecurityUser(user))
                                + "\",\"code\":\"" + new Totp(user.getMfaSecret()).now() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void legacyWithdrawalCannotBypassKycOrFraud() throws Exception {
        user.setKycStatus(KycStatus.NOT_STARTED);
        users.saveAndFlush(user);
        wallets.deposit(user.getId(), new BigDecimal("100.00"), UUID.randomUUID().toString());
        mvc.perform(post("/api/wallets/withdraw")
                        .header("Authorization", "Bearer " + jwt.generateToken(new SecurityUser(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":40,\"idempotencyKey\":\"legacy-bypass\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void metricsRequireAdminAndHealthHidesDependencyDetails() throws Exception {
        mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/metrics")
                        .header("Authorization", "Bearer " + jwt.generateToken(new SecurityUser(user))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }
}
