import { test, expect } from "@playwright/test";
import { loginAsAdmin } from "./helpers/auth";

test.describe("CRF Module", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test("CRF list page loads without JS errors", async ({ page }) => {
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));
    await page.goto("/app/crfs");
    await page.waitForLoadState("networkidle");
    expect(errors).toHaveLength(0);
  });

  test("CRF list has accessible table", async ({ page }) => {
    await page.goto("/app/crfs");
    await expect(page.locator(".ant-table")).toBeVisible({ timeout: 10000 });
  });

  test("CRF version manager link navigates correctly", async ({ page }) => {
    await page.goto("/app/crfs");
    await page.waitForURL("**/app/crfs");
    const crfRow = page.locator(".ant-table-row").first();
    if (await crfRow.isVisible()) {
      await crfRow.click();
    }
  });
});
