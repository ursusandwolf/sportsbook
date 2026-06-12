# Multi-Factor Authentication (TOTP) Flow

## 1. MFA Setup Flow (Registration of 2FA)

This flow occurs when a logged-in user decides to enable 2FA.

```mermaid
sequenceDiagram
    participant User
    participant MfaController
    participant MfaService
    participant DB

    User->>MfaController: POST /api/auth/mfa/setup
    MfaController->>MfaService: generateNewSecret()
    MfaService-->>MfaController: Base32 Secret
    MfaController-->>User: HTTP 200 {secret, qrCodeUri}
    Note right of User: User scans QR code in Google Authenticator

    User->>MfaController: POST /api/auth/mfa/enable {secret, code}
    MfaController->>MfaService: verifyCode(secret, code)
    MfaService-->>MfaController: true (valid)
    MfaController->>MfaService: enableMfa(userId, secret)
    MfaService->>DB: Update user (mfa_enabled=true, mfa_secret=...)
    MfaController-->>User: HTTP 200 OK
```

## 2. Two-Step Login Flow

This flow occurs during login if MFA is enabled for the user.

```mermaid
sequenceDiagram
    participant User
    participant AuthController
    participant AuthManager
    participant JwtUtils
    participant MfaService

    User->>AuthController: POST /api/auth/login {email, password}
    AuthController->>AuthManager: authenticate(email, password)
    AuthManager-->>AuthController: Authentication OK
    
    AuthController->>AuthController: Check user.isMfaEnabled()
    Note over AuthController: MFA is ENABLED
    
    AuthController->>JwtUtils: generateMfaToken(email)
    JwtUtils-->>AuthController: short-lived JWT (mfa: true)
    AuthController-->>User: HTTP 202 Accepted {mfaRequired: true, mfaToken: "..."}

    Note right of User: User gets code from Authenticator app

    User->>AuthController: POST /api/auth/verify-2fa {code, mfaToken}
    AuthController->>JwtUtils: validateAndGetClaims(mfaToken)
    JwtUtils-->>AuthController: Claims (email, mfa=true)
    
    AuthController->>MfaService: verifyCode(user.secret, code)
    MfaService-->>AuthController: true (valid)
    
    AuthController->>JwtUtils: generateToken(userDetails)
    JwtUtils-->>AuthController: final Access JWT
    AuthController-->>User: HTTP 200 OK {token: "..."}
```

## 3. Data Model

```mermaid
classDiagram
    class User {
        +String email
        +String passwordHash
        +boolean mfaEnabled
        +String mfaSecret
    }

    class MfaService {
        +generateNewSecret() String
        +verifyCode(secret, code) boolean
        +enableMfa(userId, secret) void
    }

    class JwtUtils {
        +generateMfaToken(email) String
        +generateToken(userDetails) String
        +validateAndGetClaims(token) Claims
    }

    class AuthController {
        +login(LoginRequest) LoginResponse
        +verify2fa(MfaVerificationRequest) LoginResponse
    }

    AuthController ..> MfaService
    AuthController ..> JwtUtils
    MfaService ..> User
```

## Key Security Features
1. **Intermediate Token**: The `mfaToken` is not a valid access token. It only grants access to the `/verify-2fa` endpoint.
2. **Short TTL**: `mfaToken` expires in 5 minutes.
3. **MFA Verification**: MFA is only enabled after the user provides a valid code for the secret (prevents lockout).
