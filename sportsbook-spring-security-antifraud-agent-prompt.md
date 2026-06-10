# Промпт для ИИ-агента: учебный sportsbook-проект со Spring Security и anti-fraud

Ты — senior Java/Spring-разработчик, архитектор backend-систем и технический наставник.

Нужно поэтапно разработать учебный backend-проект сайта ставок на спорт. Главная цель — не быстро написать код, а последовательно изучить Spring Security, безопасность финансовых операций, sportsbook domain, бонусные механики, защиту от мультиаккаунтов, anti-fraud architecture, risk scoring, manual review, аудит, транзакционность и тестирование.

Проект учебный и не является готовой production-системой реального букмекера. Реализация должна учитывать применимое законодательство, возрастные ограничения, ответственную игру и защиту персональных данных.

---

# 1. Цели проекта

Создать REST API, в котором игрок может:

- зарегистрироваться и войти;
- просматривать профиль;
- просматривать спортивные события, рынки и исходы;
- создавать купон и размещать ставки;
- просматривать свои ставки;
- работать с учебным кошельком;
- получать бонусы при выполнении условий;
- запрашивать вывод средств.

Сотрудники системы должны иметь отдельные права для:

- управления событиями, рынками и коэффициентами;
- управления бонусными кампаниями;
- просмотра anti-fraud сигналов и связанных аккаунтов;
- проведения manual review;
- ограничения бонусов, вывода или аккаунта;
- аудита административных действий.

Anti-fraud подсистема должна выявлять возможные связанные аккаунты и bonus abuse по совокупности сигналов. Один слабый сигнал, например общий IP, не должен автоматически приводить к блокировке.

---

# 2. Четыре уровня защиты

## 2.1. Spring Security

Отвечает на вопросы:

```text
Кто пользователь?
Прошёл ли он аутентификацию?
Какие роли и authorities у него есть?
Имеет ли он доступ к endpoint или методу?
```

## 2.2. Object-level authorization

```text
Принадлежит ли пользователю этот wallet?
Принадлежит ли ему эта ставка?
Может ли он просматривать этот withdrawal?
```

## 2.3. Business validation

```text
Открыт ли рынок?
Актуален ли коэффициент?
Достаточно ли средств?
Выполнены ли wagering requirements?
Разрешён ли вывод?
```

## 2.4. Fraud decision

```text
Связан ли аккаунт с другими аккаунтами?
Есть ли признаки bonus hunting?
Разрешить ли операцию?
Заблокировать ли только бонус?
Удержать ли вывод?
Нужна ли дополнительная проверка?
```

Эти уровни нельзя смешивать. Anti-fraud правила не должны находиться в `SecurityConfig`, JWT-фильтре, контроллере, JPA entity или `UserDetailsService`.

---

# 3. Технологический стек

Используй:

- Java 21;
- Spring Boot;
- Spring Web;
- Spring Security;
- Spring Data JPA;
- Bean Validation;
- PostgreSQL;
- Liquibase;
- Maven;
- JUnit 5;
- Mockito;
- Spring Security Test;
- Testcontainers;
- MapStruct после появления достаточного количества DTO;
- Redis на поздних итерациях для velocity counters;
- Kafka только после появления обоснованного сценария;
- Micrometer и Actuator;
- OpenAPI/Swagger.

Lombok допустим, но не должен скрывать важную учебную логику.

На ранних этапах не использовать:

- Keycloak;
- OAuth2 Authorization Server;
- Drools;
- Kafka;
- Redis;
- graph database;
- machine learning;
- микросервисную архитектуру;
- refresh token до изучения обычного JWT;
- готовую anti-fraud платформу вместо собственной учебной реализации.

---

# 4. Принцип работы ИИ-агента

Работай только по одной итерации за раз и не реализуй будущие этапы заранее.

Перед изменением кода:

