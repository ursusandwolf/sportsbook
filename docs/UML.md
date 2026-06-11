# UML Diagrams

## User & Security Pattern

```mermaid
classDiagram
    class UserDetails {
        <<interface>>
        +getAuthorities()
        +getPassword()
        +getUsername()
    }

    class SecurityUser {
        -User user
        +getAuthorities()
        +getUser()
    }

    class SecurityUtils {
        <<static>>
        +getCurrentUser() Optional~SecurityUser~
    }

    class User {
        -Long id
        -String email
        -String passwordHash
        -UserStatus status
        -Set~Role~ roles
    }

    class Role {
        -Long id
        -RoleName name
    }

    UserDetails <|.. SecurityUser
    SecurityUser o-- User
    User "1" *-- "many" Role
    SecurityUtils ..> SecurityUser
```

## Security Flow

```text
HTTP Request
    ↓
SecurityFilterChain
    ↓
BasicAuthenticationFilter
    ↓
AuthenticationManager
    ↓
CustomUserDetailsService  ───>  UserRepository (PostgreSQL)
    ↓
SecurityUser (Principal)
    ↓
Controller  <───  SecurityUtils
```
