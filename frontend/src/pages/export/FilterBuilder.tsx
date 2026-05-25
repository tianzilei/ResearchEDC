import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Card, Table, Button, Typography, Space, Modal, Form, Input,
  Spin, message, Empty, Breadcrumb,
} from "antd";

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
  const [filters, setFilters] = useState<FilterItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    setLoading(true);
    fetch("/api/legacy/filters")
      .then(r => r.ok ? r.json() : [])
      .then(data => { setFilters(data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  const handleCreate = async () => {
    try {
      const vals = await form.validateFields();
      const res = await fetch("/api/legacy/filters", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(vals),
      });
      if (!res.ok) { message.error("Failed to create filter"); return; }
      message.success("Filter created");
      setCreateOpen(false);
      form.resetFields();
      const data = await fetch("/api/legacy/filters").then(r => r.ok ? r.json() : []);
      setFilters(data);
    } catch { /* validation */ }
  };

  if (loading) {
    return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  }

  const columns = [
    { title: "Name", dataIndex: "name", key: "name" },
    { title: "Description", dataIndex: "description", key: "description", render: (v: string) => v || "-" },
    {
      title: "Created", dataIndex: "dateCreated", key: "created",
      render: (v: string) => v ? new Date(v).toLocaleDateString() : "-",
    },
    {
      title: "", key: "actions",
      render: (_: any, record: FilterItem) => (
        <Button size="small"
          onClick={() => window.open(`/legacy/ViewFilterDetails?id=${record.id}`, "_blank")}>
          View Details
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/data-export">Export Center</Link> },
          { title: "Filters" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>Filters</Title>
              <Text type="secondary">{filters.length} filter{filters.length !== 1 ? "s" : ""}</Text>
            </div>
          </Space>
          <Button type="primary" onClick={() => setCreateOpen(true)}>
            New Filter
          </Button>
        </div>
      </Card>

      {filters.length === 0 ? (
        <Card style={{ borderRadius: 14 }}><Empty description="No filters defined" /></Card>
      ) : (
        <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
          <Table dataSource={filters} columns={columns} rowKey="id" pagination={false} />
        </Card>
      )}

      <Modal title="Create Filter" open={createOpen}
        onOk={handleCreate} onCancel={() => { setCreateOpen(false); form.resetFields(); }}>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="Filter Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Completed CRFs" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} placeholder="Optional description" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
