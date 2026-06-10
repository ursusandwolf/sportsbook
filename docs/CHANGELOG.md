# Changelog

## [0.0.1] - 2026-06-11

### Added
- **Iteration 0**: Project skeleton with Spring Boot, Maven, PostgreSQL, and Liquibase. Added `GET /api/public/health`.
- **Iteration 1**: Initial Spring Security setup with HTTP Basic and in-memory users (`player`, `support`).
- **Iteration 2**: Dynamic user management. Persistent `User` and `Role` entities in PostgreSQL. Custom `UserDetailsService` to load users from the database. Added seed data via Liquibase.
