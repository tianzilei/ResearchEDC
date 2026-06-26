# 前端设计系统文档 — ResearchEDC

> 基于 Mono-Performance 设计理念，无图片、无图标、极简排版。

## 设计理念

- **文本优先**：清晰的字体层级驱动界面，不使用装饰性图片或图标。
- **极简性能**：系统字体栈，无外部 Web Font，无动画库，无 CSS-in-JS。
- **昼夜主题**：所有页面支持 `daylight`（日间）和 `night`（夜间）双主题。
- **无障碍**：中文字优先，足够的对比度，可见的焦点状态，语义化 HTML。

---

## 主题系统

主题通过两层机制实现：

1. **CSS 变量**（`global.css`）— 驱动所有自定义布局和组件的颜色/尺寸
2. **Ant Design ConfigProvider**（`theme.ts`）— 驱动 Ant Design 组件主题

### 主题切换

```tsx
// ThemeProvider 监听 mode 状态
// 1. 设置 <html data-theme="daylight|night">
// 2. 传入 ConfigProvider theme={getTheme(mode)}
// CSS 变量通过 [data-theme] 选择器切换
```

### CSS 变量 — 色彩

| Token | Daylight | Night | 用途 |
|-------|----------|-------|------|
| `--bg` | `#f7f7f4` | `#11110f` | 页面底色 |
| `--bg-layout` | `#efefe9` | `#161614` | Layout 背景 |
| `--bg-secondary` | `#f0f0eb` | `#22221f` | 次要背景 |
| `--panel` | `#ffffff` | `#181816` | 卡片/面板背景 |
| `--panel-muted` | `#f0f0eb` | `#22221f` | 面板次级背景 |
| `--text` | `#151515` | `#ecece5` | 主文本 |
| `--text-secondary` | `#5f5f57` | `#aaa99f` | 副文本 |
| `--text-muted` | `#8b8b83` | `#7a7a72` | 弱化文本 |
| `--border` | `#d8d8d0` | `#383832` | 边框 |
| `--border-light` | `#e5e5de` | `#2a2a26` | 浅边框 |
| `--border-strong` | `#9c9c92` | `#626258` | 强调边框 |
| `--accent` | `#1f1f1f` | `#e6e6dc` | 强调色 |
| `--accent-text` | `#ffffff` | `#11110f` | 强调色上文本 |
| `--danger` | `#8f1d1d` | `#e07a7a` | 危险/错误 |
| `--danger-bg` | `#fce9e9` | `#2e1818` | 危险背景 |
| `--warning` | `#7a5a00` | `#d0a84a` | 警告 |
| `--warning-bg` | `#fcf3dc` | `#2e2410` | 警告背景 |
| `--success` | `#236b3a` | `#7fc78e` | 成功 |
| `--success-bg` | `#e4f0e8` | `#1a2e1e` | 成功背景 |
| `--info` | `#1a5a7a` | `#7ab8d0` | 信息 |
| `--info-bg` | `#e4eef2` | `#1a2a30` | 信息背景 |
| `--focus` | `#000000` | `#f2f2ea` | 焦点环色 |
| `--header-bg` | `#1f1f1f` | `#181816` | 顶栏背景 |
| `--header-text` | `#ecece5` | `#ecece5` | 顶栏文本 |
| `--sider-bg` | `#ffffff` | `#181816` | 侧栏背景 |
| `--sider-border` | `#d8d8d0` | `#383832` | 侧栏边框 |
| `--shadow-sm` | `0 1px 2px rgba(0,0,0,0.04)` | `0 1px 2px rgba(0,0,0,0.2)` | 小阴影 |
| `--shadow-md` | `0 2px 8px rgba(0,0,0,0.06)` | `0 2px 8px rgba(0,0,0,0.3)` | 中阴影 |

### CSS 变量 — 圆角

| Token | 值 | 用途 |
|-------|-----|------|
| `--radius-sm` | `2px` | 小圆角 |
| `--radius-md` | `4px` | 默认圆角 |
| `--radius-lg` | `6px` | 大圆角 |

