package com.example.sportsbook.auth.api;

import com.example.sportsbook.auth.api.dto.LoginRequest;
import com.example.sportsbook.auth.api.dto.LoginResponse;
import com.example.sportsbook.auth.api.dto.MfaVerificationRequest;
import com.example.sportsbook.auth.service.MfaService;
import com.example.sportsbook.security.SecurityUser;
import com.example.sportsbook.security.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final MfaService mfaService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();

        if (userDetails.getUser().isMfaEnabled()) {
            String mfaToken = jwtUtils.generateMfaToken(userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(LoginResponse.builder()
                            .mfaRequired(true)
                            .mfaToken(mfaToken)
                            .build());
        }

        String token = jwtUtils.generateToken(userDetails);
        return ResponseEntity.ok(new LoginResponse(token, false, null));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponse> verify2fa(@Valid @RequestBody MfaVerificationRequest request) {
        Claims claims = jwtUtils.validateAndGetClaims(request.getMfaToken());
        if (claims == null || claims.get("mfa") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwtUtils.getUsernameFromClaims(claims);
        SecurityUser userDetails = (SecurityUser) userDetailsService.loadUserByUsername(email);

        if (!mfaService.verifyCode(userDetails.getUser().getMfaSecret(), request.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtUtils.generateToken(userDetails);
        return ResponseEntity.ok(new LoginResponse(token, false, null));
    }
}
