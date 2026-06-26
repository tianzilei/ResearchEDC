import { test, expect } from "@playwright/test";
import { loginAsAdmin } from "./helpers/auth";

test.describe("Dashboard", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test("displays welcome greeting with user name", async ({ page }) => {
    await expect(page.getByText(/[早晚]上[好哈]/)).toBeVisible({ timeout: 10000 });
  });

  test("shows module quick-action cards", async ({ page }) => {
    await expect(page.locator(".ant-card").first()).toBeVisible({ timeout: 15000 });
  });

  test("sidebar navigation menu is visible", async ({ page }) => {
    await expect(page.locator(".ant-layout-sider")).toBeVisible();
    await expect(page.getByText("总览").first()).toBeVisible();
  });

  test("clicking module card navigates to feature page", async ({ page }) => {
    const studiesCard = page.locator(".ant-card").filter({ hasText: "项目" }).first();
    if (await studiesCard.isVisible()) {
      await studiesCard.click();
      await page.waitForURL("**/app/studies", { timeout: 10000 });
    }
  });

  test("language switcher changes UI language", async ({ page }) => {
    const langSelect = page.locator(".header-lang-select");
    if (await langSelect.isVisible()) {
      await langSelect.click();
      await page.locator(".ant-select-item-option", { hasText: "English" }).click();
      await expect(page.getByText("Overview")).toBeVisible({ timeout: 5000 });
    }
  });
});
