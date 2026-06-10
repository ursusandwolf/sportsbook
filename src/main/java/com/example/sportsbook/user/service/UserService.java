package com.example.sportsbook.user.service;

import com.example.sportsbook.user.api.dto.RegistrationRequest;
import com.example.sportsbook.user.api.dto.RegistrationResponse;
import com.example.sportsbook.user.api.dto.UserResponse;
import com.example.sportsbook.user.model.*;
import com.example.sportsbook.user.repository.RoleRepository;
import com.example.sportsbook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public UserResponse getUserResponseByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
            throw new RuntimeException("User not found");
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
