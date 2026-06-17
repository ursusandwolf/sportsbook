# Changelog

## [Iteration 13] - 2026-06-17
### Added
- Device Identification module for fraud detection infrastructure.
- `Device` and `UserDeviceLink` entities and repositories.
- `DeviceService` to extract device fingerprint (User-Agent, Accept-Language, IP).
- `DeviceIdentificationFilter` to automatically capture device signals on authenticated requests.
- Liquibase migration adding `devices` and `user_device_links` tables.
- `DeviceIntegrationTests` to verify device signal collection.

### Changed
- `SecurityConfig` updated to include `DeviceIdentificationFilter` after `JwtAuthenticationFilter`.


## [Iteration 10] - 2026-06-17
### Added
- `settlement` module for bet outcome management.
- `SettlementService` with support for WON, LOST, VOID, and CANCELLED statuses.
- `SettlementController` for admin/trader settlement operations.
- `audit` module for immutable event logging.
- `AuditService` and `AuditEvent` entity with persistence.
- Liquibase migrations for `settled_at` column in `bets` and `audit_events` table.
- New integration tests in `SettlementIntegrationTests`.

### Changed
- `WalletService` updated with idempotent `settleWin`, `settleLoss`, and `settleRefund` methods.
- `WalletService.execute` modified to allow zero amounts (required for `BET_LOSS`).
- `SecurityUser` updated with `getId()` for easier access to user identity in audit logs.
- `Bet` entity updated with `settledAt` field.

## [Iteration 9] - 2026-06-17
### Added
- `betting` module for single bet placement.
- `Bet` entity with status tracking (ACCEPTED, PENDING, etc.).
- `BetService` coordinating bet placement with wallet reservation.
- `BetController` for player-facing betting API.
- Liquibase migration for `bets` table with financial constraints.
- `BettingIntegrationTests` verifying placement, validation, and fund reservation.

### Changed
- `WalletService` added `reserveFunds` method with optimistic locking.
- `GlobalExceptionHandler` updated to map `IllegalStateException` to HTTP 422.

## [Iteration 8] - 2026-06-17
### Added
- `sports` module: `Sport`, `SportEvent` entities and management APIs.
- `market` module: `Market`, `Selection` entities and odds management.
- Liquibase migrations for sports, events, markets, and selections.
- Integration tests for sports and market management.

## [Iteration 7] - 2026-06-17
### Added
- `wallet` module: `Wallet` and `WalletEntry` (ledger).
- Immutable ledger pattern for financial operations.
- Optimistic locking on `Wallet` to prevent race conditions.
- Idempotency support for wallet operations.
- Integration tests for deposits and concurrent reservations.

## [Iteration 6.5] - 2026-06-17
### Added
- MFA integration: TOTP (Google Authenticator) support.
- Two-step login flow: credentials -> 202 Accepted + mfaToken -> verify TOTP -> final JWT.
- `MfaService` for secret generation and verification.
- `MfaIntegrationTests`.

## [Iteration 6] - 2026-06-17
### Added
- JWT authentication: Stateless login with `Bearer` tokens.
- `JwtUtils`, `JwtAuthenticationFilter`, and login endpoint.
- `SportsbookApplicationTests` verified context loading.

## [Iteration 5] - 2026-06-17
### Added
- `@PreAuthorize` on controller methods.
- `SecurityUtils` for accessing current user principal.
- `/api/users/me` and `/api/admin/users` endpoints.

## [Iteration 4] - 2026-06-17
### Added
- `RegistrationController` and `RegistrationRequest` DTO.
- `UserService` for handling user creation and validation.
- Liquibase migration 001 for users and roles.

## [Iteration 3] - 2026-06-17
### Added
- `BCryptPasswordEncoder` for secure password storage.
- User Status checks in `SecurityUser`.

## [Iteration 2] - 2026-06-17
### Added
- JPA-backed `UserDetailsService`.
- `User`, `Role` entities.

## [Iteration 1] - 2026-06-17
### Added
- Basic `SecurityFilterChain` with HTTP Basic.

## [Iteration 0] - 2026-06-17
### Added
- Initial project structure.
- Health check endpoint.
