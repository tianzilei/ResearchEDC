import { Suspense } from "react";
import { Outlet } from "react-router-dom";
import { Layout, Menu, Button, theme as antTheme, Dropdown, Space, Select } from "antd";
import type { MenuProps } from "antd";
import {
  DashboardOutlined,
  UserOutlined,
  LogoutOutlined,
  MedicineBoxOutlined,
  FileTextOutlined,
  ExportOutlined,
  SafetyOutlined,
  AuditOutlined,
  SettingOutlined,
  DownOutlined,
  FormOutlined,
  LinkOutlined,
  CheckCircleOutlined,
  GlobalOutlined,
} from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import { useAuth } from "@/providers/AuthProvider";
import { useNavigate, useLocation } from "react-router-dom";
import { usePermissions } from "@/hooks/usePermissions";
import StudySwitcher from "@/components/StudySwitcher";
import { SkeletonPage } from "@/components/SkeletonCard";
import type { Permission } from "@/types/user";
import { SUPPORTED_LANGUAGES } from "@/i18n";

const { Header, Sider, Content } = Layout;

function useMenuItems(): MenuProps["items"] {
  const permissions = usePermissions();
  const has = (p: Permission) => permissions.includes(p);
  const { t } = useTranslation();

  const items: NonNullable<MenuProps["items"]> = [
    { key: "/app/dashboard", icon: <DashboardOutlined />, label: t("layout.dashboard") },
  ];

  if (has("study:view")) {
    items.push({ key: "/app/studies", icon: <MedicineBoxOutlined />, label: t("layout.studies") });
  }
  if (has("subject:view")) {
    items.push({ key: "/app/subjects", icon: <UserOutlined />, label: t("layout.subjects") });
  }
  if (has("crf:design")) {
    items.push({ key: "/app/crfs", icon: <FileTextOutlined />, label: t("layout.crfs") });
  }
  if (has("crf:design")) {
    items.push({
      key: "questionnaires",
      icon: <FormOutlined />,
      label: t("layout.questionnaires"),
      children: [
        { key: "/app/questionnaires/templates", icon: <FileTextOutlined />, label: t("layout.templates") },
        { key: "/app/questionnaires/assignments", icon: <LinkOutlined />, label: t("layout.assignments") },
        { key: "/app/questionnaires/responses", icon: <CheckCircleOutlined />, label: t("layout.responses") },
        { key: "/app/questionnaires/export", icon: <ExportOutlined />, label: t("layout.export") },
      ],
    } satisfies NonNullable<MenuProps["items"]>[number]);
  }
  if (has("data:export")) {
    items.push({ key: "/app/data-export", icon: <ExportOutlined />, label: t("layout.dataExport") });
  }
  if (has("randomization:view")) {
    items.push({ key: "/app/randomization", icon: <SafetyOutlined />, label: t("layout.randomization") });
  }
  if (has("audit:view")) {
    items.push({ key: "/app/audit-log", icon: <AuditOutlined />, label: t("layout.auditLog") });
  }
  if (has("admin:access")) {
    items.push({ key: "/app/admin", icon: <SettingOutlined />, label: t("layout.admin") });
  }

  return items;
}

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { token } = antTheme.useToken();
  const menuItems = useMenuItems();
  const { t, i18n } = useTranslation();

  const defaultOpenKeys = location.pathname.startsWith("/app/questionnaires")
    ? ["questionnaires"]
    : [];

  const selectedKey = (() => {
    const path = location.pathname;
    if (path.startsWith("/app/randomization")) return "/app/randomization";
    if (path.startsWith("/app/questionnaires")) return "/app/questionnaires/templates";
    return ((menuItems ?? []) as { key: string }[]).find(
      (item) => item.key !== "/app/dashboard" && path.startsWith(item.key),
    )?.key ?? "/app/dashboard";
  })();

  const userMenuItems: MenuProps["items"] = [
    {
      key: "profile",
      label: user?.email ?? user?.name,
      disabled: true,
    },
    { type: "divider" },
    {
      key: "logout",
      icon: <LogoutOutlined />,
      label: t("layout.logout"),
      onClick: () => {
        logout();
        void navigate("/login");
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
          padding: "0 28px",
          height: 60,
          lineHeight: "60px",
          borderBottom: "1.5px solid rgba(212,168,84,0.30)",
          background: "#0F1A2E",
          backgroundImage:
            "linear-gradient(180deg, rgba(255,255,255,0.03) 0%, transparent 100%)",
          position: "relative",
          zIndex: 10,
        }}
      >
        <Space size="middle">
          <div
            style={{
              width: 36,
              height: 36,
              borderRadius: "50%",
              border: "1.5px solid rgba(212,168,84,0.4)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              background: "rgba(212,168,84,0.08)",
              flexShrink: 0,
            }}
          >
            <MedicineBoxOutlined style={{ fontSize: 18, color: "#099A87" }} />
          </div>
          <span
            style={{
              color: "#F8F5F0",
              fontSize: 17,
              fontWeight: 500,
              fontFamily: "'DM Sans', sans-serif",
              letterSpacing: "0.04em",
              textShadow: "0 1px 2px rgba(0,0,0,0.2)",
            }}
          >
            OpenClinica
          </span>
          <div style={{ borderLeft: "1px solid rgba(212,168,84,0.2)", height: 28, width: 1 }} />
          <StudySwitcher />
        </Space>
        <Space size="middle">
          <Select
            value={i18n.language?.startsWith("zh") ? "zh" : "en"}
            onChange={(lng) => { void i18n.changeLanguage(lng); }}
            size="small"
            variant="borderless"
            style={{
              minWidth: 80,
              color: "rgba(248,245,240,0.75)",
              fontFamily: "'DM Sans', sans-serif",
              fontSize: 12,
            }}
            popupMatchSelectWidth={false}
            suffixIcon={<GlobalOutlined style={{ color: "rgba(248,245,240,0.5)", fontSize: 13 }} />}
            options={SUPPORTED_LANGUAGES.map((l) => ({
              value: l.key,
              label: l.label,
            }))}
          />
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <Button
              type="text"
              style={{
                color: "rgba(248,245,240,0.85)",
                height: 60,
                fontFamily: "'DM Sans', sans-serif",
                fontSize: 13,
                letterSpacing: "0.02em",
              }}
            >
              <Space>
                <UserOutlined style={{ color: "#D4A854", fontSize: 14 }} />
                {user?.name ?? "User"}
                <DownOutlined style={{ fontSize: 9, opacity: 0.6 }} />
              </Space>
            </Button>
          </Dropdown>
        </Space>
      </Header>
      <Layout>
        <Sider
          width={220}
          style={{
            background: token.colorBgContainer,
            borderRight: "1px solid #EDE8E0",
            boxShadow: "inset 0 2px 4px rgba(15,26,46,0.04)",
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
              paddingTop: 16,
              paddingBottom: 16,
            }}
          />
        </Sider>
        <Content
          style={{
            padding: 28,
            background: token.colorBgLayout,
            backgroundImage:
              "radial-gradient(circle, #D9D4CA 0.6px, transparent 0.6px)",
            backgroundSize: "28px 28px",
            minHeight: "calc(100vh - 60px)",
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
              <div className="animate-in" style={{ animationDuration: "0.5s" }}>
                <Outlet />
              </div>
            </Suspense>
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}
