package com.example.sportsbook.user.repository;

import com.example.sportsbook.user.model.Role;
import com.example.sportsbook.user.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
