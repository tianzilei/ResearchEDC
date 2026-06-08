import { Card, Form, Input, Button, message } from "antd";
import { useNavigate } from "react-router-dom";
import { apiClient } from "@/api/client";

const { TextArea } = Input;

export default function ContactForm() {
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const handleSubmit = async (values: Record<string, string>) => {
    try {
      await apiClient.post("/api/v1/legacy/contact", values);
      message.success("Message sent. An administrator will respond shortly.");
      form.resetFields();
    } catch {
      message.error("Failed to send message. Please try again.");
    }
  };

  return (
    <div style={{ maxWidth: 480, margin: "60px auto", padding: "0 24px" }}>
      <Card
        title="Contact Us"
        style={{ border: "1px solid var(--border-light)" }}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="name"
            label="Your Name"
            rules={[{ required: true, message: "Required" }]}
          >
            <Input placeholder="Your full name" />
          </Form.Item>
          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: "Required" },
              { type: "email", message: "Invalid email" },
            ]}
          >
            <Input placeholder="Your email address" />
          </Form.Item>
          <Form.Item
            name="subject"
            label="Subject"
            rules={[{ required: true, message: "Required" }]}
          >
            <Input placeholder="What is this about?" />
          </Form.Item>
          <Form.Item
            name="message"
            label="Message"
            rules={[{ required: true, message: "Required" }]}
          >
            <TextArea rows={6} placeholder="Describe your issue or question" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block>
            Send Message
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