---

## 排版系统

### 字体栈

```css
--font-sans: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
--font-mono: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
```

不引入任何外部 Web Font。

### 字号体系

| CSS 变量 | 值 | Ant Design Token 等价 | 典型用途 |
|----------|-----|----------------------|----------|
| `--font-size-xs` | `12px` | — | 状态标签、时间戳、元数据 |
| `--font-size-sm` | `13px` | — | 副标题、描述文字、表格说明 |
| `--font-size-base` | `14px` | `fontSize` | 正文、按钮、表格内容 |
| `--font-size-lg` | `15px` | — | 区段小标题、大按钮文字 |
| `--font-size-h4` | `16px` | `fontSizeHeading4` | H4 标题、卡片标题 |
| `--font-size-h3` | `18px` | `fontSizeHeading3` | H3 标题、页面标题 |
| `--font-size-h2` | `20px` | `fontSizeHeading2` | H2 标题 |
| `--font-size-h1` | `24px` | `fontSizeHeading1` | H1 标题、统计数据 |
| `--font-size-stat` | `24px` | `Statistic.contentFontSize` | 统计数字 |

### 行高

| 用途 | 值 |
|------|-----|
| 正文 (`body`) | `1.5` |
| 标题 (`h1-h6`) | `1.3` |
| 状态标签 (`.status`) | `1.6` |
| H1 line-height | `1.25` |
| H2 line-height | `1.3` |
| H3 line-height | `1.35` |
| 中度中文排版 | `1.6`–`1.65` |

### 字重

| 值 | 用途 |
|-----|------|
| `400` (normal) | 正文、副文本 |
| `500` (medium) | 状态标签、按钮文字 |
| `600` (semibold) | 标题、强调文字、统计数字 |

### Ant Design 排版 Token

```ts
// theme.ts 中的定义
fontSize: 14            // 基础字号
fontSizeHeading1: 24    // H1
fontSizeHeading2: 20    // H2
fontSizeHeading3: 18    // H3
fontSizeHeading4: 16    // H4
fontWeightStrong: 600   // 粗体
lineHeight: 1.5         // 基础行高
lineHeightHeading1: 1.25
lineHeightHeading2: 1.3
lineHeightHeading3: 1.35
```

### 组件级字号 (Button)

| Token | 值 |
|-------|-----|
| `Button.contentFontSize` | `14px` (默认) |
| `Button.contentFontSizeLG` | `15px` (大按钮) |
| `Button.contentFontSizeSM` | `13px` (小按钮) |

### 其他组件 Token

| 组件 | Token | 值 |
|------|-------|-----|
| Tag | `lineHeight` | `1.6` |
| Tabs | `titleFontSize` | `14px` |
| Tabs (LG) | `titleFontSizeLG` | `15px` |
| Badge | `textFontSize` | `11px` |
| Statistic | `titleFontSize` | `13px` |
| Statistic | `contentFontSize` | `24px` |
| Empty | `fontSize` | `14px` |

---

## CSS 工具类

### 页面头部

```html
<div class="page-header">
  <div class="page-header-left">
    <div class="accent-bar" />
    <h1 class="page-header-title">页面标题</h1>
    <p class="page-header-subtitle">副标题说明</p>
  </div>
  <div class="page-header-actions">
    <!-- 操作按钮 -->
  </div>
</div>
```

- `.page-header-title`: `font-size: 18px; font-weight: 600`
- `.page-header-subtitle`: `font-size: 13px; color: var(--text-secondary)`
- `.accent-bar`: 3px 宽竖条，用于标题左侧强调

### 状态标签

```html
<span class="status status-success">正常</span>
<span class="status status-warning">待审核</span>
<span class="status status-danger">异常</span>
<span class="status status-info">信息</span>
<span class="status status-default">默认</span>
```

- 统一 `font-size: 12px; font-weight: 500; line-height: 1.6`

