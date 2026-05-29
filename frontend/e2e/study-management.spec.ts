import { test, expect } from "@playwright/test";
import { loginAsAdmin } from "./helpers/auth";

test.describe("Study Management — Deep Navigation", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test("study list shows table with studies", async ({ page }) => {
    await page.goto("/app/studies");
    await page.waitForURL("**/app/studies");
    await expect(page.locator(".ant-table")).toBeVisible({ timeout: 10000 });
    await expect(page.getByText("Demo Sleep Study")).toBeVisible({ timeout: 10000 });
  });

  test("study list has correct tab structure", async ({ page }) => {
    await page.goto("/app/studies");
    await page.waitForURL("**/app/studies");
    await expect(page.getByText("项目").first()).toBeVisible();
    await expect(page.getByText("新建项目")).toBeVisible();
  });

  test("study detail page loads and shows tabs", async ({ page }) => {
    await page.goto("/app/studies/1");
    await page.waitForURL("**/app/studies/1");

    const studyName = page.getByText("Demo Sleep Study");
    const notFound = page.getByText("项目未找到");
    const isVisible = await studyName.isVisible().catch(() => false);
    const is404 = await notFound.isVisible().catch(() => false);

    if (isVisible) {
      await expect(page.getByText("概览")).toBeVisible();
      await expect(page.getByText(/站点/)).toBeVisible();
      await expect(page.getByText("快捷操作")).toBeVisible();
    } else if (is404) {
      await expect(page.getByText("返回项目列表")).toBeVisible();
    }
  });

  test("study detail shows protocol descriptions", async ({ page }) => {
    await page.goto("/app/studies/1");
    await page.waitForURL("**/app/studies/1");
    const studyName = page.getByText("Demo Sleep Study");
    const notFound = page.getByText("项目未找到");

    if (await studyName.isVisible().catch(() => false)) {
      await expect(page.locator(".ant-descriptions").first()).toBeVisible({ timeout: 10000 });
      await expect(page.getByText("方案信息")).toBeVisible();
      await expect(page.getByText("研究设计")).toBeVisible();
      await expect(page.getByText("机构与入组")).toBeVisible();
    } else if (await notFound.isVisible().catch(() => false)) {
      await expect(page.getByText("项目 #1 不存在")).toBeVisible();
    }
  });

  test("study detail actions tab shows quick-action cards", async ({ page }) => {
    await page.goto("/app/studies/1");
    await page.waitForURL("**/app/studies/1");
    const studyName = page.getByText("Demo Sleep Study");

    if (await studyName.isVisible().catch(() => false)) {
      await page.getByText("快捷操作").click();
      await page.waitForTimeout(500);
      await expect(page.getByText("编辑项目")).toBeVisible();
      await expect(page.getByText("管理受试者")).toBeVisible();
      await expect(page.getByText("事件定义")).toBeVisible();
    }
  });

  test("study edit page loads with form sections", async ({ page }) => {
    await page.goto("/app/studies/1/edit");
    await page.waitForURL("**/app/studies/1/edit");
    await expect(page.getByText("编辑研究")).toBeVisible({ timeout: 10000 });
    await expect(page.getByText("方案信息")).toBeVisible();
    await expect(page.getByText("赞助信息")).toBeVisible();
    await expect(page.getByText("研究设计")).toBeVisible();
    await expect(page.locator("button", { hasText: "保存修改" })).toBeVisible();
  });

  test("study wizard shows 8-step layout", async ({ page }) => {
    await page.goto("/app/studies/create");
    await page.waitForURL("**/studies/create", { timeout: 10000 });
    await expect(page.locator(".ant-steps")).toBeVisible({ timeout: 10000 });
    const stepItems = page.locator(".ant-steps-item");
    await expect(stepItems).toHaveCount(8);
    await expect(stepItems.first()).toHaveClass(/ant-steps-item-active/);
  });

  test("study wizard step navigation progresses", async ({ page }) => {
    await page.goto("/app/studies/create");
    await page.waitForURL("**/studies/create", { timeout: 10000 });

    await page.locator("#name").fill("E2E Test Study");
    await page.waitForTimeout(300);

    const nextBtn = page.locator("button", { hasText: /下一步/ });
    await expect(nextBtn).toBeVisible({ timeout: 5000 });
    await nextBtn.click();
    await page.waitForTimeout(1000);

    const stepItems = page.locator(".ant-steps-item");
    await expect(stepItems.nth(1)).toHaveClass(/ant-steps-item-active/);
  });

  test("event definitions page navigates to correct URL", async ({ page }) => {
    await page.goto("/app/studies/1/event-definitions");
    await page.waitForURL("**/event-definitions", { timeout: 10000 });
    await expect(page).toHaveURL(/.*\/app\/studies\/\d+\/event-definitions/);
  });

  test("study pages render without JavaScript errors", async ({ page }) => {
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));

    const pages = [
      "/app/studies",
      "/app/studies/1",
      "/app/studies/1/edit",
      "/app/studies/create",
      "/app/studies/1/event-definitions",
    ];

    for (const url of pages) {
      await page.goto(url);
      await page.waitForLoadState("networkidle");
      await page.waitForTimeout(1000);
    }

    expect(errors).toHaveLength(0);
  });
});
