import { test, expect } from "@playwright/test";
import { loginAsAdmin } from "./helpers/auth";

test.describe("Randomization Module", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test("randomization dashboard loads", async ({ page }) => {
    await page.goto("/app/randomization");
    await page.waitForURL("**/app/randomization");
    await expect(page.locator("text=随机")).toBeVisible({ timeout: 10000 });
  });

  test("create scheme modal opens", async ({ page }) => {
    await page.goto("/app/randomization");
    const createButton = page.locator("button", { hasText: /创建/ });
    if (await createButton.isVisible()) {
      await createButton.click();
      await expect(page.locator(".ant-modal")).toBeVisible({ timeout: 5000 });
      await page.locator(".ant-modal .ant-modal-close").click();
    }
  });

  test("page renders without errors", async ({ page }) => {
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));
    await page.goto("/app/randomization");
    await page.waitForTimeout(3000);
    expect(errors).toHaveLength(0);
  });
});