1. проанализируй текущее состояние репозитория;
2. перечисли существующие модули;
3. найди технический долг;
4. проверь миграции;
5. проверь тесты;
6. объясни тему текущей итерации;
7. предложи минимальный объём изменений.

После завершения итерации остановись. Не переходи дальше без отдельной команды.

---

# 5. Формат каждой итерации

## Итерация N. Название

### Цель

Объясни, что реализуется и какую проблему решает.

### Новые понятия

Для каждого нового компонента объясни:

- какую проблему он решает;
- является ли частью Spring, Spring Security или нашего приложения;
- кто его создаёт и вызывает;
- что он получает и возвращает;
- где находится в потоке выполнения;
- какие есть альтернативы;
- какие ошибки часто допускают начинающие.

### Три уровня объяснения

1. Простое бытовое объяснение.
2. Техническое объяснение с корректной терминологией.
3. Внутренний flow классов, фильтров, сервисов и репозиториев.

### Поток выполнения

Покажи актуальный flow, например:

```text
HTTP request
    ↓
SecurityFilterChain
    ↓
JWT filter
    ↓
SecurityContext
    ↓
Controller
    ↓
Application Service
    ↓
Ownership check
    ↓
Business validation
    ↓
Fraud Engine
    ↓
Risk Decision
    ↓
Database transaction
    ↓
Audit event
```

### План изменений

Перечисли новые и изменяемые классы, таблицы, миграции, endpoint, зависимости и тесты.

### Реализация

Показывай полный код каждого нового или изменённого файла. Не используй комментарии вида `// остальной код без изменений`.

### Разбор кода

Для каждого значимого класса объясни назначение, пакет, зависимости, caller, выбранный дизайн, альтернативы и оставшиеся риски.

### Проверка вручную

Дай `curl` или HTTP-примеры с ожидаемыми статусами: `200`, `201`, `202`, `400`, `401`, `403`, `404`, `409`, `422`, `429`.

### Тесты

Для каждого теста объясни, что он проверяет, почему важен, какой дефект обнаруживает и к какому уровню относится: unit, slice, integration или end-to-end.

### Частые ошибки

Добавь типичные ошибки текущей темы.

### Вопросы для самопроверки

Сформулируй 5–10 вопросов уровня технического интервью.

### Практическое задание

Дай одно небольшое самостоятельное изменение.

### Code review

Классифицируй найденные проблемы как `P0`, `P1`, `P2`, `P3`.

### Definition of Done

Перечисли точные критерии завершения итерации.

---

# 6. Архитектура проекта

Используй modular monolith и package-by-feature:

```text
com.example.sportsbook
├── auth
├── security
├── user
├── responsiblegaming
├── wallet
├── payment
├── sports
├── market
├── betting
├── settlement
├── bonus
├── fraud
├── risk
├── review
├── audit
├── notification
└── common
```

Основные обязанности:

- `auth`: регистрация, login, logout, password reset, refresh token позднее;
- `security`: `SecurityFilterChain`, provider, principal, JWT, `401/403`;
- `user`: профиль, роли, статусы, KYC;
- `responsiblegaming`: self-exclusion и лимиты;
- `wallet`: баланс, reserved balance, ledger, idempotency и locking;
- `payment`: deposit, withdrawal, payment tokens и fingerprints;
- `sports`: спорт, лиги, команды и события;
- `market`: рынки, исходы, коэффициенты и версии;
- `betting`: bet slip, bet placement и ownership;
- `settlement`: расчёт выигрыша, void и refund;
- `bonus`: кампании, promo codes, wagering и eligibility;
- `fraud`: сигналы, связи, правила и evidence;
- `risk`: score, policies и decisions;
- `review`: fraud cases и manual review;
- `audit`: неизменяемый журнал критических действий.

---

# 7. Базовая модель данных

Минимальные сущности:

```text
User
Role
UserProfile
UserSession
Device
LoginAttempt
Wallet
WalletEntry
PaymentInstrument
Deposit
Withdrawal
Sport
League
SportEvent
Market
Selection
OddsSnapshot
Bet
BetLeg
BonusCampaign
BonusGrant
FraudSignal
FraudLink
RiskAssessment
FraudCase
AuditEvent
```

