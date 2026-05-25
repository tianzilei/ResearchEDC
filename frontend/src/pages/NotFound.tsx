import { Button, Typography } from "antd";
import { useNavigate } from "react-router-dom";

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
        background: "var(--header-bg)",
      }}
    >
      <div style={{ textAlign: "center" }}>
        <Title
          level={1}
          style={{
            fontWeight: 700,
            fontSize: 64,
            color: "var(--header-text)",
            margin: 0,
            lineHeight: 1,
            letterSpacing: "-0.03em",
          }}
        >
          404
        </Title>
        <Text
          style={{
            color: "var(--text-muted)",
            fontSize: 15,
            display: "block",
            lineHeight: 1.6,
            marginTop: 16,
          }}
        >
          页面未找到
        </Text>
        <Button
          type="primary"
          size="large"
          onClick={() => { navigate("/app/dashboard"); }}
          style={{
            height: 44,
            fontSize: 15,
            fontWeight: 500,
            paddingInline: 32,
            marginTop: 24,
          }}
        >
          返回首页
        </Button>
      </div>
    </div>
  );
}