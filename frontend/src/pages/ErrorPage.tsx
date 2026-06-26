import { Button, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

const { Title, Text } = Typography;

interface ErrorPageProps {
  status?: 403 | 404 | 500;
  title?: string;
  subTitle?: string;
}

const SUBTITLE_KEYS: Record<403 | 404 | 500, string> = {
  403: "error.403.subtitle",
  404: "error.404.subtitle",
  500: "error.500.subtitle",
};

export default function ErrorPage({
  status = 404,
  title,
  subTitle,
}: ErrorPageProps) {
  const { t } = useTranslation();
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
          {title ?? status}
        </Title>
        <Text
          style={{
            color: "var(--text-muted)",
            fontSize: 15,
            maxWidth: 400,
            display: "block",
            lineHeight: 1.6,
            marginTop: 16,
          }}
        >
          {subTitle ?? t(SUBTITLE_KEYS[status])}
        </Text>
        <Button
          type="primary"
          size="large"
          onClick={() => navigate("/app/dashboard")}
          style={{
            height: 44,
            fontSize: 15,
            fontWeight: 500,
            paddingInline: 32,
            marginTop: 24,
          }}
        >
          {t("error.backToDashboard")}
        </Button>
      </div>
    </div>
  );
}