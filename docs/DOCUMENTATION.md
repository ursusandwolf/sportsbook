# Technical Documentation

## Architecture
The project follows a **Modular Monolith** pattern with **package-by-feature** structure.

### Security Layers
1. **Spring Security**: Authentication and URL/Method authorization (Current Focus).
2. **Object-level Authorization**: Ownership checks (Upcoming).
3. **Business Validation**: Rules and limits (Upcoming).
4. **Fraud Decision**: Anti-fraud engine (Upcoming).

## Modules

### `common`
- **Error Handling**: `GlobalExceptionHandler` provides a standardized `ErrorResponse` JSON.
- **Health**: `/api/public/health` for basic liveness check.

### `user`
- **Entities**: `User`, `Role`.
- **Statuses**: `UserStatus` (ACTIVE, SUSPENDED, etc.), `KycStatus`.
- **Persistence**: `UserRepository` (Spring Data JPA).

### `security`
- **Authentication**: JWT (JSON Web Token) (Layer 2.1). Stateless sessions.
- **JWT**: `JwtUtils` for generation/validation, `JwtAuthenticationFilter` for request processing.
- **UserDetails**: `SecurityUser` wraps the `User` entity to bridge with Spring Security.
- **Service**: `CustomUserDetailsService` loads users by email.
- **Utils**: `SecurityUtils` provides static access to the currently authenticated `SecurityUser`.
- **Method Security**: `@EnableMethodSecurity` enabled; `@PreAuthorize` used for role-based endpoint protection.

### `auth`
- **Endpoints**: `/api/auth/login` for JWT issuance, `/api/auth/register` for new user creation.

### `wallet`
- **Wallet**: Stores current `availableBalance` and `reservedBalance`. Uses `@Version` for optimistic locking.
- **WalletEntry**: A ledger entry. Once created, it cannot be modified or deleted.
- **Idempotency**: All financial operations require an `idempotencyKey` to prevent duplicate processing.
- **Atomic Operations**: `WalletService` ensures atomicity between balance updates and ledger creation.

### `sports` & `market`
- **Sport**: High-level category (e.g., Football, Basketball).
- **SportEvent**: A specific match. Tracks participants and start time.
- **Market**: A betting opportunity (e.g., "Match Winner"). Linked to an event.
- **Selection**: A specific outcome (e.g., "Home Win"). Contains the `odds`.
- **EventStatus**: `SCHEDULED`, `LIVE`, `SUSPENDED`, `FINISHED`, `CANCELLED`.
- **MarketStatus**: `OPEN`, `SUSPENDED`, `CLOSED`, `SETTLED`, `VOID`.
- **Security**: Public read access to the line; restricted write access for `ROLE_TRADER` and `ROLE_ADMIN`.

## Database Schema
- `users`: Core user data, password hashes, and statuses.
- `roles`: Authority names (ROLE_PLAYER, etc.).
- `user_roles`: Many-to-many join table.
- `wallets`: Available and reserved balances for players.
- `wallet_entries`: Immutable ledger for financial auditing.
- `sports`: Sport categories.
- `sport_events`: Match data and status.
- `markets`: Betting markets for events.
- `selections`: Possible outcomes and odds.