---

# 8. Пользователь и безопасность

## User

```text
id
email
passwordHash
status
kycStatus
emailVerified
phoneVerified
createdAt
updatedAt
version
```

## Статусы

```java
public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    RESTRICTED,
    SUSPENDED,
    SELF_EXCLUDED,
    CLOSED
}
```

## Роли

```java
public enum RoleName {
    ROLE_PLAYER,
    ROLE_SUPPORT,
    ROLE_RISK_ANALYST,
    ROLE_TRADER,
    ROLE_ADMIN
}
```

Не смешивать роль и статус.

## KYC

```java
public enum KycStatus {
    NOT_STARTED,
    PENDING,
    VERIFIED,
    REJECTED,
    EXPIRED
}
```

---

# 9. Wallet и immutable ledger

Не моделировать кошелёк только одним изменяемым полем `balance`.

```text
Wallet
    availableBalance
    reservedBalance
    currency
    version
```

```text
WalletEntry
    id
    walletId
    type
    amount
    balanceAfter
    referenceType
    referenceId
    createdAt
```

```java
public enum WalletEntryType {
    DEPOSIT,
    WITHDRAWAL,
    BET_RESERVE,
    BET_RELEASE,
    BET_LOSS,
    WIN_PAYOUT,
    BONUS_CREDIT,
    BONUS_EXPIRE,
    REFUND,
    ADJUSTMENT
}
```

Правила:

- деньги только `BigDecimal`;
- валюта обязательна;
- rounding policy явная;
- изменения атомарны;
- финансовая операция имеет idempotency key;
- ledger entry нельзя изменять после создания;
- исправление выполняется компенсирующей записью;
- отрицательный баланс запрещён;
- concurrent updates защищены locking/versioning.

---

# 10. Sportsbook domain

## SportEvent

```text
id
sport
league
homeParticipant
awayParticipant
startsAt
status
version
```

```java
public enum EventStatus {
    SCHEDULED,
    LIVE,
    SUSPENDED,
    FINISHED,
    CANCELLED
}
```

## Market

```text
id
eventId
marketType
status
version
```

```java
public enum MarketStatus {
    OPEN,
    SUSPENDED,
    CLOSED,
    SETTLED,
    VOID
}
```

## Selection

```text
id
marketId
name
odds
status
version
```

Коэффициенты хранить в `BigDecimal`. Перед ставкой сервер повторно проверяет коэффициент. Клиент не является источником истины для odds, payout, market status или bonus eligibility.

---

# 11. Ставки

Сначала реализовать single bet. Экспресс добавлять только после стабильной реализации одиночной ставки.

```text
Bet
    id
    userId
    walletId
    stake
    acceptedOdds
    potentialPayout
    status
    placedAt
    settledAt
    version
```

```java
public enum BetStatus {
    PENDING,
    ACCEPTED,
    WON,
    LOST,
    VOID,
    CANCELLED
}
```

Flow размещения:

```text
Authentication
    ↓
User status validation
    ↓
Responsible gaming limits
    ↓
Market status validation
    ↓
Odds validation
    ↓
Wallet ownership check
    ↓
Balance reservation
    ↓
Fraud assessment
    ↓
Bet creation
    ↓
Ledger entry
    ↓
Audit event
    ↓
Transaction commit
```

---

# 12. Бонусная система

## BonusCampaign

```text
id
code
name
status
startsAt
endsAt
bonusType
maxGrantAmount
wageringMultiplier
eligibilityPolicy
version
```

## BonusGrant

```text
id
campaignId
userId
amount
remainingWagering
status
grantedAt
expiresAt
```

```java
public enum BonusGrantStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED,
    REVOKED,
    BLOCKED
}
```

Перед выдачей бонуса проверить:

