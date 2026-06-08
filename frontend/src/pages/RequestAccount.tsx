import { Card, Form, Input, Button, message } from "antd";
import { useNavigate } from "react-router-dom";
import { apiClient } from "@/api/client";

const { TextArea } = Input;

export default function RequestAccount() {
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const handleSubmit = async (values: Record<string, string>) => {
    try {
      await apiClient.post("/api/v1/legacy/request-account", values);
      message.success("Account request submitted. You will receive an email when approved.");
      navigate("/login");
    } catch {
      message.error("Failed to submit request. Please try again.");
    }
  };

  return (
    <div style={{ maxWidth: 480, margin: "60px auto", padding: "0 24px" }}>
      <Card
        title="Request Account"
        style={{ border: "1px solid var(--border-light)" }}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="firstName"
            label="First Name"
            rules={[{ required: true, message: "Required" }]}
          >
            <Input placeholder="First name" />
          </Form.Item>
          <Form.Item
            name="lastName"
            label="Last Name"
            rules={[{ required: true, message: "Required" }]}
          >
            <Input placeholder="Last name" />
          </Form.Item>
          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: "Required" },
              { type: "email", message: "Invalid email" },
            ]}
          >
            <Input placeholder="Email address" />
          </Form.Item>
          <Form.Item
            name="phone"
            label="Phone"
          >
            <Input placeholder="Phone number" />
          </Form.Item>
          <Form.Item
            name="institution"
            label="Institution"
          >
            <Input placeholder="Organization" />
          </Form.Item>
          <Form.Item
            name="notes"
            label="Notes"
          >
            <TextArea rows={3} placeholder="Any additional information" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block>
            Submit Request
          </Button>
        </Form>
      </Card>
      <div style={{ textAlign: "center", marginTop: 16 }}>
        <Button type="link" onClick={() => navigate("/login")}>
          Back to Login
        </Button>
      </div>
    </div>
  );
}
