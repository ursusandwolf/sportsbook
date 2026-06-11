# Project Context: Sportsbook

## Status
- **Current Phase**: Iteration 6 Complete
- **Last Update**: 2026-06-11

## Project Goal
Create a learning-focused Sportsbook backend with a strong emphasis on Spring Security, financial integrity, and anti-fraud mechanisms.

## System State
- **Core**: Spring Boot 3.4.0, Java 21.
- **Database**: PostgreSQL (Production), H2 (Test). Migrations managed by Liquibase.
- **Security**: 
    - Layer 2.1 implemented.
    - Authentication: JWT (JSON Web Token).
    - User Store: Database-backed (JPA + CustomUserDetailsService).
    - Roles: ROLE_PLAYER, ROLE_SUPPORT, ROLE_RISK_ANALYST, ROLE_TRADER, ROLE_ADMIN.
- **Modules**: 
    - `common`: Health, Exception handling.
    - `user`: Entities, Repositories, Profile API.
    - `security`: SecurityFilterChain, UserDetails implementation, JWT.
    - `auth`: Registration, Login.

## Roadmap & Pending Items
- [ ] **Iteration 3**: PasswordEncoder hardening and User Statuses (Locked/Disabled).
- [ ] **Iteration 4**: User Registration with validation.
- [x] **Iteration 5**: Current user context and method-level security.
- [x] **Iteration 6**: JWT implementation.
- [ ] **Iteration 7**: Wallet and immutable ledger.
