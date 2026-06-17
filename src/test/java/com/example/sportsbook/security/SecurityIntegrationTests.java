package com.example.sportsbook.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void unauthenticatedAccess_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/fraud/cases/1/status")
                .param("status", "CLOSED"))
                .andExpect(status().isUnauthorized());
    }

    // Note: Additional tests for role-based access would require
    // setting up authenticated users (e.g., using @WithMockUser)
    // or real auth flow.
}
