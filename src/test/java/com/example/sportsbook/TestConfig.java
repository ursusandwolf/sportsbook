package com.example.sportsbook;

import com.example.sportsbook.sports.model.Sport;
import com.example.sportsbook.sports.repository.SportRepository;
import com.example.sportsbook.user.model.KycStatus;
import com.example.sportsbook.user.model.Role;
import com.example.sportsbook.user.model.RoleName;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.user.model.UserStatus;
import com.example.sportsbook.user.repository.RoleRepository;
import com.example.sportsbook.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Set;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    public CommandLineRunner initTestData(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, SportRepository sportRepository) {
        return args -> {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_ADMIN)));
            Role playerRole = roleRepository.findByName(RoleName.ROLE_PLAYER)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_PLAYER)));

            if (sportRepository.findByName("Football").isEmpty()) {
                sportRepository.save(new Sport(null, "Football"));
            }

            User admin = User.builder()
                    .email("admin@example.com")
                    .passwordHash(passwordEncoder.encode("password"))
                    .status(UserStatus.ACTIVE)
                    .kycStatus(KycStatus.NOT_STARTED)
                    .roles(Set.of(adminRole))
                    .createdAt(OffsetDateTime.now())
                    .build();
            if (userRepository.findByEmail(admin.getEmail()).isEmpty()) {
                userRepository.save(admin);
            }

            User player = User.builder()
                    .email("player@example.com")
                    .passwordHash(passwordEncoder.encode("password"))
                    .status(UserStatus.ACTIVE)
                    .kycStatus(KycStatus.NOT_STARTED)
                    .roles(Set.of(playerRole))
                    .createdAt(OffsetDateTime.now())
                    .build();
            if (userRepository.findByEmail(player.getEmail()).isEmpty()) {
                userRepository.save(player);
            }
        };
    }
}
