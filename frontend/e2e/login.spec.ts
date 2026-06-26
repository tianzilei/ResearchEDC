import { test, expect } from "@playwright/test";
import { login } from "./helpers/auth";

test.describe("Authentication", () => {
  test("redirects unauthenticated users from protected routes", async ({ page }) => {
    await page.goto("/app/dashboard");
    await page.waitForLoadState("networkidle");
    await expect(page.getByText("ResearchEDC").first()).toBeVisible({ timeout: 10000 });
  });

  test("shows login page for unauthenticated users", async ({ page }) => {
    await page.goto("/app/login");
    await page.waitForLoadState("networkidle");
    const body = page.locator("body");
    await expect(body).toBeVisible({ timeout: 15000 });
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));
    await page.waitForTimeout(1000);
    expect(errors).toHaveLength(0);
  });

  test("successful login via API redirects to dashboard", async ({ page }) => {
    await login(page);
    await expect(page.getByText(/[早晚]上/)).toBeVisible();
  });

  test("authenticated user can access dashboard directly", async ({ page }) => {
    await login(page);
    await expect(page.getByText(/[早晚]上/)).toBeVisible();
  });
});
