# Changelog

## [0.0.1] - 2026-06-11

### Added
- **Iteration 0**: Project skeleton with Spring Boot, Maven, PostgreSQL, and Liquibase. Added `GET /api/public/health`.
- **Iteration 1**: Initial Spring Security setup with HTTP Basic and in-memory users (`player`, `support`).
- **Iteration 2**: Dynamic user management. Persistent `User` and `Role` entities in PostgreSQL. Custom `UserDetailsService` to load users from the database. Added seed data via Liquibase.
- **Iteration 3**: Password hardening with BCrypt. Implemented account status checks (SUSPENDED, PENDING) and expiration logic (updatedAt > 365 days).
- **Iteration 4**: User Registration API. Implemented `RegistrationRequest` DTO with Bean Validation (email, password complexity, 18+ age check). Added Global Exception Handling for validation and business errors.
