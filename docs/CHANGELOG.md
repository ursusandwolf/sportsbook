# Changelog

## [0.0.1] - 2026-06-11

### Added
- **Iteration 0**: Project skeleton with Spring Boot, Maven, PostgreSQL, and Liquibase. Added `GET /api/public/health`.
- **Iteration 1**: Initial Spring Security setup with HTTP Basic and in-memory users (`player`, `support`).
- **Iteration 2**: Dynamic user management. Persistent `User` and `Role` entities in PostgreSQL. Custom `UserDetailsService` to load users from the database. Added seed data via Liquibase.
- **Iteration 3**: Password hardening with BCrypt. Implemented account status checks (SUSPENDED, PENDING) and expiration logic (updatedAt > 365 days).
- **Iteration 4**: User Registration API. Implemented `RegistrationRequest` DTO with Bean Validation (email, password complexity, 18+ age check). Added Global Exception Handling for validation and business errors.
- **Iteration 5**: Method-level security. Added `SecurityUtils` for user context access and applied `@PreAuthorize` on admin endpoints.
- **Iteration 6**: JWT Authentication. Implemented stateless authentication using JSON Web Tokens. Added `/api/auth/login` endpoint and `JwtAuthenticationFilter`.

## [0.0.2] - 2026-06-12

### Fixed
- **Security P1**: `JwtAuthenticationFilter` now checks user status (`isEnabled`, `isAccountNonLocked`, `isAccountNonExpired`) before authenticating a request with JWT.
- **Performance**: Optimized JWT parsing in `JwtAuthenticationFilter` by combining validation and claim extraction.

### Added
- **Iteration 7**: Wallet and Immutable Ledger.
    - Persistent `Wallet` entity with `availableBalance` and `reservedBalance`.
    - `WalletEntry` (ledger) for auditing all financial transactions.
    - Optimistic locking using `@Version` to prevent race conditions.
    - Idempotency support for financial operations using `idempotencyKey`.
    - `WalletService` with atomic deposit and fund reservation logic.
    - Automatic wallet creation upon user registration.
    - `WalletController` for balance checks and deposits.
    - Integration tests for wallet operations and idempotency.
