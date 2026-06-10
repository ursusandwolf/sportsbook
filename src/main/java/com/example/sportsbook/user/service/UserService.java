package com.example.sportsbook.user.service;

import com.example.sportsbook.user.api.dto.RegistrationRequest;
import com.example.sportsbook.user.api.dto.RegistrationResponse;
import com.example.sportsbook.user.model.*;
import com.example.sportsbook.user.repository.RoleRepository;
import com.example.sportsbook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        Role playerRole = roleRepository.findByName(RoleName.ROLE_PLAYER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.PENDING_VERIFICATION)
                .kycStatus(KycStatus.NOT_STARTED)
                .emailVerified(false)
                .phoneVerified(false)
                .roles(Set.of(playerRole))
                .build();

        User savedUser = userRepository.save(user);

        return RegistrationResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .status(savedUser.getStatus().name())
                .build();
    }
}
