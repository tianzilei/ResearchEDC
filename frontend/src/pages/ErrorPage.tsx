import { Button, Result } from "antd";
import { useNavigate } from "react-router-dom";

interface ErrorPageProps {
  status?: 403 | 404 | 500;
  title?: string;
  subTitle?: string;
}

const DEFAULT_MESSAGES: Record<number, { title: string; subTitle: string }> = {
  403: {
    title: "403",
    subTitle: "You do not have permission to access this page.",
  },
  404: {
    title: "404",
    subTitle: "The page you are looking for does not exist.",
  },
  500: {
    title: "500",
    subTitle: "Something went wrong. Please try again later.",
  },
};

export default function ErrorPage({
  status = 404,
  title,
  subTitle,
}: ErrorPageProps) {
  const navigate = useNavigate();
  const defaultMsg = DEFAULT_MESSAGES[status] ?? DEFAULT_MESSAGES[500]!;

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <Result
        status={status}
        title={title ?? defaultMsg.title}
        subTitle={subTitle ?? defaultMsg.subTitle}
        extra={
          <Button type="primary" onClick={() => navigate("/app/dashboard")}>
            Back to Dashboard
          </Button>
        }
      />
    </div>
  );
}
