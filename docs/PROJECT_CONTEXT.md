# Project Context: Sportsbook

## Status
- **Current Phase**: Iteration 9 Complete
- **Last Update**: 2026-06-17

## Project Goal
Create a learning-focused Sportsbook backend with a strong emphasis on Spring Security, financial integrity, and anti-fraud mechanisms.

## System State
- **Core**: Spring Boot 3.4.0, Java 21.
- **Database**: PostgreSQL (Production), H2 (Test). Migrations managed by Liquibase.
- **Security**: 
    - Layer 2.1 implemented.
    - Authentication: JWT (JSON Web Token).
    - MFA: TOTP supported (Aerogear).
    - User Store: Database-backed (JPA + CustomUserDetailsService).
    - Roles: ROLE_PLAYER, ROLE_SUPPORT, ROLE_RISK_ANALYST, ROLE_TRADER, ROLE_ADMIN.
    - User Status Enforcement: Checked during JWT authentication and before business operations.
- **Modules**: 
    - `common`: Health, Exception handling.
    - `user`: Entities, Repositories, Profile API.
    - `security`: SecurityFilterChain, UserDetails implementation, JWT.
    - `auth`: Registration, Login, MFA.
    - `wallet`: Wallet management, immutable ledger, atomic updates.
    - `sports`: Sport, SportEvent management.
    - `market`: Market and Selection management.
    - `betting`: Single bet placement and lifecycle.

## Roadmap & Pending Items
- [x] **Iteration 3**: PasswordEncoder hardening and User Statuses (Locked/Disabled).
- [x] **Iteration 4**: User Registration with validation.
- [x] **Iteration 5**: Current user context and method-level security.
- [x] **Iteration 6**: JWT implementation.
- [x] **Iteration 7**: Wallet and immutable ledger.
- [x] **Iteration 8**: Events and markets.
- [x] **Iteration 9**: Single bet.
- [ ] **Iteration 10**: Settlement.