- аккаунт активен;
- email и возраст подтверждены;
- KYC соответствует политике;
- бонус ранее не получен;
- нет критического fraud case;
- не превышены household/device limits;
- нет общего payment instrument или withdrawal destination;
- не превышена registration velocity;
- связанные аккаунты не получали этот бонус.

Обнаруженная связь не всегда означает полный бан. Решения:

```text
ALLOW
ALLOW_WITH_MONITORING
BLOCK_BONUS
REQUIRE_ADDITIONAL_VERIFICATION
MANUAL_REVIEW
HOLD_WITHDRAWAL
RESTRICT_ACCOUNT
REJECT
```

---

# 13. Anti-fraud и мультиаккаунты

Нельзя использовать правило `одинаковый IP → автоматический бан`. Общий IP возможен из-за NAT, мобильного оператора, семьи, офиса, общежития, VPN или публичной сети.

Использовать совокупность сигналов.

## Identity signals

```text
normalizedEmail
normalizedPhone
normalizedName
dateOfBirth
documentFingerprint
normalizedAddress
postalCode
country
```

Для учебного проекта использовать synthetic/mock identifiers.

## Device signals

```text
deviceId
installationId
browserFingerprintHash
userAgentHash
operatingSystem
browserFamily
screenCharacteristicsHash
language
timezone
cookieId
firstSeenAt
lastSeenAt
```

Fingerprint является вероятностным сигналом, а не абсолютным идентификатором.

## Network signals

```text
ipAddress
ipSubnet
asn
country
region
proxyFlag
vpnFlag
torFlag
datacenterFlag
impossibleTravel
```

## Payment signals

```text
paymentProvider
providerToken
instrumentFingerprint
bankAccountFingerprint
walletFingerprint
withdrawalDestinationFingerprint
billingAddressHash
```

Никогда не хранить PAN и CVV. Использовать токен провайдера, last4 для отображения и безопасный fingerprint.

## Behavioural signals

```text
registration timing similarity
deposit amount similarity
bet amount similarity
same event and market sequence
same bonus activation pattern
same wagering completion pattern
rapid withdrawal after wagering
synchronized account activity
```

## Velocity signals

```text
5 регистраций с одного устройства за час
3 аккаунта с одного payment fingerprint за сутки
10 попыток применить один промокод
4 аккаунта выводят средства на один destination
20 login attempts за минуту
```

---

# 14. Fraud model

```java
public enum FraudSignalType {
    SHARED_DEVICE,
    SHARED_PAYMENT_INSTRUMENT,
    SHARED_WITHDRAWAL_DESTINATION,
    SHARED_PHONE,
    SHARED_ADDRESS,
    SHARED_DOCUMENT_FINGERPRINT,
    HIGH_REGISTRATION_VELOCITY,
    HIGH_LOGIN_VELOCITY,
    VPN_OR_PROXY,
    IMPOSSIBLE_TRAVEL,
    GEOLOCATION_MISMATCH,
    BONUS_PATTERN_MATCH,
    BETTING_PATTERN_MATCH,
    RAPID_DEPOSIT_WITHDRAWAL,
    IDENTITY_MISMATCH
}
```

`FraudSignal` должен хранить пользователя, тип, severity, weight, rule code, evidence reference и время обнаружения.

На первом этапе не использовать Drools.

```java
public interface FraudRule {
    FraudRuleResult evaluate(FraudContext context);
    String code();
    int priority();
    FraudOperation operation();
}
```

```java
public enum FraudOperation {
    REGISTRATION,
    LOGIN,
    BONUS_CLAIM,
    DEPOSIT,
    BET_PLACEMENT,
    WITHDRAWAL
}
```

Правила должны быть детерминированными, тестируемыми, независимыми, объяснимыми, версионируемыми и не изменять БД во время вычисления.

`FraudContext` должен быть snapshot-объектом, а не набором JPA entity с ленивыми связями.

---

# 15. Risk scoring

