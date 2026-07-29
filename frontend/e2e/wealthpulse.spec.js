import { expect, test } from "@playwright/test";

const jwt = [
  btoa(JSON.stringify({ alg: "HS256", typ: "JWT" })),
  btoa(JSON.stringify({ sub: "e2e-user" })),
  "test-signature",
].join(".");

test.beforeEach(async ({ page }) => {
  let assets = [];
  let nextId = 1;
  const responseHeaders = {
    "access-control-allow-origin": "http://127.0.0.1:4173",
    "access-control-allow-credentials": "true",
    "access-control-allow-headers": "Authorization, Content-Type",
    "access-control-allow-methods": "GET, POST, PUT, DELETE, OPTIONS",
  };

  await page.route(/^http:\/\/localhost:828[34]\/api\//, async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;

    if (path === "/api/auth/register" && request.method() === "POST") {
      return route.fulfill({
        headers: responseHeaders,
        json: {
          token: jwt,
          username: "e2e-user",
          email: "e2e-user@wealthpulse.local",
          expiresInMs: 86_400_000,
        },
      });
    }

    if (path === "/api/assets/update-quantity" && request.method() === "PUT") {
      const incoming = request.postDataJSON();
      const saved = { ...incoming, id: nextId++ };
      assets.push(saved);
      return route.fulfill({ headers: responseHeaders, json: saved });
    }

    if (path === "/api/assets" && request.method() === "GET") {
      return route.fulfill({ headers: responseHeaders, json: assets });
    }

    if (path === "/api/cache/status") {
      return route.fulfill({ headers: responseHeaders, json: [] });
    }

    if (path === "/api/news/markets") {
      return route.fulfill({ headers: responseHeaders, json: [] });
    }

    if (path === "/api/market-tracker/quotes") {
      return route.fulfill({ headers: responseHeaders, json: [] });
    }

    return route.fulfill({ headers: responseHeaders, status: 204 });
  });

  await page.goto("/");
  await page.getByRole("button", { name: /Create User Account/i }).click();
  await page.getByPlaceholder("John Doe").fill("End To End User");
  await page.getByPlaceholder("Choose a Username...").fill("e2e-user");
  await page.getByPlaceholder("Create strong password...").fill("SecureAA12!");
  await page.locator('input[name="securityQuestion"]').fill("test reminder");
  await page.getByRole("button", { name: "Establish Entry" }).click();
  await expect(page.getByText("Welcome, e2e-user!")).toBeVisible();
});

test("adds stock and precious-metal positions through the portfolio UI", async ({
  page,
}) => {
  await page.getByPlaceholder("e.g. AAPL").fill("AAPL");
  await page.getByPlaceholder("e.g. Apple Inc.").fill("Apple Inc.");
  await page.locator('input[name="quantity"]').fill("2.5");
  await page.locator('input[name="amountPaid"]').fill("450");
  await page.locator('input[name="price"]').fill("200");
  await page.getByRole("button", { name: "Add To Portfolio" }).click();

  await expect(page.getByRole("cell", { name: "AAPL" })).toBeVisible();
  await expect(page.getByRole("cell", { name: /2.5 Shares/ })).toBeVisible();

  await page.getByRole("button", { name: "Bond", exact: true }).click();
  await page.getByPlaceholder("e.g. AAPL").fill("AAPL");
  await page.getByPlaceholder("e.g. Apple Inc.").fill("Apple Corporate Bond");
  await page.locator('input[name="quantity"]').fill("1");
  await page.locator('input[name="amountPaid"]').fill("98.5");
  await page.locator('input[name="price"]').fill("98.5");
  await page.getByRole("button", { name: "Add To Portfolio" }).click();

  await expect(page.getByRole("cell", { name: "AAPL" })).toHaveCount(2);
  await expect(page.getByRole("cell", { name: "STOCK", exact: true })).toBeVisible();
  await expect(page.getByRole("cell", { name: "Bond", exact: true })).toBeVisible();

  await page.getByRole("button", { name: /Stocks In-Depth/i }).click();
  await expect(page.getByRole("heading", { name: "Stock Holdings" })).toBeVisible();
  await expect(page.getByRole("heading", { name: "Bond Holdings" })).toBeVisible();
  await expect(page.getByRole("cell", { name: "AAPL" })).toHaveCount(2);

  await page.getByRole("button", { name: /Portfolio Overview/i }).click();
  await page.getByRole("button", { name: "Precious Metal" }).click();
  await page.getByPlaceholder("e.g. Gold Eagle, Silver Bar").fill("Silver Eagle");
  await page.locator('select[name="metalSymbol"]').selectOption("XAG");
  await page.locator('input[name="quantity"]').fill("100.5");
  await page.locator('input[name="amountPaid"]').fill("2814");
  await page.locator('input[name="price"]').fill("28");
  await page.getByRole("button", { name: "Add To Portfolio" }).click();

  await expect(page.getByRole("cell", { name: "Silver Eagle" })).toBeVisible();
  await expect(page.getByRole("cell", { name: /100.5 Oz/ })).toBeVisible();
});

test("updates the real options calculator without reloading the page", async ({
  page,
}) => {
  await page.getByRole("button", { name: /Learning Center/i }).click();

  const stockPrice = page.locator(".learning-number-field").filter({
    hasText: "Stock Market Price ($)",
  }).locator("input");
  const strikePrice = page.locator(".learning-number-field").filter({
    hasText: "Option Strike Price ($)",
  }).locator("input");

  await stockPrice.fill("125");
  await strikePrice.fill("100");

  await expect(
    page.locator(".result-badge").filter({ hasText: "Call Intrinsic Value:" }),
  ).toContainText("$25.00");
  await expect(
    page.locator(".result-badge").filter({ hasText: "Put Intrinsic Value:" }),
  ).toContainText("$0.00");
});
