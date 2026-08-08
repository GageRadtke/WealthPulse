# WealthPulse

> A portfolio and precious-metals tracking application

WealthPulse combines traditional investment tracking with the physical-weight,
purity, spot-price, and melt-value calculations needed by precious-metals
collectors. It provides authenticated, user-specific portfolios, market-based
valuations, performance analytics, financial goals, and an educational learning
center.

## Core Features

- Track stocks, ETFs, bonds, and physical precious metals.
- Calculate portfolio value, cost basis, realized and unrealized gains, and
  allocation.
- Record portfolio transactions and capture daily performance snapshots.
- Compare portfolio performance with selectable market benchmarks.
- Calculate precious-metal purity, troy-ounce weight, and melt value.
- Import multiple holdings from CSV data.
- Review market quotes, market news, goals, charts, and educational lessons.
- Isolate each user's data behind stateless JWT authentication.

## Architecture

| Layer | Technology | Responsibility |
| --- | --- | --- |
| Frontend | React, Vite, Chart.js | Dashboard, forms, charts, and learning tools |
| Backend | Java 21, Spring Boot, Spring Security | REST API, validation, authentication, and financial calculations |
| Persistence | PostgreSQL, Spring Data JPA | Users, holdings, transactions, prices, and snapshots |
| Testing | JUnit, Mockito, Testcontainers, Vitest, Playwright | Unit, integration, and browser-level verification |

The frontend is organized by feature, while the backend uses controller,
service, repository, and model layers.

## Start Here

New contributors and reviewers should read [PROJECT_GUIDE.md](PROJECT_GUIDE.md).
It explains the folder structure, frontend and backend request flows, major
services, naming conventions, and the safest order for exploring the code.

```text
frontend/src/app/                  Application composition
frontend/src/features/             Feature-owned UI, hooks, and API modules
frontend/src/shared/               Reusable components and finance utilities
backend/src/main/java/.../controller  HTTP endpoints
backend/src/main/java/.../service     Business rules and integrations
backend/src/main/java/.../repository  Database access
backend/src/main/java/.../model       JPA entities
backend/src/test/                   Backend unit and integration tests
Documentation/                     Design and planning documents
```

## Local Setup

Prerequisites:

- Java 21
- Node.js 22.22.2 or later
- npm 10 or later
- PostgreSQL
- Docker-compatible container runtime for the backend integration test

Create a PostgreSQL database named `wealth_pulse`, then configure environment
variables as needed:

```text
DB_URL=jdbc:postgresql://localhost:5432/wealth_pulse
DB_USERNAME=postgres
DB_PASSWORD=your-password
JWT_SECRET=a-long-random-secret
ALPHA_VANTAGE_KEY=optional-market-data-key
GOLD_API_KEY=optional-metals-data-key
```

Start both applications from the repository root:

```bash
./start_all.sh
```

The default frontend and backend addresses are:

```text
Frontend: http://localhost:5173
Backend:  http://localhost:8283
```

## Verification

Frontend:

```bash
cd frontend
npm run lint
npm test
npm run build
npm run test:e2e
```

Backend:

```bash
cd backend
./mvnw test
```

See [TESTING.md](TESTING.md) for test coverage, environment requirements, and
the manual acceptance checklist.

## Project Milestones

| Phase | Focus |
| --- | --- |
| M1 | Environment and database setup |
| M2 | Authenticated asset-ledger API |
| M3 | Stock and precious-metal market integrations |
| M4 | Responsive React dashboard |
| M5 | Portfolio calculations and performance analytics |
| M6 | Financial learning center and interactive tools |
| M7 | Automated testing and user acceptance testing |

## Disclaimer

Projections and performance illustrations depend on their stated assumptions.
Past performance does not guarantee future results. WealthPulse provides
educational information, not personalized investment, legal, or tax advice,
and does not act as a fiduciary.

## License

WealthPulse is available under the [MIT License](LICENSE).
