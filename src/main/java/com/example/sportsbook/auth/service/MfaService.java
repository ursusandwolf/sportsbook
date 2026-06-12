package com.example.sportsbook.auth.service;

import com.example.sportsbook.user.model.User;
import com.example.sportsbook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final UserRepository userRepository;

    public String generateNewSecret() {
        return Base32.random();
    }

    public boolean verifyCode(String secret, String code) {
        if (code == null || code.length() != 6) return false;
        try {
            Totp totp = new Totp(secret);
            return totp.verify(code);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void enableMfa(Long userId, String secret) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setMfaSecret(secret);
        user.setMfaEnabled(true);
        userRepository.save(user);
    }
}
