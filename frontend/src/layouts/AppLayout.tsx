import { Suspense } from "react";
import { Outlet } from "react-router-dom";
import { Layout, Menu, Button, theme as antTheme, Dropdown, Space } from "antd";
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
} from "@ant-design/icons";
import { useAuth } from "@/providers/AuthProvider";
import { useNavigate, useLocation } from "react-router-dom";
import { usePermissions } from "@/hooks/usePermissions";
import StudySwitcher from "@/components/StudySwitcher";
import { SkeletonPage } from "@/components/SkeletonCard";
import type { Permission } from "@/types/user";

const { Header, Sider, Content } = Layout;

function useMenuItems(): MenuProps["items"] {
  const permissions = usePermissions();
  const has = (p: Permission) => permissions.includes(p);

  const items: NonNullable<MenuProps["items"]> = [
    { key: "/app/dashboard", icon: <DashboardOutlined />, label: "Dashboard" },
  ];

  if (has("study:view")) {
    items.push({ key: "/app/studies", icon: <MedicineBoxOutlined />, label: "Studies" });
  }
  if (has("subject:view")) {
    items.push({ key: "/app/subjects", icon: <UserOutlined />, label: "Subjects" });
  }
  if (has("crf:design")) {
    items.push({ key: "/app/crfs", icon: <FileTextOutlined />, label: "CRFs" });
  }
  if (has("crf:design")) {
    items.push({
      key: "questionnaires",
      icon: <FormOutlined />,
      label: "Questionnaires",
      children: [
        { key: "/app/questionnaires/templates", icon: <FileTextOutlined />, label: "Templates" },
        { key: "/app/questionnaires/assignments", icon: <LinkOutlined />, label: "Assignments" },
        { key: "/app/questionnaires/responses", icon: <CheckCircleOutlined />, label: "Responses" },
        { key: "/app/questionnaires/export", icon: <ExportOutlined />, label: "Export" },
      ],
    } satisfies NonNullable<MenuProps["items"]>[number]);
  }
  if (has("data:export")) {
    items.push({ key: "/app/data-export", icon: <ExportOutlined />, label: "Data Export" });
  }
  if (has("randomization:view")) {
    items.push({ key: "/app/randomization", icon: <SafetyOutlined />, label: "Randomization" });
  }
  if (has("audit:view")) {
    items.push({ key: "/app/audit-log", icon: <AuditOutlined />, label: "Audit Log" });
  }
  if (has("admin:access")) {
    items.push({ key: "/app/admin", icon: <SettingOutlined />, label: "Admin" });
  }

  return items;
}

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { token } = antTheme.useToken();
  const menuItems = useMenuItems();

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
      label: "Logout",
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
          borderBottom: "1px solid rgba(212,168,84,0.25)",
        }}
      >
        <Space size="middle">
          <MedicineBoxOutlined style={{ fontSize: 22, color: "#099A87" }} />
          <span
            style={{
              color: "#F8F5F0",
              fontSize: 17,
              fontWeight: 500,
              fontFamily: "'DM Sans', sans-serif",
              letterSpacing: "0.02em",
            }}
          >
            OpenClinica
          </span>
          <StudySwitcher />
        </Space>
        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
          <Button
            type="text"
            style={{
              color: "rgba(248,245,240,0.8)",
              height: 60,
              fontFamily: "'DM Sans', sans-serif",
            }}
          >
            <Space>
              <UserOutlined style={{ color: "#D4A854" }} />
              {user?.name ?? "User"}
              <DownOutlined style={{ fontSize: 10 }} />
            </Space>
          </Button>
        </Dropdown>
      </Header>
      <Layout>
        <Sider
          width={220}
          style={{
            background: token.colorBgContainer,
            borderRight: "1px solid #EDE8E0",
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
              transition: "background 0.2s",
            }}
          />
        </Sider>
        <Content
          style={{
            padding: 28,
            background: token.colorBgLayout,
            backgroundImage:
              "radial-gradient(circle, #D9D4CA 0.8px, transparent 0.8px)",
            backgroundSize: "24px 24px",
          }}
        >
          <Suspense fallback={<SkeletonPage />}>
            <div className="animate-in" style={{ animationDuration: "0.45s" }}>
              <Outlet />
            </div>
          </Suspense>
        </Content>
      </Layout>
    </Layout>
  );
}
