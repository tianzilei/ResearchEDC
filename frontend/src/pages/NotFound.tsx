import { Button, Typography, Space } from "antd";
import { useNavigate } from "react-router-dom";
import { MedicineBoxOutlined } from "@ant-design/icons";

const { Title, Text } = Typography;

export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "#0F1A2E",
        backgroundImage:
          "radial-gradient(circle at 25% 50%, rgba(9,154,135,0.08) 0%, transparent 50%), radial-gradient(circle at 75% 30%, rgba(212,168,84,0.06) 0%, transparent 50%), radial-gradient(circle, rgba(248,245,240,0.04) 0.6px, transparent 0.6px)",
        backgroundSize: "100% 100%, 100% 100%, 28px 28px",
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
      <div
        style={{
          position: "absolute",
          top: 90,
          left: "50%",
          transform: "translateX(-50%)",
          fontFamily: "'Sora', sans-serif",
          fontSize: 200,
          fontWeight: 700,
          color: "rgba(212,168,84,0.06)",
          pointerEvents: "none",
          lineHeight: 1,
          zIndex: 0,
        }}
      >
        404
      </div>
      <Space
        direction="vertical"
        size="middle"
        style={{ position: "relative", zIndex: 1, textAlign: "center" }}
      >
        <div
          style={{
            width: 72,
            height: 72,
            borderRadius: "50%",
            background: "rgba(212,168,84,0.10)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            margin: "0 auto",
          }}
        >
          <MedicineBoxOutlined style={{ color: "#D4A854", fontSize: 34 }} />
        </div>

        <div>
          <Title
            level={1}
            style={{
              fontFamily: "'Sora', sans-serif",
              fontWeight: 700,
              fontSize: 72,
              color: "#F8F5F0",
              margin: 0,
              lineHeight: 1,
              letterSpacing: "-0.03em",
            }}
          >
            404
          </Title>
          <div
            style={{
              width: 40,
              height: 2,
              background: "#D4A854",
              margin: "16px auto",
              borderRadius: 1,
            }}
          />
          <Text
            style={{
              color: "rgba(248,245,240,0.65)",
              fontSize: 15,
              fontFamily: "'DM Sans', sans-serif",
              display: "block",
              lineHeight: 1.6,
            }}
          >
            Page not found.
          </Text>
        </div>

        <Button
          type="primary"
          size="large"
          onClick={() => { navigate("/app/dashboard"); }}
          style={{
            height: 48,
            fontSize: 15,
            borderRadius: 12,
            fontWeight: 500,
            boxShadow: "0 2px 8px rgba(9,154,135,0.30)",
            fontFamily: "'DM Sans', sans-serif",
            paddingInline: 32,
            marginTop: 8,
          }}
        >
          Back to Dashboard
        </Button>
      </Space>
    </div>
  );
}
