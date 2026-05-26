import { Card, Col, Row, Typography, Space, Layout } from "antd";
import { useAuth } from "@/providers/AuthProvider";
import { useNavigate } from "react-router-dom";

const { Title, Text } = Typography;
const { Content } = Layout;

interface NavItem {
  key: string;
  label: string;
  route: string;
}

const NAV_ITEMS: NavItem[] = [
  { key: "dashboard", label: "总览", route: "/app/dashboard" },
  { key: "studies", label: "项目", route: "/app/studies" },
  { key: "subjects", label: "受试者", route: "/app/subjects" },
  { key: "crfs", label: "CRF 库", route: "/app/crfs" },
  { key: "questionnaires", label: "问卷", route: "/app/questionnaires/templates" },
  { key: "export", label: "数据导出", route: "/app/data-export" },
  { key: "randomization", label: "随机", route: "/app/randomization" },
  { key: "audit", label: "审计日志", route: "/app/audit-log" },
  { key: "admin", label: "管理", route: "/app/admin" },
];

function AuthenticatedMenu() {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        background: "var(--header-bg)",
        overflow: "auto",
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          padding: "40px 24px 0",
        }}
      >
        <Space size={12}>
          <span
            style={{
              color: "var(--header-text)",
              fontSize: 15,
              fontWeight: 600,
              letterSpacing: "0.02em",
            }}
          >
            ResearchEDC
          </span>
        </Space>
      </div>

      <div
        style={{
          width: 32,
          height: 2,
          background: "var(--border-strong)",
          margin: "16px auto 0",
        }}
      />

      <div
        style={{
          textAlign: "center",
          padding: "28px 24px 8px",
        }}
      >
        <Title
          level={3}
          style={{
            margin: 0,
            fontWeight: 600,
            color: "var(--header-text)",
          }}
        >
          欢迎，{user?.firstName ?? user?.username ?? "用户"}
        </Title>
        <Text
          style={{
            color: "var(--text-muted)",
            fontSize: 14,
            marginTop: 4,
            display: "block",
          }}
        >
          请选择一个模块开始使用
        </Text>
      </div>

      <Content
        style={{
          flex: 1,
          display: "flex",
          justifyContent: "center",
          padding: "24px",
        }}
      >
        <div style={{ maxWidth: 960, width: "100%" }}>
          <Row gutter={[12, 12]}>
            {NAV_ITEMS.map((item) => (
              <Col key={item.key} xs={24} sm={12} md={8}>
                <Card
                  hoverable
                  onClick={() => navigate(item.route)}
                  style={{
                    border: "1px solid var(--border)",
                    background: "var(--panel)",
                    cursor: "pointer",
                  }}
                  styles={{ body: { padding: 20 } }}
                >
                  <div style={{ fontWeight: 600, fontSize: 16, color: "var(--text)" }}>
                    {item.label}
                  </div>
                </Card>
              </Col>
            ))}
          </Row>
        </div>
      </Content>

      <div
        style={{
          textAlign: "center",
          padding: "24px",
        }}
      >
        <Text
          style={{
            color: "var(--text-muted)",
            fontSize: 12,
          }}
        >
          ResearchEDC — 临床研究数据管理平台
        </Text>
      </div>
    </div>
  );
}

export default function MainMenu() {
  const { isAuthenticated, isInitialized } = useAuth();

  if (!isInitialized) {
    return (
      <div
        style={{
          minHeight: "100vh",
          background: "var(--header-bg)",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      />
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return <AuthenticatedMenu />;
}