import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  Card, Table, Button, Typography, Space, Modal, Form, Input,
  Spin, message, Empty, Breadcrumb,
} from "antd";
import { apiClient } from "@/api/client";

const { Title, Text } = Typography;

interface FilterItem {
  id: number;
  name: string;
  description: string;
  studyId: number;
  ownerId: number;
  dateCreated: string;
}

export default function FilterBuilder() {
  const { t } = useTranslation();
  const [filters, setFilters] = useState<FilterItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    setLoading(true);
    apiClient.get<FilterItem[]>("/api/legacy/filters")
      .then(data => { setFilters(data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  const handleCreate = async () => {
    try {
      const vals = await form.validateFields();
      await apiClient.post<FilterItem>("/api/legacy/filters", vals);
      message.success(t("filter.created"));
      setCreateOpen(false);
      form.resetFields();
      const data = await apiClient.get<FilterItem[]>("/api/legacy/filters");
      setFilters(data);
    } catch {
      message.error(t("filter.createFailed"));
    }
  };

  if (loading) {
    return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  }

  const columns = [
    { title: t("filter.column.name"), dataIndex: "name", key: "name" },
    { title: t("filter.column.description"), dataIndex: "description", key: "description", render: (v: string) => v || "-" },
    {
      title: t("filter.column.created"), dataIndex: "dateCreated", key: "created",
      render: (v: string) => v ? new Date(v).toLocaleDateString() : "-",
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/data-export">{t("export.title")}</Link> },
          { title: t("filter.title") },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16, borderRadius: 6 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>{t("filter.title")}</Title>
              <Text type="secondary">{t("filter.count", { count: filters.length })}</Text>
            </div>
          </Space>
          <Button type="primary" onClick={() => setCreateOpen(true)}>
            {t("filter.new")}
          </Button>
        </div>
      </Card>

      {filters.length === 0 ? (
        <Card style={{ borderRadius: 6 }}><Empty description={t("filter.empty")} /></Card>
      ) : (
        <Card style={{ borderRadius: 6 }} styles={{ body: { padding: 0 } }}>
          <Table dataSource={filters} columns={columns} rowKey="id" pagination={false} />
        </Card>
      )}

      <Modal title={t("filter.create")} open={createOpen}
        onOk={handleCreate} onCancel={() => { setCreateOpen(false); form.resetFields(); }}>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label={t("filter.form.name")} rules={[{ required: true }]}>
            <Input placeholder={t("filter.form.namePlaceholder")} />
          </Form.Item>
          <Form.Item name="description" label={t("filter.form.description")}>
            <Input.TextArea rows={3} placeholder={t("filter.form.descriptionPlaceholder")} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
