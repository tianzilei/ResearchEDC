import { Card, Typography, Space, Button, Tag } from "antd";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/providers/AuthProvider";

const { Title, Text } = Typography;

export default function Enterprise() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

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
      <div style={{ maxWidth: 720, margin: "0 auto", padding: "60px 24px", width: "100%" }}>
        <Card
          style={{
            marginBottom: 24,
            textAlign: "center",
            border: "1px solid var(--border-light)",
          }}
        >
          <Title level={2} style={{ margin: 0, marginBottom: 8 }}>
            ResearchEDC
          </Title>
          <Text type="secondary" style={{ fontSize: 16 }}>
            Electronic Data Capture Platform
          </Text>
        </Card>

        <Card
          style={{
            marginBottom: 24,
            textAlign: "center",
            border: "1px solid var(--border-light)",
          }}
        >
          <Space direction="vertical" size="middle" style={{ width: "100%" }}>
            <div>
              <Text strong style={{ fontSize: 18 }}>
                {user?.firstName} {user?.lastName}
              </Text>
              <br />
              <Text type="secondary">{user?.email}</Text>
              {user?.roles && user.roles.length > 0 && (
                <div style={{ marginTop: 8 }}>
                  {user.roles.map((r) => (
                    <Tag key={r} style={{ marginRight: 4 }}>
                      {r}
                    </Tag>
                  ))}
                </div>
              )}
            </div>
            <Space size="middle">
              <Button type="primary" size="large" onClick={() => navigate("/app/dashboard")}>
                Dashboard
              </Button>
              <Button size="large" onClick={() => navigate("/app/studies")}>
                Studies
              </Button>
              <Button size="large" onClick={() => navigate("/app/subjects")}>
                Subjects
              </Button>
            </Space>
          </Space>
        </Card>

        <Card
          style={{
            textAlign: "center",
            border: "1px solid var(--border-light)",
          }}
        >
          <Space size="middle">
            <Button onClick={() => navigate("/app/profile")}>
              Profile
            </Button>
            <Button onClick={() => navigate("/app/admin")}>
              Admin
            </Button>
            <Button danger onClick={() => { logout(); navigate("/login"); }}>
              Logout
            </Button>
          </Space>
        </Card>
      </div>
    </div>
  );
}
