package com.example.sportsbook.auth;

import com.example.sportsbook.auth.api.dto.*;
import com.example.sportsbook.security.jwt.JwtUtils;
import com.example.sportsbook.user.api.dto.RegistrationRequest;
import org.jboss.aerogear.security.otp.Totp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MfaIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    private String registerAndLogin(String email) {
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .email(email).password("Password123!").confirmPassword("Password123!")
                .dateOfBirth(LocalDate.now().minusYears(20)).build();
        restTemplate.postForEntity("http://localhost:" + port + "/api/auth/register", regRequest, Map.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("Password123!");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/login", loginRequest, LoginResponse.class);
        return response.getBody().getToken();
    }

    @Test
    void fullMfaFlowTest() {
        String email = "mfa_test@example.com";
        String token = registerAndLogin(email);

        // 1. Setup MFA
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<MfaSetupResponse> setupResp = restTemplate.exchange(
                "http://localhost:" + port + "/api/auth/mfa/setup", HttpMethod.POST, new HttpEntity<>(headers), MfaSetupResponse.class);
        
        String secret = setupResp.getBody().getSecret();
        assertThat(secret).isNotNull();

        // 2. Enable MFA with correct code
        String code = new Totp(secret).now();
        ResponseEntity<Void> enableResp = restTemplate.exchange(
                "http://localhost:" + port + "/api/auth/mfa/enable", HttpMethod.POST, 
                new HttpEntity<>(Map.of("secret", secret, "code", code), headers), Void.class);
        assertThat(enableResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3. Try to login - should get 202 and mfaToken
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("Password123!");
        ResponseEntity<LoginResponse> loginResp = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/login", loginRequest, LoginResponse.class);
        
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(loginResp.getBody().getMfaRequired()).isTrue();
        String mfaToken = loginResp.getBody().getMfaToken();
        assertThat(mfaToken).isNotNull();

        // 4. Verify 2FA code to get final token
        MfaVerificationRequest verifyReq = new MfaVerificationRequest();
        verifyReq.setMfaToken(mfaToken);
        verifyReq.setCode(new Totp(secret).now());

        ResponseEntity<LoginResponse> finalResp = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/verify-2fa", verifyReq, LoginResponse.class);
        
        assertThat(finalResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(finalResp.getBody().getToken()).isNotNull();
        assertThat(finalResp.getBody().getMfaRequired()).isFalse();
    }
}
