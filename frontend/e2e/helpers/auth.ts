import type { Page } from "@playwright/test";
import { expect } from "@playwright/test";

export interface TestCredentials {
  username: string;
  password: string;
}

export const ADMIN_CREDENTIALS: TestCredentials = {
  username: "admin",
  password: "password",
};

export const INVESTIGATOR_CREDENTIALS: TestCredentials = {
  username: "investigator",
  password: "password",
};

export async function login(
  page: Page,
  credentials: TestCredentials = ADMIN_CREDENTIALS,
): Promise<void> {
  const response = await page.request.post("/api/v1/auth/login", {
    data: { username: credentials.username, password: credentials.password },
  });
  if (!response.ok()) {
    throw new Error(`Login failed: ${response.status()} ${await response.text()}`);
  }
  await page.goto("/app/dashboard", { waitUntil: "networkidle" });
  await expect(page.locator("text=总览")).toBeVisible({ timeout: 15000 });
}

export async function loginViaUI(
  page: Page,
  credentials: TestCredentials = ADMIN_CREDENTIALS,
): Promise<void> {
  await page.goto("/app/login", { waitUntil: "networkidle" });
  await page.waitForSelector('[name="username"]', { timeout: 15000 });
  await page.fill('[name="username"]', credentials.username);
  await page.fill('[name="password"]', credentials.password);
  await page.click('[type="submit"]');
  await page.waitForURL("**/app/dashboard", { timeout: 15000 });
  await expect(page.locator("text=总览")).toBeVisible({ timeout: 10000 });
}

export async function loginAsAdmin(page: Page): Promise<void> {
  await login(page, ADMIN_CREDENTIALS);
}

export async function loginAsInvestigator(page: Page): Promise<void> {
  await login(page, INVESTIGATOR_CREDENTIALS);
}

export async function logout(page: Page): Promise<void> {
  const userMenu = page.locator("button:has-text('admin')");
  if (await userMenu.isVisible()) {
    await userMenu.click();
    await page.locator("text=退出登录").click();
    await page.waitForURL("**/login", { timeout: 10000 });
  }
}
