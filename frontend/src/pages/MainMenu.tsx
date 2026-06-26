import { Card, Col, Row, Typography, Space, Layout } from "antd";
import { useAuth } from "@/providers/AuthProvider";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

const { Title, Text } = Typography;
const { Content } = Layout;

interface NavItem {
  key: string;
  labelKey: string;
  route: string;
}

const NAV_ITEMS: NavItem[] = [
  { key: "dashboard", labelKey: "layout.dashboard", route: "/app/dashboard" },
  { key: "studies", labelKey: "layout.studies", route: "/app/studies" },
  { key: "subjects", labelKey: "layout.subjects", route: "/app/subjects" },
  { key: "crfs", labelKey: "layout.crfs", route: "/app/crfs" },
  { key: "questionnaires", labelKey: "layout.questionnaires", route: "/app/questionnaires/templates" },
  { key: "export", labelKey: "layout.dataExport", route: "/app/data-export" },
  { key: "randomization", labelKey: "layout.randomization", route: "/app/randomization" },
  { key: "audit", labelKey: "layout.auditLog", route: "/app/audit-log" },
  { key: "admin", labelKey: "layout.admin", route: "/app/admin" },
];

function AuthenticatedMenu() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();

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
          {t("main.welcome", { name: user?.firstName ?? user?.username ?? t("common.user") })}
        </Title>
        <Text
          style={{
            color: "var(--text-muted)",
            fontSize: 14,
            marginTop: 4,
            display: "block",
          }}
        >
          {t("main.chooseModule")}
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
                    {t(item.labelKey)}
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
          {t("app.footer")}
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
