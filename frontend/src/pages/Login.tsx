import { Button, Card, Typography, Space } from "antd";
import { MedicineBoxOutlined } from "@ant-design/icons";
import { useAuth } from "@/providers/AuthProvider";
import { Navigate } from "react-router-dom";

const { Title, Text } = Typography;

export default function Login() {
  const { isAuthenticated, isInitialized, login } = useAuth();

  if (!isInitialized) {
    return null;
  }

  if (isAuthenticated) {
    return <Navigate to="/app/dashboard" replace />;
  }

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "#f0f2f5",
      }}
    >
      <Card style={{ width: 400, textAlign: "center" }}>
        <Space direction="vertical" size="large" style={{ width: "100%" }}>
          <MedicineBoxOutlined style={{ fontSize: 48, color: "#1677ff" }} />
          <div>
            <Title level={3} style={{ margin: 0 }}>
              OpenClinica
            </Title>
            <Text type="secondary">Clinical Data Management System</Text>
          </div>
          <Button type="primary" size="large" block onClick={login}>
            Sign in with Keycloak
          </Button>
        </Space>
      </Card>
    </div>
  );
}
