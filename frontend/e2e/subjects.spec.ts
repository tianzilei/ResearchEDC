import { test, expect } from "@playwright/test";
import { loginAsAdmin } from "./helpers/auth";

test.describe("Subjects Module", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test("subject list page loads without JS errors", async ({ page }) => {
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));
    await page.goto("/app/subjects");
    await page.waitForLoadState("networkidle");
    expect(errors).toHaveLength(0);
  });

  test("subject page renders without JS errors", async ({ page }) => {
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));
    await page.goto("/app/subjects");
    await page.waitForTimeout(3000);
    expect(errors).toHaveLength(0);
  });
});
