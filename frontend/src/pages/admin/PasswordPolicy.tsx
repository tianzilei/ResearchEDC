import { useState } from "react";
import {
  Card, Typography, Form, InputNumber, Switch, Button,
  message, Divider, Breadcrumb,
} from "antd";
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
        { title: <Link to="/app/admin">管理</Link> },
        { title: "密码策略" },
      ]} style={{ marginBottom: 16 }} />

      <Card>
        <Title level={4} style={{ marginBottom: 16 }}>密码要求</Title>
        <Text type="secondary" style={{ display: "block", marginBottom: 24 }}>
          配置用户账户的密码复杂度要求。
          生产环境中这些设置通过 Keycloak 管理。
        </Text>

        <Form form={form} layout="vertical" style={{ maxWidth: 500 }}>
          <Form.Item name="minLength" label="最小长度" initialValue={8}>
            <InputNumber min={4} max={64} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="requireUppercase" label="需要大写字母" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
          <Form.Item name="requireLowercase" label="需要小写字母" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
          <Form.Item name="requireDigit" label="需要数字" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
          <Form.Item name="requireSpecial" label="需要特殊字符" valuePropName="checked" initialValue={false}>
            <Switch />
          </Form.Item>
          <Form.Item name="maxAge" label="密码最长天数" initialValue={90}>
            <InputNumber min={0} max={365} style={{ width: "100%" }} />
          </Form.Item>
          <Divider />
          <Button type="primary" onClick={handleSave} loading={saving}>
            保存策略
          </Button>
        </Form>
      </Card>
    </div>
  );
}