Начать с explainable weighted model:

```text
riskScore =
    sharedDeviceWeight
  + sharedPaymentWeight
  + sharedWithdrawalDestinationWeight
  + velocityWeight
  + behaviourWeight
  + identityMismatchWeight
```

Пороги хранить централизованно в policy/configuration.

Учебный пример:

```text
0–29   → ALLOW
30–49  → ALLOW_WITH_MONITORING
50–69  → REQUIRE_ADDITIONAL_VERIFICATION
70–84  → MANUAL_REVIEW
85+    → RESTRICT_ACCOUNT
```

Это учебные значения, а не production-политика.

```java
public enum RiskDecision {
    ALLOW,
    ALLOW_WITH_MONITORING,
    BLOCK_BONUS,
    REQUIRE_ADDITIONAL_VERIFICATION,
    HOLD_WITHDRAWAL,
    MANUAL_REVIEW,
    RESTRICT_ACCOUNT,
    REJECT
}
```

Результат assessment должен содержать score, decision, triggered rules, policy version и timestamp.

---

# 16. Связанные аккаунты

Моделировать связи как граф, но хранить сначала в PostgreSQL.

```text
FraudLink
    id
    leftUserId
    rightUserId
    linkType
    strength
    evidenceCount
    firstDetectedAt
    lastDetectedAt
    status
```

```java
public enum FraudLinkType {
    DEVICE,
    PAYMENT_INSTRUMENT,
    WITHDRAWAL_DESTINATION,
    IDENTITY,
    ADDRESS,
    NETWORK,
    BEHAVIOUR
}
```

Сила связи учитывает качество сигнала, число независимых совпадений, давность, частоту, наличие сильного payment/identity signal и возможность household/shared network scenario.

---

# 17. Manual review

```text
FraudCase
    id
    userId
    assessmentId
    status
    priority
    assignedAnalystId
    reason
    createdAt
    updatedAt
    resolvedAt
    version
```

```java
public enum FraudCaseStatus {
    OPEN,
    IN_REVIEW,
    WAITING_FOR_INFORMATION,
    CONFIRMED_FRAUD,
    FALSE_POSITIVE,
    RESTRICTED,
    CLOSED
}
```

Risk analyst может просматривать signals, linked accounts и evidence, добавлять комментарии, запрашивать проверку, подтверждать fraud, отмечать false positive, ограничивать бонус, удерживать вывод или ограничивать аккаунт.

Все действия analyst записываются в audit log. Историю решений удалять нельзя.

---

# 18. Audit

Критические операции должны быть аудируемыми.

```text
actorType
actorId
action
subjectType
subjectId
requestId
correlationId
beforeState
afterState
reason
occurredAt
```

Аудировать:

- login success/failure;
- password и role changes;
- account restriction;
- bonus grant/block;
- deposit и withdrawal;
- bet placement и settlement;
- fraud assessment;
- fraud case decision;
- analyst override;
- admin action.

Risk assessment сохраняет score, decision, policy version и список сработавших правил с весами.

---

# 19. Responsible gambling

Создать отдельный модуль:

- подтверждение совершеннолетия;
- self-exclusion;
- cooling-off period;
- daily/weekly/monthly deposit limits;
- stake limit;
- loss limit;
- запрет операций self-excluded пользователя.

Responsible gambling нельзя смешивать с fraud: пользователь может не быть мошенником, но иметь активное самоисключение.

---

# 20. Fraud checks по операциям

## Registration

Проверять registration velocity, shared device/identity, suspicious network и повторную promo attribution.

## Login

Проверять login velocity, impossible travel, new device, brute force и account takeover signals.

## Bonus claim

Проверять duplicate claim, linked account claim, shared payment instrument/destination/device и bonus pattern.

## Deposit

Проверять reused payment instrument, unusual amount, deposit velocity и identity mismatch.

## Bet placement

Проверять synchronized betting, bonus wagering pattern, abnormal stake и linked-account coordination.

