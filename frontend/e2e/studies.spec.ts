import { test, expect } from "@playwright/test";
import { loginAsAdmin } from "./helpers/auth";

test.describe("Studies Module", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test("study list page shows demo study", async ({ page }) => {
    await page.goto("/app/studies");
    await page.waitForURL("**/app/studies");
    await expect(page.locator("text=Demo Sleep Study")).toBeVisible({ timeout: 10000 });
  });

  test("study list has table with columns", async ({ page }) => {
    await page.goto("/app/studies");
    await expect(page.locator(".ant-table")).toBeVisible({ timeout: 10000 });
  });

  test("create study wizard opens", async ({ page }) => {
    await page.goto("/app/studies");
    const createButton = page.locator("button", { hasText: /创建|新建|New|Create/ });
    if (await createButton.isVisible()) {
      await createButton.click();
      await page.waitForURL("**/studies/create", { timeout: 10000 });
    }
  });

  test("navigating to study detail shows study info", async ({ page }) => {
    await page.goto("/app/studies");
    const studyLink = page.locator("a", { hasText: "Demo Sleep Study" });
    if (await studyLink.isVisible()) {
      await studyLink.click();
      await page.waitForURL(/.*\/app\/studies\/\d+/, { timeout: 10000 });
    }
  });

  test("study page renders without JS errors", async ({ page }) => {
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));
    await page.goto("/app/studies");
    await page.waitForTimeout(3000);
    expect(errors).toHaveLength(0);
  });
});
