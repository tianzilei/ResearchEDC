import { Outlet } from "react-router-dom";
import { Layout, Menu, Button, theme as antTheme } from "antd";
import {
  DashboardOutlined,
  UserOutlined,
  LogoutOutlined,
  MedicineBoxOutlined,
} from "@ant-design/icons";
import { useAuth } from "@/providers/AuthProvider";
import { useNavigate } from "react-router-dom";

const { Header, Sider, Content } = Layout;

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { token } = antTheme.useToken();

  const menuItems = [
    {
      key: "/app/dashboard",
      icon: <DashboardOutlined />,
      label: "Dashboard",
    },
    {
      key: "/app/studies",
      icon: <MedicineBoxOutlined />,
      label: "Studies",
      disabled: true,
    },
    {
      key: "/app/subjects",
      icon: <UserOutlined />,
      label: "Subjects",
      disabled: true,
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
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <MedicineBoxOutlined style={{ fontSize: 24, color: "#fff" }} />
          <span style={{ color: "#fff", fontSize: 18, fontWeight: 600 }}>
            OpenClinica
          </span>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
          {user && (
            <span style={{ color: "rgba(255,255,255,0.85)" }}>
              {user.name}
            </span>
          )}
          <Button
            type="text"
            icon={<LogoutOutlined />}
            onClick={logout}
            style={{ color: "rgba(255,255,255,0.85)" }}
          >
            Logout
          </Button>
        </div>
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
            defaultSelectedKeys={["/app/dashboard"]}
            items={menuItems}
            onClick={({ key }) => {
              navigate(key);
            }}
            style={{ height: "100%", borderRight: 0 }}
          />
        </Sider>
        <Content style={{ padding: 24, background: token.colorBgLayout }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