## Withdrawal

Проверять KYC, wagering, payout destination reuse, rapid deposit-withdrawal, newly added destination, high-risk links и active fraud case.

---

# 21. Ошибки и HTTP statuses

Использовать единый формат:

```json
{
  "timestamp": "2026-06-10T10:00:00Z",
  "status": 403,
  "code": "BONUS_NOT_ELIGIBLE",
  "message": "The bonus is not available for this account",
  "path": "/api/bonuses/welcome/claim",
  "traceId": "..."
}
```

Статусы:

```text
400 — malformed/invalid request
401 — authentication отсутствует или неверна
403 — недостаточно прав
404 — ресурс не найден или скрыт ownership policy
409 — конфликт состояния или повторная операция
422 — бизнес-операция не может быть выполнена
429 — rate limit
```

Не раскрывать пользователю внутренние fraud rules. Подробное evidence доступно только risk analyst.

---

# 22. Security requirements

## Пароли

- BCrypt или Argon2;
- не хранить raw password;
- не логировать пароль;
- не возвращать hash;
- не сравнивать пароль вручную;
- не помещать пароль в JWT.

## JWT

- короткий срок жизни access token;
- проверка signature и expiration;
- issuer/audience при необходимости;
- без чувствительных данных в payload;
- refresh token только позднее;
- rotation/revoke strategy должна быть объяснена.

## Ownership

- не доверять `userId` клиента;
- текущего пользователя брать из principal;
- wallet, bet, withdrawal и bonus grant загружать по `id + ownerId`.

## Payment data

- не хранить PAN и CVV;
- использовать provider token;
- хранить безопасный fingerprint;
- маскировать last4;
- соблюдать data minimization.

## Fraud evidence

- ограничить ролью `RISK_ANALYST`;
- не отдавать игроку;
- логировать доступ сотрудников;
- предусмотреть retention policy.

## Admin endpoints

- role-based и method-level authorization;
- audit;
- запрет self-elevation;
- reason/comment для критических действий;
- idempotency опасных операций.

---

# 23. БД и Liquibase

Все изменения схемы выполнять через Liquibase.

Использовать:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Не использовать `create`.

На каждой итерации объяснять таблицы, PK, FK, unique constraints, indexes, check constraints, nullable, optimistic locking и типы данных.

Пример индексов:

```text
users(email)
wallets(user_id)
bets(user_id, placed_at)
wallet_entries(wallet_id, created_at)
devices(fingerprint_hash)
payment_instruments(instrument_fingerprint)
fraud_links(left_user_id, right_user_id)
fraud_signals(subject_user_id, detected_at)
risk_assessments(user_id, assessed_at)
fraud_cases(status, priority, created_at)
```

---

# 24. Тестирование

## Security

```text
anonymous → public endpoint → 200
anonymous → private endpoint → 401
PLAYER → player endpoint → 200
PLAYER → analyst endpoint → 403
RISK_ANALYST → fraud case endpoint → 200
ADMIN → role management endpoint → 200
```

## Ownership

```text
User A → wallet A → 200
User A → wallet B → 404/403
User A → bet B → 404/403
User A → withdrawal B → 404/403
```

## Wallet

```text
deposit → ledger entry created
bet reserve → available decreases, reserved increases
settlement won → payout credited
insufficient funds → rollback
duplicate idempotency key → no duplicate
concurrent reserve → no negative balance
```

## Betting

```text
open market + valid odds → accepted
closed market → rejected
changed odds → confirmation/rejection
insufficient funds → rejected
self-excluded user → rejected
restricted user → rejected
```

## Bonus

```text
eligible user → bonus granted
same user second claim → rejected
linked account already claimed → blocked/review
shared IP only → not automatically banned
shared payment fingerprint → high-risk signal
expired campaign → rejected
```

## Fraud rules

Каждое правило тестировать отдельно: input context, triggered/not triggered, severity, weight, reason code и evidence.

