import { test, expect } from "@playwright/test";
import { loginAsAdmin } from "./helpers/auth";

test.describe("Admin Module", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test("admin dashboard loads with section cards", async ({ page }) => {
    await page.goto("/app/admin");
    await page.waitForURL("**/app/admin");
    await expect(page.locator("text=管理")).toBeVisible();
    await expect(page.locator("text=用户管理")).toBeVisible({ timeout: 10000 });
  });

  test("user management page loads", async ({ page }) => {
    await page.goto("/app/admin/users");
    await page.waitForURL("**/app/admin/users");
    await expect(page.locator(".ant-table")).toBeVisible({ timeout: 10000 });
  });

  test("navigating admin section cards works", async ({ page }) => {
    await page.goto("/app/admin");
    const auditCard = page.locator(".ant-card", { hasText: "审计日志" });
    if (await auditCard.isVisible()) {
      await auditCard.click();
      await page.waitForURL("**/app/admin/audit-log", { timeout: 10000 });
    }
  });

  test("audit log page loads", async ({ page }) => {
    await page.goto("/app/admin/audit-log");
    await page.waitForURL("**/app/admin/audit-log");
    await expect(page.locator("text=审计")).toBeVisible({ timeout: 10000 });
  });

  test("CRF library admin page loads", async ({ page }) => {
    await page.goto("/app/admin/crf-library");
    await page.waitForURL("**/app/admin/crf-library");
    await expect(page.locator(".ant-card").first()).toBeVisible({ timeout: 10000 });
  });

  test("system configuration page loads", async ({ page }) => {
    await page.goto("/app/admin/system");
    await page.waitForURL("**/app/admin/system");
    await expect(page.getByRole("heading", { name: /系统/ })).toBeVisible({ timeout: 10000 });
  });

  test("admin page captures JS errors", async ({ page }) => {
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));
    await page.goto("/app/admin/users");
    await page.waitForTimeout(3000);
    expect(errors).toHaveLength(0);
  });
});
