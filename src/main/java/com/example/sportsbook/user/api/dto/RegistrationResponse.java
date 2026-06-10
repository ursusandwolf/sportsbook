package com.example.sportsbook.user.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationResponse {
    private final Long id;
    private final String email;
    private final String status;
}
