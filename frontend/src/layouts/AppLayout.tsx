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

  const selectedKey = (() => {
    if (location.pathname.startsWith("/app/randomization")) return "/app/randomization";
    return ((menuItems ?? []) as { key: string }[]).find(
      (item) => item.key !== "/app/dashboard" && location.pathname.startsWith(item.key),
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
          padding: "0 24px",
          height: 56,
          lineHeight: "56px",
        }}
      >
        <Space size="middle">
          <MedicineBoxOutlined style={{ fontSize: 24, color: "#fff" }} />
          <span style={{ color: "#fff", fontSize: 18, fontWeight: 600 }}>
            OpenClinica
          </span>
          <StudySwitcher />
        </Space>
        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
          <Button
            type="text"
            style={{ color: "rgba(255,255,255,0.85)", height: 56 }}
          >
            <Space>
              <UserOutlined />
              {user?.name ?? "User"}
              <DownOutlined />
            </Space>
          </Button>
        </Dropdown>
      </Header>
      <Layout>
        <Sider
          width={220}
          style={{ background: token.colorBgContainer }}
          breakpoint="lg"
          collapsedWidth={0}
        >
          <Menu
            mode="inline"
            selectedKeys={[selectedKey]}
            items={menuItems}
            onClick={({ key }) => {
              navigate(key);
            }}
            style={{ height: "100%", borderRight: 0, paddingTop: 8 }}
          />
        </Sider>
        <Content style={{ padding: 24, background: token.colorBgLayout }}>
          <Suspense fallback={<SkeletonPage />}>
            <Outlet />
          </Suspense>
        </Content>
      </Layout>
    </Layout>
  );
}
