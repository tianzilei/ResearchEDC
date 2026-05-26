import { useCallback } from "react";
import { Button, Card, Typography, Space, Form, Input, Alert } from "antd";
import { useAuth } from "@/providers/AuthProvider";
import { Navigate } from "react-router-dom";

const { Title, Text } = Typography;

export default function Login() {
  const { isAuthenticated, isInitialized, login, loginError, loginLoading } = useAuth();
  const [form] = Form.useForm();

  const handleSubmit = useCallback(
    async (values: { username: string; password: string }) => {
      await login(values.username, values.password);
    },
    [login],
  );

  if (!isInitialized) {
    return (
      <div
        style={{
          minHeight: "100vh",
          background: "var(--header-bg)",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      />
    );
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
        background: "var(--header-bg)",
      }}
    >
      <Card
        style={{
          width: 400,
          border: "1px solid var(--border)",
          background: "var(--panel)",
        }}
      >
        <Space
          direction="vertical"
          size="middle"
          style={{ width: "100%", textAlign: "center" }}
        >
          <div>
            <Title
              level={3}
              style={{
                margin: 0,
                fontWeight: 600,
                color: "var(--text)",
              }}
            >
              ResearchEDC
            </Title>
            <Text
              style={{
                color: "var(--text-secondary)",
                fontSize: 13,
              }}
            >
              临床研究数据管理系统
            </Text>
          </div>

          <div
            style={{
              width: 32,
              height: 2,
              background: "var(--accent)",
              margin: "2px auto 6px",
            }}
          />

          {loginError && (
            <Alert
              message={loginError}
              type="error"
              closable
              style={{
                textAlign: "left",
                fontSize: 13,
              }}
            />
          )}

          <Form
            form={form}
            layout="vertical"
            onFinish={handleSubmit}
            style={{ width: "100%", textAlign: "left" }}
            requiredMark={false}
          >
            <Form.Item
              name="username"
              label="用户名"
              rules={[{ required: true, message: "请输入用户名" }]}
            >
              <Input
                size="large"
                placeholder="用户名"
                autoComplete="username"
              />
            </Form.Item>

            <Form.Item
              name="password"
              label="密码"
              rules={[{ required: true, message: "请输入密码" }]}
            >
              <Input.Password
                size="large"
                placeholder="密码"
                autoComplete="current-password"
              />
            </Form.Item>

            <Form.Item style={{ marginBottom: 0 }}>
              <Button
                type="primary"
                size="large"
                block
                htmlType="submit"
                loading={loginLoading}
                style={{
                  height: 44,
                  fontSize: 15,
                  fontWeight: 500,
                }}
              >
                登录
              </Button>
            </Form.Item>
          </Form>
        </Space>
      </Card>
    </div>
  );
}