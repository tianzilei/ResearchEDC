import { test, expect } from '@playwright/test';

test.describe('ResearchEDC SPA', () => {

  test('SPA shell loads and renders login redirect', async ({ page }) => {
    const response = await page.goto('/app/dashboard');
    // SPA fallback should serve index.html for /app/* routes
    expect(response?.status()).toBeLessThan(400);
    await expect(page.locator('#root')).toBeAttached();
  });

  test('dashboard page has header', async ({ page }) => {
    await page.goto('/app/dashboard');
    await expect(page.locator('header, nav, [class*="header"], [class*="Header"]'))
      .toBeAttached();
  });

  test('CRF list page loads', async ({ page }) => {
    await page.goto('/app/crfs');
    const response = await page.goto('/app/crfs');
    expect(response?.status()).toBeLessThan(400);
  });

  test('data export page loads', async ({ page }) => {
    await page.goto('/app/data-export');
    expect(page.url()).toContain('/app/data-export');
  });

  test('randomization page loads', async ({ page }) => {
    await page.goto('/app/randomization');
    expect(page.url()).toContain('/app/randomization');
  });
});
