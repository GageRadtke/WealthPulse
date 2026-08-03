# WealthPulse Testing Guide

This guide separates automated tests from manual user acceptance testing (UAT).
Only report a test as completed after you have run it and recorded the result.

## 1. Run the automated backend tests

Open a terminal in the project folder:

```bash
cd /home/gage/WealthPulse/backend
./mvnw test
```

A successful run ends with:

```text
BUILD SUCCESS
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
```

These tests currently verify:

- JWT generation, username extraction, user matching, expiration, malformed
  tokens, and signature tampering
- fresh market-cache usage without an external request
- stale-cache fallback after a simulated provider failure
- the error returned when neither an API price nor cached price is available
- safe mapping of create and quantity-update DTOs without persistence state
- purchase, partial-sale, and complete-sale cost-basis calculations
- rejection of sales that exceed the owned quantity
- valid gold karats and silver fineness values, including metal-specific defaults
- registration and JWT creation against an isolated PostgreSQL database
- persisted asset retrieval and isolation between two different users
- rejection of cross-user asset deletion and malformed bearer tokens

Test source:

- `backend/src/test/java/com/example/wealthpulse/security/JwtServiceTest.java`
- `backend/src/test/java/com/example/wealthpulse/service/AssetServiceTest.java`
- `backend/src/test/java/com/example/wealthpulse/service/AssetRequestMapperTest.java`
- `backend/src/test/java/com/example/wealthpulse/service/AssetManagementRulesTest.java`
- `backend/src/test/java/com/example/wealthpulse/service/AssetValidationServiceTest.java`
- `backend/src/test/java/com/example/wealthpulse/integration/AssetAndAuthIntegrationTest.java`

The integration test uses Testcontainers and therefore requires a running
Docker-compatible container engine. If Docker is unavailable, Maven reports
that one test was skipped. Start Docker and rerun `./mvnw test`; the completed
result must say `Skipped: 0` before recording the integration test as passed.

## 2. Run the automated frontend tests

```bash
cd /home/gage/WealthPulse/frontend
npm test
```

A successful run includes:

```text
Test Files  3 passed
Tests       10 passed
```

These tests currently verify:

- call and put intrinsic-value calculations
- invalid option inputs do not produce `NaN`
- login form values are submitted correctly
- the create-account button invokes the registration action
- stock portfolio totals and safe handling of empty or invalid holdings
- goal progress, remaining goal value, and allocation calculations

To rerun tests automatically while editing code:

```bash
npm run test:watch
```

## 3. Check code quality and the production build

```bash
cd /home/gage/WealthPulse/frontend
npm run lint
npm run build
```

Lint checks the source for code-quality problems. Build verifies that Vite can
produce the deployable frontend.

## 4. Run the Playwright browser tests

Install the browser once on a new computer:

```bash
cd /home/gage/WealthPulse/frontend
npx playwright install chromium
```

Run the browser suite:

```bash
npm run test:e2e
```

A successful run ends with:

```text
2 passed
```

The Playwright suite verifies:

- registration through the real React form
- adding stock, bond, and precious-metal positions and seeing them in the portfolio
- stock and bond holdings appear in their detailed dashboard sections
- updating the real call/put options calculator without a page reload

The browser suite mocks HTTP responses so it is deterministic and does not
modify the development database. The Testcontainers suite separately verifies
the real Spring Boot, JWT, JPA, and PostgreSQL integration.

## 5. Perform manual UAT

Start the application with the project's normal startup command:

```bash
cd /home/gage/WealthPulse
./start_all.sh
```

Use two test accounts so you do not risk personal data. For each scenario,
record the date, tester, result (`Pass` or `Fail`), and notes or screenshots.

| ID | Steps | Expected result |
| --- | --- | --- |
| UAT-01 | Register a test account, sign out, sign in with the correct password, then try an incorrect password. | Registration and correct login succeed; incorrect login is rejected; protected dashboard data is not available while signed out. |
| UAT-02 | Add one stock and one precious-metal holding. Edit each quantity, refresh the browser, then delete the records. | Both records appear with the correct values, edits survive refresh, and deleted records disappear. |
| UAT-03 | Create an asset in account A. Sign out and sign in to account B. | Account B cannot see or modify account A's asset. |
| UAT-04 | Open the dashboard and compare its total with the sum of the displayed holdings. | The dashboard total and charts agree with the holdings data. |
| UAT-05 | Open the learning tools and change each available calculator control. | Results update without a page reload and remain valid numbers. |
| UAT-06 | Repeat the core workflow at desktop width and a narrow mobile-sized browser width. | Text remains readable, controls remain usable, and content does not overlap. |

### UAT result template

Copy this block once for each test:

```text
Test ID:
Date:
Tester:
Browser/device:
Result: Pass / Fail
Actual result:
Screenshot filename:
Issue or follow-up:
```

## 6. Keep the paper accurate

The repository currently has five backend unit-test classes, one backend
integration-test class, three frontend unit-test files, and one Playwright
specification containing two browser tests. It uses JUnit/Mockito,
Vitest/React Testing Library, Testcontainers, and Playwright.

Do not describe the Testcontainers case as passed when Maven reports it as
skipped. The Playwright tests are browser/UI tests with mocked HTTP responses;
do not describe them as full-stack PostgreSQL E2E tests.
