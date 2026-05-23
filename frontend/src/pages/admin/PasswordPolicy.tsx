import { useState } from "react";
import {
  Card, Typography, Form, InputNumber, Switch, Button,
  Space, message, Divider, Breadcrumb,
} from "antd";
import { SaveOutlined, LockOutlined } from "@ant-design/icons";
import { Link } from "react-router-dom";

const { Title, Text } = Typography;

export default function PasswordPolicy() {
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);

  const handleSave = () => {
    setSaving(true);
    message.success("Password policy updated (local state only — no backend API)");
    setSaving(false);
  };

  return (
    <div>
      <Breadcrumb items={[
        { title: <Link to="/app/admin">Admin</Link> },
        { title: "Password Policy" },
      ]} style={{ marginBottom: 16 }} />

      <Card style={{ borderRadius: 14 }}>
        <Space style={{ marginBottom: 16 }}>
          <LockOutlined style={{ fontSize: 24, color: "var(--color-primary, #099A87)" }} />
          <Title level={4} style={{ margin: 0 }}>Password Requirements</Title>
        </Space>
        <Text type="secondary" style={{ display: "block", marginBottom: 24 }}>
          Configure password complexity requirements for user accounts.
          These settings are managed via Keycloak in production.
        </Text>

        <Form form={form} layout="vertical" style={{ maxWidth: 500 }}>
          <Form.Item name="minLength" label="Minimum Length" initialValue={8}>
            <InputNumber min={4} max={64} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="requireUppercase" label="Require Uppercase Letter" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
          <Form.Item name="requireLowercase" label="Require Lowercase Letter" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
          <Form.Item name="requireDigit" label="Require Digit" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
          <Form.Item name="requireSpecial" label="Require Special Character" valuePropName="checked" initialValue={false}>
            <Switch />
          </Form.Item>
          <Form.Item name="maxAge" label="Password Max Age (days)" initialValue={90}>
            <InputNumber min={0} max={365} style={{ width: "100%" }} />
          </Form.Item>
          <Divider />
          <Button type="primary" icon={<SaveOutlined />} onClick={handleSave} loading={saving}>
            Save Policy
          </Button>
        </Form>
      </Card>
    </div>
  );
}
