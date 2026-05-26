// Frontend mono-performance smoke test
// Handles both authenticated and unauthenticated states

import { chromium } from 'playwright';
import { writeFileSync } from 'fs';

const BASE = 'http://127.0.0.1:5173';
const results = [];
let passed = 0;
let failed = 0;

function check(label, ok, detail) {
  if (ok) { passed++; results.push(`PASS ${label} — ${detail || 'ok'}`); }
  else { failed++; results.push(`FAIL ${label} — ${detail || 'FAILED'}`); }
}

async function run() {
  const browser = await chromium.launch({ 
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });
  const context = await browser.newContext({ viewport: { width: 1280, height: 900 } });
  const page = await context.newPage();

  // Track network requests
  const googleFontRequests = [];
  const imageRequests = [];
  page.on('request', req => {
    const url = req.url();
    if (url.includes('fonts.googleapis.com') || url.includes('fonts.gstatic.com')) {
      googleFontRequests.push(url);
    }
    if (req.resourceType() === 'image' && !url.startsWith('data:')) {
      imageRequests.push(url);
    }
  });

  // ===== 1. GLOBAL HTML CHECKS =====
  console.log('\n--- Global HTML checks ---');
  await page.goto(BASE + '/login', { waitUntil: 'domcontentloaded', timeout: 15000 });
  await page.waitForTimeout(2000);

  const currentUrl = page.url();
  const isAuthenticated = currentUrl.includes('/app/');

  const htmlLang = await page.getAttribute('html', 'lang');
  check('html lang="zh-CN"', htmlLang === 'zh-CN', `lang="${htmlLang}"`);

  const title = await page.title();
  check('Chinese page title', title.includes('临床'), `"${title}"`);

  check('No Google Font requests', googleFontRequests.length === 0,
    googleFontRequests.length > 0 ? `Found: ${googleFontRequests.join(', ')}` : 'ok');
  check('No static image requests', imageRequests.length === 0,
    imageRequests.length > 0 ? `Images: ${imageRequests.slice(0,5).join(', ')}` : 'ok');

  const themeAttr = await page.getAttribute('html', 'data-theme');
  check('data-theme attribute present', themeAttr !== null, `value="${themeAttr}"`);

  const bodyFont = await page.evaluate(() => getComputedStyle(document.body).fontFamily);
  check('System font stack (no Google Fonts)', !bodyFont.includes('Sora') && !bodyFont.includes('DM'), `font: ${bodyFont.substring(0,60)}`);

  const hasReducedMotion = await page.evaluate(() => {
    try {
      const css = [...document.styleSheets].flatMap(s => {
        try { return [...s.cssRules]; } catch { return []; }
      });
      return css.some(r => r.cssText?.includes('prefers-reduced-motion'));
    } catch { return false; }
  });
  check('CSS prefers-reduced-motion rule', hasReducedMotion, null);

  const imgCount = await page.evaluate(() => document.querySelectorAll('img').length);
  check('No <img> elements on page', imgCount === 0, `found ${imgCount}`);

  // ===== 2. LOGIN PAGE (if still on /login) or DASHBOARD (if authenticated) =====
  if (isAuthenticated) {
    console.log('\n--- Authenticated: testing dashboard instead of login page ---');
    const dashText = await page.textContent('body').catch(() => '');
    check('Dashboard: renders content', dashText.length > 50, `~${dashText.length} chars`);
    const hasChinese = dashText.includes('总览') || dashText.includes('项目') || 
                       dashText.includes('全部') || dashText.includes('状态') ||
                       dashText.includes('统计') || dashText.includes('活跃');
    check('Dashboard: Chinese labels present', hasChinese, null);

    const themeBtn = page.locator('button:has-text("夜间"), button:has-text("日间")');
    const tCount = await themeBtn.count();
    check('Dashboard: theme toggle button', tCount > 0, `found ${tCount}`);
    if (tCount > 0) {
      await themeBtn.first().click();
      await page.waitForTimeout(500);
      const nightTheme = await page.getAttribute('html', 'data-theme');
      check('Theme toggle -> night', nightTheme === 'night', `data-theme="${nightTheme}"`);
      await themeBtn.first().click();
      await page.waitForTimeout(300);
      const dayTheme = await page.getAttribute('html', 'data-theme');
      check('Theme toggle -> daylight', dayTheme === 'daylight', `data-theme="${dayTheme}"`);
    }

    // Check sidebar navigation
    const navEl = page.locator('.ant-layout-sider, nav, [class*="sider"]');
    const navText = await navEl.textContent().catch(() => '');
    check('Sidebar: Chinese nav items', 
      (navText?.includes('总览') || navText?.includes('项目') || navText?.includes('受试者')), null);

    // Check header
    const headerText = await page.textContent('header').catch(() => '');
    const hasHeaderContent = headerText && headerText.length > 10;
    check('Header renders with content', !!hasHeaderContent, null);

    await page.screenshot({ path: '/tmp/smoke-dashboard.png', fullPage: true });
  } else {
    console.log('\n--- Unauthenticated: testing login page ---');
    const bodyText = await page.textContent('body');
    check('Chinese UI labels', bodyText.includes('用户名') && bodyText.includes('密码'), null);

    const hasForm = await page.evaluate(() => !!document.querySelector('form'));
    check('<form> element present', hasForm, null);

    const inputCount = await page.evaluate(() => document.querySelectorAll('input:not([type="hidden"])').length);
    check('Visible form inputs >= 2', inputCount >= 2, `found ${inputCount}`);

    const usernameInput = page.locator('input#username, input[id*="username"]');
    const passwordInput = page.locator('input[type="password"]');
    const submitBtn = page.locator('button[type="submit"], button:has-text("登录")');
    check('Username field', await usernameInput.count() > 0, null);
    check('Password field', await passwordInput.count() > 0, null);
    check('Submit button with Chinese text', await submitBtn.count() > 0, null);

    const svgCount = await page.evaluate(() => document.querySelectorAll('svg').length);
    console.log(`  [info] SVG elements on login: ${svgCount}`);

    await page.screenshot({ path: '/tmp/smoke-login.png', fullPage: true });
  }

  // ===== 3. MAIN MENU =====
  console.log('\n--- Main Menu (/) ---');
  await page.goto(BASE + '/', { waitUntil: 'domcontentloaded', timeout: 15000 });
  await page.waitForTimeout(1000);

  const menuText = await page.textContent('body').catch(() => '');
  check('Main menu: greeting text', menuText.includes('欢迎'), null);
  check('Main menu: Chinese nav items', 
    menuText.includes('项目') && menuText.includes('受试者') && menuText.includes('CRF'), null);
  check('Main menu: subtitle text', menuText.includes('选择一个模块'), null);
  
  const svgCountMenu = await page.evaluate(() => document.querySelectorAll('svg').length);
  console.log(`  [info] SVG elements on main menu: ${svgCountMenu}`);
  
  await page.screenshot({ path: '/tmp/smoke-menu.png', fullPage: true });

  // ===== 4. 404 PAGE =====
  console.log('\n--- 404 Page ---');
  await page.goto(BASE + '/nonexistent-route-xyz', { waitUntil: 'domcontentloaded', timeout: 15000 }).catch(() => {});
  await page.waitForTimeout(1000);
  const nfText = await page.textContent('body').catch(() => '');
  check('404 page: Chinese text', nfText.includes('页面') || nfText.includes('返回'), null);
  await page.screenshot({ path: '/tmp/smoke-404.png', fullPage: true }).catch(() => {});

  // ===== REPORT =====
  console.log('\n' + '='.repeat(60));
  console.log(`SMOKE TEST RESULTS: ${passed} passed, ${failed} failed, ${passed + failed} total`);
  console.log('='.repeat(60));
  for (const r of results) console.log(r);

  writeFileSync('/tmp/smoke-test-results.txt', 
    `SMOKE TEST RESULTS: ${passed} passed, ${failed} failed\n\n` + results.join('\n'));
  
  await browser.close();
  process.exit(failed > 0 ? 1 : 0);
}

run().catch(err => {
  console.error('Test crashed:', err.message);
  process.exit(1);
});