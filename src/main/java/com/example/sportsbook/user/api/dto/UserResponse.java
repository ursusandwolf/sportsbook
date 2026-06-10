package com.example.sportsbook.user.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class UserResponse {
    private final Long id;
    private final String email;
    private final String status;
    private final Set<String> roles;
}
