import { Suspense } from "react";
import { Outlet } from "react-router-dom";
import { Layout, Menu, Dropdown, Space, Select } from "antd";
import type { MenuProps } from "antd";
import { useTranslation } from "react-i18next";
import { useAuth } from "@/providers/AuthProvider";
import { useTheme } from "@/providers/ThemeProvider";
import { useNavigate, useLocation } from "react-router-dom";
import { usePermissions } from "@/hooks/usePermissions";
import StudySwitcher from "@/components/StudySwitcher";
import { SkeletonPage } from "@/components/SkeletonCard";
import type { Permission } from "@/types/user";
import { SUPPORTED_LANGUAGES } from "@/i18n";

const { Header, Sider, Content } = Layout;

function useMenuItems(): MenuProps["items"] {
  const { t } = useTranslation();
  const permissions = usePermissions();
  const has = (p: Permission) => permissions.includes(p);

  const items: NonNullable<MenuProps["items"]> = [
    { key: "/app/dashboard", label: t("layout.dashboard") },
  ];

  if (has("study:view")) {
    items.push({ key: "/app/studies", label: t("layout.studies") });
  }
  if (has("subject:view")) {
    items.push({ key: "/app/subjects", label: t("layout.subjects") });
  }
  if (has("crf:design")) {
    items.push({ key: "/app/crfs", label: t("layout.crfs") });
  }
  if (has("crf:design")) {
    items.push({
      key: "questionnaires",
      label: t("layout.questionnaires"),
      children: [
        { key: "/app/questionnaires/templates", label: t("layout.templates") },
        { key: "/app/questionnaires/assignments", label: t("layout.assignments") },
        { key: "/app/questionnaires/responses", label: t("layout.responses") },
        { key: "/app/questionnaires/export", label: t("layout.export") },
      ],
    });
  }
  if (has("data:export")) {
    items.push({ key: "/app/data-export", label: t("layout.dataExport") });
  }
  if (has("randomization:view")) {
    items.push({ key: "/app/randomization", label: t("layout.randomization") });
  }
  if (has("audit:view")) {
    items.push({ key: "/app/audit-log", label: t("layout.auditLog") });
  }
  if (has("admin:access")) {
    items.push({ key: "/app/admin", label: t("layout.admin") });
  }

  return items;
}

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const menuItems = useMenuItems();
  const { i18n, t } = useTranslation();
  const { mode, toggleTheme } = useTheme();

  const defaultOpenKeys = location.pathname.startsWith("/app/questionnaires")
    ? ["questionnaires"]
    : [];

  const selectedKey = (() => {
    const path = location.pathname;
    if (path.startsWith("/app/randomization")) return "/app/randomization";
    if (path.startsWith("/app/questionnaires")) return "/app/questionnaires/templates";
    return (menuItems?.filter((item) => item && typeof item === "object" && "key" in item) as { key: string }[])?.find(
      (item) => item.key !== "/app/dashboard" && path.startsWith(item.key),
    )?.key ?? "/app/dashboard";
  })();

  const userMenuItems: MenuProps["items"] = [
    {
      key: "profile",
      label: user?.email ?? (`${user?.firstName ?? ""} ${user?.lastName ?? ""}`.trim() || user?.username),
      disabled: true,
    },
    { type: "divider" },
    { key: "profile-page", label: t("layout.accountSettings"), onClick: () => navigate("/app/profile") },
    { type: "divider" },
    {
      key: "logout",
      label: t("layout.logout"),
      onClick: () => {
        logout();
        navigate("/login");
      },
    },
  ];

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Header
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "0 20px",
          height: 52,
          lineHeight: "normal",
          background: "var(--header-bg)",
          position: "relative",
          zIndex: 10,
          borderBottom: "1px solid var(--border)",
          overflow: "hidden",
        }}
      >
        <Space size="middle">
          <div style={{ fontWeight: 600, fontSize: 13, color: "var(--header-text)", letterSpacing: "0.02em" }}>
            ResearchEDC
          </div>
          <div style={{ borderLeft: "1px solid var(--border)", height: 20, width: 1 }} />
          <StudySwitcher />
        </Space>
        <div style={{ display: "flex", alignItems: "stretch", gap: 0 }}>
          <button
            onClick={toggleTheme}
            style={{
              background: "var(--header-bg)",
              border: "none",
              cursor: "pointer",
              color: "var(--header-text)",
              fontSize: 13,
              height: 52,
              minWidth: 88,
              padding: "0 12px",
              lineHeight: "52px",
              fontFamily: "inherit",
              textAlign: "center",
              boxSizing: "border-box",
              margin: 0,
              flexShrink: 0,
            }}
          >
            {mode === "daylight" ? t("layout.themeNight") : t("layout.themeDay")}
          </button>
          <Select
            value={i18n.language?.startsWith("zh") ? "zh" : "en"}
            onChange={(lng) => { i18n.changeLanguage(lng); }}
            variant="borderless"
            className="header-lang-select"
            style={{
              color: "var(--header-text)",
              fontSize: 13,
              height: 52,
              minWidth: 88,
              textAlign: "center",
              flexShrink: 0,
            }}
            popupMatchSelectWidth={false}
            options={SUPPORTED_LANGUAGES.map((l) => ({
              value: l.key,
              label: l.label,
            }))}
          />
          <Dropdown
            menu={{ items: userMenuItems }}
            placement="bottomRight"
            popupRender={(menu) => (
              <div className="header-user-dropdown">{menu}</div>
            )}
          >
            <button
              style={{
                background: "var(--header-bg)",
                border: "none",
                cursor: "pointer",
                color: "var(--header-text)",
                fontSize: 13,
                height: 52,
                minWidth: 88,
                padding: "0 12px",
                lineHeight: "52px",
                fontFamily: "inherit",
                textAlign: "center",
                boxSizing: "border-box",
                margin: 0,
                flexShrink: 0,
              }}
            >
              {user?.firstName ?? user?.username ?? t("common.user")}
            </button>
          </Dropdown>
        </div>
      </Header>
      <Layout>
        <Sider
          width={200}
          style={{
            background: "var(--sider-bg)",
            borderRight: "1px solid var(--sider-border)",
          }}
          breakpoint="lg"
          collapsedWidth={0}
        >
          <Menu
            mode="inline"
            selectedKeys={[selectedKey]}
            defaultOpenKeys={defaultOpenKeys}
            items={menuItems}
            onClick={({ key }) => {
              navigate(key);
            }}
            style={{
              height: "100%",
              borderRight: 0,
              paddingTop: 12,
              paddingBottom: 12,
            }}
          />
        </Sider>
        <Content
          style={{
            padding: 24,
            background: "var(--bg-layout)",
            minHeight: "calc(100vh - 52px)",
          }}
        >
          <div
            style={{
              maxWidth: 1400,
              margin: "0 auto",
              width: "100%",
            }}
          >
            <Suspense fallback={<SkeletonPage />}>
              <Outlet />
            </Suspense>
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}
