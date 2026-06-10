package com.example.sportsbook.user.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @GetMapping
    public Map<String, String> getProfile() {
        return Map.of(
                "username", "player",
                "email", "player@example.com",
                "status", "ACTIVE"
        );
    }
}