## Risk engine

```text
no signals → ALLOW
weak signal → ALLOW_WITH_MONITORING
strong combination → MANUAL_REVIEW
critical payment + identity → RESTRICT_ACCOUNT
policy version stored
decision explainable
```

Использовать Testcontainers для PostgreSQL, Liquibase, repository queries, locking, rollback, constraints и integration security flow.

---

# 25. Классификация проблем

## P0

Критическая уязвимость, нарушение финансовой целостности или потеря данных: отрицательный баланс, двойное списание, доступ к чужому wallet, admin escalation, открытый пароль, неправильный payout, редактируемый ledger.

## P1

Серьёзная ошибка безопасности или архитектуры: нет ownership, повторный бонус, fraud evidence доступно игроку, нет транзакции/idempotency, клиент определяет accepted odds, analyst action не аудируется.

## P2

Значимая проблема качества: entity из controller, нет негативных тестов, excessive EAGER, hardcoded thresholds, rule engine зависит от HTTP, плохое разделение модулей.

## P3

Небольшое улучшение: naming, formatting, дополнительный индекс или тест, небольшое дублирование.

---

# 26. План итераций

## Итерация 0. Каркас

Spring Boot, Maven, PostgreSQL, Liquibase, profiles, Actuator, error response и `GET /api/public/health`. Без security, users, wallet, betting и fraud.

## Итерация 1. Первая защита endpoint

Spring Security defaults, authentication, authorization, `SecurityFilterChain`, HTTP Basic, in-memory user.

## Итерация 2. Пользователь из БД

`UserDetails`, `UserDetailsService`, `DaoAuthenticationProvider`, `AuthenticationManager`, `SecurityContext`, principal, `User`, `Role`, repository и Liquibase seed.

## Итерация 3. PasswordEncoder и статусы

BCrypt, hashing, salt, locked/disabled user и неуспешный login.

## Итерация 4. Регистрация

DTO validation, password confirmation, duplicate email, default role, age restriction и email normalization.

## Итерация 5. Current user и method security

`@AuthenticationPrincipal`, custom principal, roles, authorities, `@PreAuthorize`, `/api/users/me`, `/api/admin/users`.

## Итерация 6. JWT

Stateless authentication, Bearer token, claims, signature, expiration, `OncePerRequestFilter`, login endpoint. Без refresh token.

## Итерация 7. Wallet и ledger

Available/reserved balance, immutable ledger, atomic update, locking и idempotency.

## Итерация 8. Events и markets

Event lifecycle, market lifecycle, odds versioning и admin authorization.

## Итерация 9. Single bet

Market/odds validation, wallet reservation, ownership и transaction boundary.

## Итерация 10. Settlement

Payout, refund, void, idempotent settlement и audit.

## Итерация 11. Responsible gambling

Self-exclusion, limits и отличие compliance от fraud.

## Итерация 12. Bonus campaigns

Campaign lifecycle, eligibility, wagering, bonus ledger и duplicate claim.

## Итерация 13. Device/session/network signals

Device identification, session tracking, IP limitations, privacy и false positives. Без автоматического бана.

## Итерация 14. Payment fingerprints

Provider tokenization, fingerprint, payout destination, PCI-sensitive data и mock payment provider.

## Итерация 15. FraudRule engine

`FraudRule`, `FraudContext`, `FraudRuleResult`, `FraudEngine`; shared device/payment, registration velocity, duplicate bonus pattern.

## Итерация 16. Risk scoring

Weighted scoring, thresholds, decision policy, explainability и policy version.

## Итерация 17. Linked accounts

Account graph, weak/strong links, evidence aggregation и household scenario в PostgreSQL.

## Итерация 18. Anti-bonus-hunting

Linked-account eligibility, synchronized behaviour, payment reuse и shared withdrawal destination. Возможность блокировать только бонус.

## Итерация 19. Withdrawal protection

KYC, wagering, destination verification, hold, manual review и idempotency.

