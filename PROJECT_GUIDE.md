# WealthPulse Project Guide

This guide is the quickest way to understand the repository. It describes
where code belongs and how data moves through the application. Straightforward
methods are intentionally not commented line by line; comments are reserved for
business rules, external-system behavior, and decisions that are not obvious
from the code itself.

## 1. Recommended Reading Order

1. Read `frontend/src/app/App.jsx` to see authentication and page selection.
2. Read `frontend/src/features/assets/hooks/useAssets.js` to see the main
   frontend data flow.
3. Read `backend/.../controller/AssetController.java` to see the REST boundary.
4. Follow it into `AssetManagementService`, then the repositories and models.
5. Read the performance feature from `PortfolioPerformanceController` through
   the four `Portfolio*Service` classes.
6. Use `TESTING.md` to verify behavior before and after a change.

In backend paths below, `backend/...` means
`backend/src/main/java/com/example/wealthpulse`.

## 2. Repository Map

```text
WealthPulse/
├── frontend/                 React application
│   ├── src/app/              Root application composition
│   ├── src/pages/            Page-level components
│   ├── src/features/         Feature-owned UI, hooks, APIs, and data
│   ├── src/shared/           Reusable finance logic and components
│   ├── src/styles/           Global and feature styles
│   └── e2e/                  Playwright browser tests
├── backend/                  Spring Boot REST API
│   ├── src/main/java/        Production Java code
│   ├── src/main/resources/   Runtime configuration
│   └── src/test/             Unit and integration tests
├── Documentation/            Architecture and planning PDFs
├── README.md                 Setup and project summary
├── TESTING.md                Automated and manual test instructions
└── start_all.sh              Starts frontend and backend together
```

## 3. Frontend Flow

```text
main.jsx
  → AuthProvider
  → App.jsx
      → AuthPage when signed out
      → DashboardPage when signed in
          → feature components
          → feature hooks
          → feature API modules
          → shared Axios client
          → Spring Boot /api endpoints
```

Frontend conventions:

- A feature keeps its components, hooks, API functions, constants, and helpers
  under `src/features/<feature>`.
- Cross-feature code belongs in `src/shared` only when it is genuinely reusable.
- Components render UI; hooks coordinate state and side effects; API modules
  describe HTTP calls; finance utilities perform pure calculations.
- The shared client in `src/api/client.js` attaches the JWT and handles expired
  sessions. Feature API modules should use that client instead of Axios directly.
- `App.jsx` owns authentication-level navigation. Dashboard view selection is
  intentionally lightweight and does not use a routing library.

## 4. Backend Flow

```text
HTTP request
  → controller       validates transport input and selects a use case
  → service          applies business rules
  → repository       loads or saves JPA entities
  → PostgreSQL

External market requests
  → AssetService / StockService / market service
  → database-backed cache
  → external provider only when cached data is stale or absent
```

Backend package responsibilities:

| Package | Responsibility |
| --- | --- |
| `controller` | REST paths, request bodies, status codes, and responses |
| `dto` | Validated request and response shapes |
| `service` | Business workflows, calculations, caching, and integrations |
| `repository` | Spring Data database queries |
| `model` | Persisted entities and relationships |
| `security` | JWT creation, authentication filtering, and route policy |
| `config` | Application-wide framework configuration |

Controllers should remain small. They obtain the current user, map the request,
call one service operation, and return the result. Repository calls should stay
inside services unless an endpoint exists only to inspect a repository-backed
system concern, such as cache status.

## 5. Main Business Workflows

### Asset changes

```text
AssetController
  → AssetRequestMapper
  → AssetManagementService
      → AssetValidationService
      → AssetPricingService
      → PortfolioLedgerService
      → AssetRepository
```

`AssetManagementService` owns create, merge, quantity adjustment, import, purity
update, and deletion. Quantity updates are deltas rather than replacement
values. Purchases and sales also write ledger records so performance history can
be calculated later.

### Market pricing

```text
AssetPricingService
  → AssetService cache check
      → MarketCacheRepository
      → StockService or GoldAPI when refresh is needed
```

Do not bypass the cache when adding new pricing behavior. A stale cached value
may be used when a provider is temporarily unavailable.

### Portfolio performance

```text
PortfolioPerformanceController
  → PortfolioPerformanceService     coordinates the use case
      → PortfolioSnapshotService     saves and loads daily values
      → PortfolioMetricsService      calculates ledger totals and gains
      → PortfolioReportService       formats charts, allocation, and benchmark
```

The controller-facing response shape is assembled only by
`PortfolioReportService`. Keep calculation code out of controllers and avoid
putting persistence code in the report builder.

### Authentication

```text
AuthController → AuthenticationManager / JwtService
request → JwtAuthFilter → authenticated User → protected controller
```

`AuthenticatedUserService` is the common way for protected controllers to
obtain the current `User`.

## 6. Configuration

Runtime settings live in `backend/src/main/resources/application.properties`.
Local-only defaults live in `application-dev.properties`. Deployment secrets
must come from environment variables and must never be committed.

The frontend reads `VITE_API_URL`; if it is absent, the API client uses
`http://localhost:8283`.

Important environment variables:

| Variable | Purpose |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection |
| `JWT_SECRET` | JWT signing secret; required outside local development |
| `ALPHA_VANTAGE_KEY` | Stock quote and history provider |
| `GOLD_API_KEY` | Precious-metal price provider |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins |
| `SERVER_PORT` | Backend HTTP port |
| `VITE_API_URL` | Frontend-visible backend origin |

## 7. Commenting and Section Style

- Name sections around responsibilities, not implementation chronology.
- Explain why a rule exists, especially around money, authentication, caching,
  ownership, or external providers.
- Do not restate code such as `// set loading to true` above `setLoading(true)`.
- Keep public API comments accurate. Delete stale comments during refactoring.
- Prefer small named methods over long comment blocks inside a complex method.
- Use consistent section labels such as `// Authentication`, `// Data loading`,
  and `// User actions` only when a file is long enough to benefit from them.

## 8. Safe Change Checklist

Before finishing a change:

1. Confirm user-owned records are still queried by owner ID.
2. Confirm financial inputs handle null, zero, and non-finite values.
3. Confirm market-data failures retain the documented cache fallback.
4. Run the backend and frontend commands in `TESTING.md`.
5. Update this guide if a package responsibility or major flow changes.

If an npm command fails inside `node_modules/.bin` after changing Node versions,
refresh the local dependency installation with `cd frontend && npm ci`, then run
the command again. Generated `node_modules` contents are not project source.
