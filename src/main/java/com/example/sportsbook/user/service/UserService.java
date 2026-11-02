package com.example.sportsbook.user.service;

import com.example.sportsbook.fraud.model.FraudOperation;
import com.example.sportsbook.fraud.service.FraudContext;
import com.example.sportsbook.fraud.service.FraudEngine;
import com.example.sportsbook.fraud.service.FraudRuleResult;
import com.example.sportsbook.user.api.dto.RegistrationRequest;
import com.example.sportsbook.user.api.dto.RegistrationResponse;
import com.example.sportsbook.user.api.dto.UserResponse;
import com.example.sportsbook.user.model.*;
import com.example.sportsbook.user.repository.RoleRepository;
import com.example.sportsbook.user.repository.UserRepository;
import com.example.sportsbook.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final FraudEngine fraudEngine;

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request, String ipAddress) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        Role playerRole = roleRepository.findByName(RoleName.ROLE_PLAYER)
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .kycStatus(KycStatus.NOT_STARTED)
                .emailVerified(false)
                .phoneVerified(false)
                .roles(Set.of(playerRole))
                .build();

        User savedUser = userRepository.save(user);
        walletService.createWallet(savedUser);

        List<FraudRuleResult> triggered = fraudEngine.run(FraudContext.builder()
                .userId(savedUser.getId())
                .operation(FraudOperation.REGISTRATION)
                .timestamp(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build());

        boolean registrationBlocked = triggered.stream()
                .anyMatch(result -> "REG_VELOCITY_EXCEEDED".equals(result.ruleCode()));
        if (registrationBlocked) {
            throw new IllegalStateException("Too many registrations from this IP");
        }

        return RegistrationResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .status(savedUser.getStatus().name())
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserResponseByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return mapToResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .roles(user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()))
                .build();
    }
}
