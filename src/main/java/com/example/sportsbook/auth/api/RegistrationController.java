package com.example.sportsbook.auth.api;

import com.example.sportsbook.user.api.dto.RegistrationRequest;
import com.example.sportsbook.user.api.dto.RegistrationResponse;
import com.example.sportsbook.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        return userService.register(request);
    }
}