### 数字显示

```html
<span class="number-display">1234</span>
```

- `font-variant-numeric: tabular-nums`（等宽数字对齐）

### 文本辅助

- `.text-muted`: `font-size: 13px; color: var(--text-secondary)`
- `.text-secondary`: `color: var(--text-secondary)`（不修改字号）

### 其他

- `.glass-panel`: 面板样式 (background + border)
- `.brass-divider`: 2px 高强调分隔线
- `.brass-divider--centered`: 居中版

---

## 间距

### Ant Design Spacing Tokens

| Token | 值 |
|-------|-----|
| `paddingContentVertical` | `16px` |
| `paddingContentHorizontal` | `24px` |
| `paddingLG` | `24px` |
| `paddingSM` | `12px` |
| `paddingXS` | `8px` |
| `marginLG` | `24px` |
| `marginSM` | `12px` |
| `marginXS` | `8px` |

### 控件高度

| Token | 值 |
|-------|-----|
| `controlHeight` | `36px` |
| `controlHeightLG` | `44px` |
| `controlHeightSM` | `28px` |

### 布局尺寸

| 元素 | 值 |
|------|-----|
| Header 高度 | `52px` |
| Sider 宽度 | `200px` |
| 内容区最大宽度 | `1400px` |
| 卡片 header padding | `12px 20px` |
| 卡片 body padding | `16px 20px` (默认) / `14px` (紧凑) |

---

## 约定与使用规则

### 文本用法

1. **中文优先**：导航、按钮、表格表头、状态标签、空状态、验证消息必须使用中文。
2. **正文一律使用 `font-size: 14px`**（继承 `body` 或 `--font-size-base`），不单独覆盖。
3. **副文本/描述**使用 `font-size: 13px`（`--font-size-sm`），搭配 `color: var(--text-secondary)`。
4. **状态/元数据**使用 `font-size: 12px`（`--font-size-xs`）。
5. **区段标题**使用 `font-size: 16px`（`font-size-h4` / `fontSizeHeading4`）。
6. **统计数据**使用 `font-size: 24px`（`--font-size-stat` / `fontSizeHeading1`）。
7. **代码/预格式化**使用 `font-family: var(--font-mono)`，禁止直接写 `"monospace"`。

### Header 布局规范

Header 是固定 52px 高的深色顶栏（`--header-bg: #1f1f1f`），包含左侧品牌区、右侧三个功能入口。

**结构规则：**

| 区域 | 内容 | 字号 | 约束 |
|------|------|------|------|
| 左侧 | 品牌名 + 分隔线 + 研究项目选择器 | `13px` | 品牌名 `fontWeight: 600`；StudySwitcher 无边框 |
| 右侧 | 日夜切换 / 语言选择 / 账户入口 | `13px` | 三个控件等宽等高等间距，无描边，贴靠排列 |

**三个入口控件必须满足：**

```
minWidth: 88        — 等宽
height: 52          — 撑满 Header，不溢出
fontSize: 13        — 与 Header 其他文字一致
padding: "0 12px"   — 左右内边距一致
border: "none"      — 无描边
boxSizing: "border-box"  — 防止内边距撑大
flexShrink: 0       — 不因空间不足而压缩
```

**容器规则：**

```tsx
// 三个控件的外层容器
<div style={{ display: "flex", alignItems: "stretch", gap: 0 }}>
  {/* 控件之间无间隙，紧密排列 */}
</div>
```

**Header 根元素规则：**

```tsx
<Header style={{
  height: 52,
  lineHeight: "normal",   // 覆盖 Ant Design 默认 line-height（64px）
  overflow: "hidden",     // 任何溢出直接裁剪
  display: "flex",
  alignItems: "center",
}}>
```

**Ant Design Select 嵌入 Header 的覆盖规则：**

语言选择器使用 `variant="borderless"` 但仍需 CSS 覆盖其内部 `.ant-select-selector` 的默认样式：

