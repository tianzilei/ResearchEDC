import { Button, Result } from "antd";
import { useNavigate } from "react-router-dom";

export default function NotFound() {
  const navigate = useNavigate();

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
        status="404"
        title="404"
        subTitle="Page not found."
        extra={
          <Button type="primary" onClick={() => { navigate("/app/dashboard"); }}>
            Back to Dashboard
          </Button>
        }
      />
    </div>
  );
}
