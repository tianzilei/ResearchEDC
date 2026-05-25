import { Card, Col, Row, Typography, Space, Layout } from "antd";
import {
  MedicineBoxOutlined,
  DashboardOutlined,
  UserOutlined,
  FileTextOutlined,
  ExportOutlined,
  SafetyOutlined,
  AuditOutlined,
  SettingOutlined,
  FormOutlined,
  RightOutlined,
} from "@ant-design/icons";
import { useAuth } from "@/providers/AuthProvider";
import { useNavigate } from "react-router-dom";

const { Title, Text } = Typography;
const { Content } = Layout;

interface NavItem {
  key: string;
  label: string;
  icon: React.ReactNode;
  route: string;
  color: string;
  bgColor: string;
  description: string;
}

const NAV_ITEMS: NavItem[] = [
  {
    key: "dashboard",
    label: "Dashboard",
    icon: <DashboardOutlined />,
    route: "/app/dashboard",
    color: "#099A87",
    bgColor: "rgba(9,154,135,0.10)",
    description: "Study overview and activity summary",
  },
  {
    key: "studies",
    label: "Studies",
    icon: <MedicineBoxOutlined />,
    route: "/app/studies",
    color: "#D4A854",
    bgColor: "rgba(212,168,84,0.10)",
    description: "Manage studies, sites, and event definitions",
  },
  {
    key: "subjects",
    label: "Subjects",
    icon: <UserOutlined />,
    route: "/app/subjects",
    color: "#066B5E",
    bgColor: "rgba(6,107,94,0.10)",
    description: "Manage subject enrollment and visit schedules",
  },
  {
    key: "crfs",
    label: "CRF Library",
    icon: <FileTextOutlined />,
    route: "/app/crfs",
    color: "#5B7FAB",
    bgColor: "rgba(91,127,171,0.10)",
    description: "Browse and preview Case Report Forms",
  },
  {
    key: "questionnaires",
    label: "Questionnaires",
    icon: <FormOutlined />,
    route: "/app/questionnaires/templates",
    color: "#7C5FA0",
    bgColor: "rgba(124,95,160,0.10)",
    description: "Create and manage questionnaire templates",
  },
  {
    key: "export",
    label: "Data Export",
    icon: <ExportOutlined />,
    route: "/app/data-export",
    color: "#C44A4A",
    bgColor: "rgba(196,74,74,0.10)",
    description: "Export study data in ODM XML, CSV, or Excel",
  },
  {
    key: "randomization",
    label: "Randomization",
    icon: <SafetyOutlined />,
    route: "/app/randomization",
    color: "#D4A854",
    bgColor: "rgba(212,168,84,0.10)",
    description: "Manage randomization schemes and allocation",
  },
  {
    key: "audit",
    label: "Audit Log",
    icon: <AuditOutlined />,
    route: "/app/audit-log",
    color: "#6B7280",
    bgColor: "rgba(107,114,128,0.10)",
    description: "Review audit trail and system events",
  },
  {
    key: "admin",
    label: "Admin",
    icon: <SettingOutlined />,
    route: "/app/admin",
    color: "#1A2740",
    bgColor: "rgba(26,39,64,0.08)",
    description: "User management, system configuration, and tools",
  },
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
        background: "#0F1A2E",
        backgroundImage:
          "radial-gradient(circle at 50% 20%, rgba(9,154,135,0.06) 0%, transparent 60%)",
        position: "relative",
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
            }}
          >
            <MedicineBoxOutlined style={{ fontSize: 18, color: "#099A87" }} />
          </div>
          <span
            style={{
              color: "#F8F5F0",
              fontSize: 18,
              fontWeight: 500,
              fontFamily: "'DM Sans', sans-serif",
              letterSpacing: "0.04em",
            }}
          >
            ResearchEDC
          </span>
        </Space>
      </div>

      <div
        style={{
          width: 40,
          height: 2,
          background: "rgba(212,168,84,0.3)",
          margin: "20px auto 0",
          borderRadius: 1,
        }}
      />

      <div
        style={{
          textAlign: "center",
          padding: "32px 24px 8px",
          animation: "fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) both",
        }}
      >
        <Title
          level={2}
          style={{
            margin: 0,
            fontFamily: "'Sora', sans-serif",
            fontWeight: 600,
            color: "#F8F5F0",
            fontSize: 26,
          }}
        >
          Welcome, {user?.firstName ?? user?.username ?? "User"}
        </Title>
        <Text
          style={{
            color: "rgba(248,245,240,0.55)",
            fontSize: 14,
            fontFamily: "'DM Sans', sans-serif",
            marginTop: 6,
            display: "block",
          }}
        >
          Select a module to get started
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
        <div style={{ maxWidth: 1000, width: "100%" }}>
          <Row gutter={[16, 16]}>
            {NAV_ITEMS.map((item, index) => (
              <Col key={item.key} xs={24} sm={12} md={8}>
                <Card
                  hoverable
                  className="card-hover"
                  onClick={() => navigate(item.route)}
                  style={{
                    borderRadius: 14,
                    border: "1px solid rgba(248,245,240,0.08)",
                    background: "rgba(248,245,240,0.04)",
                    backdropFilter: "blur(8px)",
                    WebkitBackdropFilter: "blur(8px)",
                    boxShadow: "0 2px 12px rgba(0,0,0,0.15)",
                    cursor: "pointer",
                    animation:
                      "fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) both",
                    animationDelay: `${0.1 + index * 0.06}s`,
                    transition:
                      "transform 0.25s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.25s cubic-bezier(0.22, 1, 0.36, 1)",
                  }}
                  styles={{
                    body: { padding: 24 },
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = "translateY(-4px)";
                    e.currentTarget.style.boxShadow =
                      "0 8px 32px rgba(0,0,0,0.25)";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = "translateY(0)";
                    e.currentTarget.style.boxShadow =
                      "0 2px 12px rgba(0,0,0,0.15)";
                  }}
                >
                  <Space
                    direction="vertical"
                    size={12}
                    style={{ width: "100%" }}
                  >
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                      }}
                    >
                      <div
                        style={{
                          width: 44,
                          height: 44,
                          borderRadius: 12,
                          background: item.bgColor,
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          color: item.color,
                          fontSize: 20,
                        }}
                      >
                        {item.icon}
                      </div>
                      <RightOutlined
                        style={{
                          fontSize: 12,
                          color: "rgba(248,245,240,0.25)",
                        }}
                      />
                    </div>
                    <div>
                      <div
                        style={{
                          fontFamily: "'Sora', sans-serif",
                          fontWeight: 600,
                          fontSize: 15,
                          color: "#F8F5F0",
                          marginBottom: 4,
                        }}
                      >
                        {item.label}
                      </div>
                      <div
                        style={{
                          color: "rgba(248,245,240,0.45)",
                          fontSize: 12,
                          lineHeight: 1.4,
                          fontFamily: "'DM Sans', sans-serif",
                        }}
                      >
                        {item.description}
                      </div>
                    </div>
                  </Space>
                </Card>
              </Col>
            ))}
          </Row>
        </div>
      </Content>

      <div
        style={{
          textAlign: "center",
          padding: "24px 24px 32px",
        }}
      >
        <Text
          style={{
            color: "rgba(248,245,240,0.25)",
            fontSize: 12,
            fontFamily: "'DM Sans', sans-serif",
          }}
        >
          ResearchEDC &mdash; Clinical Data Management Platform
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
          background: "#0F1A2E",
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