```css
.header-lang-select {
  padding: 0 !important;
}
.header-lang-select .ant-select-selector {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
  padding: 0 12px !important;
}
.header-lang-select .ant-select-selection-item {
  line-height: 52px !important;
  text-align: center !important;
}
```

**禁止：**
- ❌ 控件之间使用 `Space` 组件（会引入不可控间隙）
- ❌ 语言选择器外层包裹额外的 `<div>`（破坏等宽对齐）
- ❌ 在 Header 上设置 `lineHeight: "52px"`（Ant Design 默认 `64px` 会向下层叠）

### ModuleCard 规范

Dashboard 的快捷操作卡片。每个卡片显示模块名称和描述，**不显示内部路径**：

```tsx
<Card hoverable onClick={() => navigate(mod.path)}>
  <div style={{ fontWeight: 600, fontSize: 14 }}>{mod.name}</div>
  <div style={{ color: "var(--text-secondary)", fontSize: 12, marginTop: 4 }}>
    {mod.description}
  </div>
  {/* 不渲染 mod.path — 避免暴露内部路由细节 */}
</Card>
```

### 系统状态显示规范

系统配置页面（`/app/admin/system`）的状态数据**必须通过 REST API 获取**，禁止直接调用 `/actuator/*` 端点（该路径被 Caddy 限制为仅 localhost 可访问）。

| 数据 | 来源 API | 映射 |
|------|----------|------|
| 系统状态 | `/api/v1/dashboard/health` 或 `/api/v1/dashboard/status` 可用性 | 能成功获取 → "正常"，失败 → "异常" |
| 数据库状态 | `/api/v1/dashboard/status` → `database` 字段 | `"normal"` → "正常"，`"error"` → "异常" |
| 组件详情 | `/api/v1/dashboard/health` → `components` 字段 | `status: "UP"` → "正常"，`"DOWN"` → "异常" |

所有 API 调用必须使用 `apiClient`（来自 `@/api/client`），禁止使用裸 `fetch()`，以确保携带 session cookie 和 CSRF token。

### 禁止的行为

- ❌ 禁止在 inline style 中硬编码 `fontSize`、`fontWeight` 等排版值（优先使用 CSS 变量或主题 token）
- ❌ 禁止硬编码颜色值在排版元素中（必须使用 `var(--text)`、`var(--text-secondary)` 等）
- ❌ 禁止引入外部字体、图标库、图片
- ❌ 禁止 CSS-in-JS（styled-components / emotion）
- ❌ 禁止装饰性动画和 hover 变换

### 推荐的 TextStyle 定义方式

```tsx
// 优先使用 Ant Design Typography 组件
<Title level={4}>区段标题</Title>
<Text>正文内容</Text>
<Text type="secondary" style={{ fontSize: 13 }}>描述文字</Text>

// 或使用 CSS 变量
<div style={{ fontSize: "var(--font-size-sm)", color: "var(--text-secondary)" }}>
  描述文字
</div>
```

---

## 文件引用

| 文件 | 角色 |
|------|------|
| `frontend/src/styles/global.css` | CSS 变量、主题颜色、排版基础、工具类 |
| `frontend/src/styles/theme.ts` | Ant Design ThemeConfig（daylight + night） |
| `frontend/src/providers/ThemeProvider.tsx` | 主题切换逻辑、ConfigProvider 包裹 |
| `frontend/src/providers/AppProviders.tsx` | Provider 组合 |

---

## 维护说明

- 新增字号时，先在 `global.css` 的 `:root` 中添加 `--font-size-*` 变量，再在 `theme.ts` 中添加对应的 Ant Design token（如适用）。
- 新增语义颜色时，同时在 `global.css` 的 `daylight` 和 `night` 区块中添加对应的变量对。
- 不要单独在组件中定义新字号值——所有字号必须从 CSS 变量或主题 token 派生。
- 运行 `pnpm typecheck` 和 `pnpm test --run` 确认修改不会引入类型或功能错误。
