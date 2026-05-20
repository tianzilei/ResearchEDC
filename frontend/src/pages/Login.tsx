import { Button, Card, Typography, Space } from "antd";
import { MedicineBoxOutlined } from "@ant-design/icons";
import { useAuth } from "@/providers/AuthProvider";
import { Navigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

const { Title, Text } = Typography;

export default function Login() {
  const { t } = useTranslation();
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
        background: "#0F1A2E",
        backgroundImage:
          "radial-gradient(circle at 25% 50%, rgba(9,154,135,0.08) 0%, transparent 50%), radial-gradient(circle at 75% 30%, rgba(212,168,84,0.06) 0%, transparent 50%)",
        position: "relative",
        overflow: "hidden",
      }}
    >
      {/* Decorative corner dots */}
      <div
        style={{
          position: "absolute",
          top: 40,
          right: 60,
          width: 80,
          height: 80,
          backgroundImage:
            "radial-gradient(circle, rgba(212,168,84,0.2) 1.5px, transparent 1.5px)",
          backgroundSize: "16px 16px",
          opacity: 0.6,
        }}
      />
      <div
        style={{
          position: "absolute",
          bottom: 40,
          left: 60,
          width: 120,
          height: 120,
          backgroundImage:
            "radial-gradient(circle, rgba(9,154,135,0.15) 1.5px, transparent 1.5px)",
          backgroundSize: "20px 20px",
          opacity: 0.5,
        }}
      />

      <Card
        style={{
          width: 420,
          border: "none",
          borderRadius: 16,
          boxShadow: "0 8px 32px rgba(0,0,0,0.25)",
          background: "#F8F5F0",
          animation: "fadeInUp 0.6s cubic-bezier(0.22, 1, 0.36, 1) both",
          position: "relative",
          zIndex: 1,
        }}
      >
        <Space
          direction="vertical"
          size="middle"
          style={{ width: "100%", textAlign: "center" }}
        >
          {/* Decorative brass dot */}
          <div
            style={{
              width: 4,
              height: 4,
              borderRadius: "50%",
              background: "#D4A854",
              margin: "0 auto 4px",
            }}
          />

          {/* Icon with circular background */}
          <div
            style={{
              width: 72,
              height: 72,
              borderRadius: "50%",
              background: "rgba(9,154,135,0.10)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "0 auto",
            }}
          >
            <MedicineBoxOutlined
              style={{ fontSize: 34, color: "#099A87" }}
            />
          </div>

          <div>
            <Title
              level={2}
              style={{
                margin: 0,
                fontFamily: "'Sora', sans-serif",
                fontWeight: 600,
                color: "#1A1D23",
              }}
            >
              ResearchEDC
            </Title>
            <Text
              style={{
                color: "#6B7280",
                fontSize: 14,
                fontFamily: "'DM Sans', sans-serif",
              }}
            >
              Research Electronic Data Capture
            </Text>
          </div>

          {/* Brass divider */}
          <div
            style={{
              width: 40,
              height: 2,
              background: "#D4A854",
              margin: "4px auto 8px",
              borderRadius: 1,
            }}
          />

          <Button
            type="primary"
            size="large"
            block
            onClick={login}
            style={{
              height: 48,
              fontSize: 15,
              borderRadius: 12,
              fontWeight: 500,
              boxShadow: "0 2px 8px rgba(9,154,135,0.30)",
              fontFamily: "'DM Sans', sans-serif",
              marginTop: 4,
            }}
          >
            {t("login.signIn")}
          </Button>
        </Space>
      </Card>
    </div>
  );
}
