package com.example.sportsbook.wallet;

import com.example.sportsbook.auth.api.dto.LoginRequest;
import com.example.sportsbook.auth.api.dto.LoginResponse;
import com.example.sportsbook.user.api.dto.RegistrationRequest;
import com.example.sportsbook.wallet.api.dto.DepositRequest;
import com.example.sportsbook.wallet.api.dto.WalletResponse;
import com.example.sportsbook.wallet.api.dto.WithdrawalRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WalletIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String registerAndLogin(String email) {
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .email(email)
                .password("Password123!")
                .confirmPassword("Password123!")
                .dateOfBirth(LocalDate.now().minusYears(20))
                .build();
        restTemplate.postForEntity("http://localhost:" + port + "/api/auth/register", regRequest, Map.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("Password123!");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/login", loginRequest, LoginResponse.class);
        return response.getBody().getToken();
    }

    @Test
    void newUserHasEmptyWallet() {
        String token = registerAndLogin("wallet_test1@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<WalletResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/wallets/me", HttpMethod.GET, entity, WalletResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().availableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void depositIncreasesBalance() {
        String token = registerAndLogin("wallet_test2@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        DepositRequest depositRequest = new DepositRequest(new BigDecimal("100.00"), UUID.randomUUID().toString());
        HttpEntity<DepositRequest> depositEntity = new HttpEntity<>(depositRequest, headers);

        restTemplate.postForEntity("http://localhost:" + port + "/api/wallets/deposit", depositEntity, Void.class);

        ResponseEntity<WalletResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/wallets/me", HttpMethod.GET, new HttpEntity<>(headers), WalletResponse.class);

        assertThat(response.getBody().availableBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void depositIsIdempotent() {
        String token = registerAndLogin("wallet_test3@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        String idempotencyKey = UUID.randomUUID().toString();
        DepositRequest depositRequest = new DepositRequest(new BigDecimal("100.00"), idempotencyKey);
        HttpEntity<DepositRequest> depositEntity = new HttpEntity<>(depositRequest, headers);

        // First deposit
        restTemplate.postForEntity("http://localhost:" + port + "/api/wallets/deposit", depositEntity, Void.class);
        // Second deposit with same key
        restTemplate.postForEntity("http://localhost:" + port + "/api/wallets/deposit", depositEntity, Void.class);

        ResponseEntity<WalletResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/wallets/me", HttpMethod.GET, new HttpEntity<>(headers), WalletResponse.class);

        assertThat(response.getBody().availableBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void withdrawDecreasesBalance() {
        String token = registerAndLogin("withdraw_test@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // Deposit first
        restTemplate.postForEntity("http://localhost:" + port + "/api/wallets/deposit", 
                new HttpEntity<>(new DepositRequest(new BigDecimal("100.00"), UUID.randomUUID().toString()), headers), Void.class);

        // Withdraw
        WithdrawalRequest withdrawRequest = new WithdrawalRequest(new BigDecimal("40.00"), UUID.randomUUID().toString());
        restTemplate.postForEntity("http://localhost:" + port + "/api/wallets/withdraw", new HttpEntity<>(withdrawRequest, headers), Void.class);

        ResponseEntity<WalletResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/wallets/me", HttpMethod.GET, new HttpEntity<>(headers), WalletResponse.class);

        assertThat(response.getBody().availableBalance()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void withdrawFailsIfInsufficientFunds() {
        String token = registerAndLogin("withdraw_fail@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        WithdrawalRequest withdrawRequest = new WithdrawalRequest(new BigDecimal("100.00"), UUID.randomUUID().toString());
        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/api/wallets/withdraw", 
                new HttpEntity<>(withdrawRequest, headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("code")).isEqualTo("INSUFFICIENT_FUNDS");
    }
}
