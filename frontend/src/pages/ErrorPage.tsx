import { Button, Typography, Space } from "antd";
import { useNavigate } from "react-router-dom";
import { MedicineBoxOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";

const { Title, Text } = Typography;

interface ErrorPageProps {
  status?: 403 | 404 | 500;
  title?: string;
  subTitle?: string;
}

const ERROR_MESSAGES: Record<number, { key: string }> = {
  403: { key: "403" },
  404: { key: "404" },
  500: { key: "500" },
};

export default function ErrorPage({
  status = 404,
  title,
  subTitle,
}: ErrorPageProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const SUBTITLE_KEYS: Record<403 | 404 | 500, string> = {
    403: "error.403.subtitle",
    404: "error.404.subtitle",
    500: "error.500.subtitle",
  };

  const defaultMsg = ERROR_MESSAGES[status]
    ? { title: ERROR_MESSAGES[status].key, subTitle: t(SUBTITLE_KEYS[status]) }
    : { title: t("error.default.title"), subTitle: t("error.default.subtitle") };

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
          textAlign: "center",
          position: "relative",
          zIndex: 1,
          animation: "fadeInUp 0.6s cubic-bezier(0.22, 1, 0.36, 1) both",
        }}
      >
        {/* Large background status number */}
        <div
          style={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            fontFamily: "'Sora', sans-serif",
            fontSize: 200,
            fontWeight: 700,
            color: "rgba(212,168,84,0.06)",
            pointerEvents: "none",
            lineHeight: 1,
            zIndex: 0,
          }}
        >
          {title ?? defaultMsg.title}
        </div>

        <Space
          direction="vertical"
          size="middle"
          style={{ position: "relative", zIndex: 1 }}
        >
          {/* Icon */}
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
              fontSize: 32,
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
              {title ?? defaultMsg.title}
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
                maxWidth: 400,
                display: "block",
                lineHeight: 1.6,
              }}
            >
              {subTitle ?? defaultMsg.subTitle}
            </Text>
          </div>

          <Button
            type="primary"
            size="large"
            onClick={() => navigate("/app/dashboard")}
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
            {t("error.backToDashboard")}
          </Button>
        </Space>
      </div>
    </div>
  );
}
