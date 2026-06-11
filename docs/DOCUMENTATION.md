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

## Database Schema
- `users`: Core user data, password hashes, and statuses.
- `roles`: Authority names (ROLE_PLAYER, etc.).
- `user_roles`: Many-to-many join table.