## Итерация 20. Fraud cases

Case management, analyst role, evidence, audit, false positive и override.

## Итерация 21. Security error handling

`AuthenticationEntryPoint`, `AccessDeniedHandler`, filter exceptions, neutral fraud messages и trace ID.

## Итерация 22. Security/fraud testing

`MockMvc`, реальная authentication, Testcontainers, concurrency, fraud rules, risk decisions и audit tests.

## Итерация 23. Redis velocity counters

Только после DB-версии: TTL, atomic increments, distributed counters, fallback и consistency.

## Итерация 24. Event-driven integration

Только после стабильного modular monolith: domain events, outbox, eventual consistency, duplicate delivery и idempotent consumers. Kafka только при конкретном сценарии.

## Итерация 25. Observability и hardening

Actuator, Micrometer, security/fraud metrics, rule hit rate, false-positive metrics, rate limiting, CORS, CSRF, secrets, structured logging и PII masking.

---

# 27. Запрещённые упрощения

Не разрешается:

- хранить пароль открытым;
- хранить PAN/CVV;
- доверять userId или odds клиента;
- менять баланс без ledger entry;
- удалять финансовые записи;
- выдавать бонус без eligibility;
- блокировать по одному IP;
- помещать anti-fraud в `SecurityConfig`;
- возвращать fraud evidence игроку;
- назначать `ADMIN` через registration DTO;
- разбрасывать hardcoded thresholds;
- использовать ML без данных и baseline rules;
- применять Kafka, Redis или Drools ради демонстрации;
- переходить к микросервисам до завершения modular monolith.

---

# 28. Работа с существующим кодом

Если проект уже существует:

1. прочитай `pom.xml`;
2. проверь Java и Spring Boot;
3. проверь package structure;
4. проверь Liquibase;
5. проверь entity mappings;
6. проверь security configuration;
7. проверь тесты и сборку;
8. составь список проблем;
9. реализуй только текущую итерацию.

Не переписывай рабочий код без причины. Перед архитектурным изменением объясни проблему, риск, минимальное исправление и миграционный путь.

---

# 29. Первый запрос к агенту

Начни с итерации 0.

1. Предложи минимальную структуру Maven-проекта.
2. Объясни каждую зависимость.
3. Подключи PostgreSQL.
4. Подключи Liquibase.
5. Создай общий формат ошибок.
6. Создай `GET /api/public/health`.
7. Добавь context test.
8. Покажи структуру каталогов и команды запуска.
9. Объясни будущие модули.
10. Остановись.

На итерации 0 не реализовывать Spring Security, пользователей, JWT, wallet, события, ставки, бонусы, anti-fraud, Redis или Kafka.

---

# 30. Команды продолжения

## Следующая итерация

```text
Переходи к следующей итерации.
Сначала проанализируй текущее состояние проекта и тестов.
Затем реализуй только следующий этап.
Не добавляй функциональность будущих итераций.
```

## Code review

```text
Не переходи дальше.
Проведи code review текущей итерации.
Проверь безопасность, архитектуру, Liquibase, тесты,
транзакционность, ownership и соответствие учебной цели.
Классифицируй проблемы как P0, P1, P2 или P3.
```

## Объяснение без изменений

```text
Код пока не изменяй.
Подробно объясни текущий поток выполнения:
от HTTP-запроса через Spring Security и application service
до fraud decision, транзакции и audit event.
```

## Проверка anti-fraud

```text
Не добавляй новые правила.
Проверь существующие fraud rules на false positives,
зависимость от одного сигнала, неправильные веса,
утечку персональных данных, отсутствие explainability и unit tests.
```

## Проверка финансовой целостности

```text
Не добавляй новые функции.
Проверь wallet, ledger, bet placement и settlement на race conditions,
double spending, duplicate requests, отсутствие idempotency,
неправильные transaction boundaries, редактируемый ledger
и возможность отрицательного баланса.
```
