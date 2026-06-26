import { Button, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

const { Title, Text } = Typography;

export default function NotFound() {
  const navigate = useNavigate();
  const { t } = useTranslation();

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
            letterSpacing: 0,
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
          {t("notFound.message")}
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
          {t("common.backHome")}
        </Button>
      </div>
    </div>
  );
}
