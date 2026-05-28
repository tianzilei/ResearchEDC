import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  Breadcrumb,
  Card,
  Table,
  Tag,
  Button,
  Space,
  Typography,
  Modal,
  Form,
  Input,
  Select,
  Spin,
  message,
  Empty,
} from "antd";

import type { EventDefinitionDTO } from "@/types/event";

const { Title, Text } = Typography;

export default function EventDefinitionsPage() {
  const { id } = useParams<{ id: string }>();
  const [definitions, setDefinitions] = useState<EventDefinitionDTO[]>([]);
  const [studyName, setStudyName] = useState("");
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = () => {
    if (!id) return;
    setLoading(true);
    Promise.all([
      fetch(`/api/v1/studies/${id}`).then(r => r.ok ? r.json() : null),
      fetch(`/api/v1/events/definitions?studyId=${id}`).then(r => r.ok ? r.json() : []),
    ]).then(([study, defs]) => {
      if (study) setStudyName(study.name);
      setDefinitions(defs);
      setLoading(false);
    }).catch(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, [id]);

  const handleCreate = async () => {
    if (!id) return;
    try {
      const vals = await form.validateFields();
      const res = await fetch("/api/v1/events/definitions", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...vals, studyId: Number(id) }),
      });
      if (!res.ok) { message.error("Failed to create event definition"); return; }
      message.success("Event definition created");
      setCreateOpen(false);
      form.resetFields();
      fetchData();
    } catch { void 0; }
  };

  if (loading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 80 }}><Spin size="large" /></div>;
  }

  const columns = [
    { title: "名称", dataIndex: "name", key: "name" },
    { title: "描述", dataIndex: "description", key: "description", render: (v: string) => v || "-" },
    { title: "类别", dataIndex: "category", key: "category", render: (v: string) => v ? <Tag>{v}</Tag> : "-" },
    { title: "顺序", dataIndex: "ordinal", key: "ordinal", width: 80 },
    {
      title: "状态", dataIndex: "status", key: "status",
      render: (s: string) => (
        <span className={`status ${s === "available" ? "status-success" : "status-default"}`}>{s}</span>
      ),
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/studies">研究</Link> },
          { title: <Link to={`/app/studies/${id}`}>{studyName || `#${id}`}</Link> },
          { title: "事件定义" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>事件定义</Title>
            <Text type="secondary">已定义 {definitions.length} 个事件类型</Text>
          </div>
          <Button type="primary" onClick={() => setCreateOpen(true)}>
            新建事件定义
          </Button>
        </div>
      </Card>

      {definitions.length === 0 ? (
        <Card>
          <Empty description="暂无事件定义" />
        </Card>
      ) : (
        <Card styles={{ body: { padding: 0 } }}>
          <Table dataSource={definitions} columns={columns} rowKey="studyEventDefinitionId" pagination={false} />
        </Card>
      )}

      <Modal
        title="创建事件定义"
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        okText="创建"
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input placeholder="例如：筛选访视" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="category" label="类别" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Screening">筛选</Select.Option>
                <Select.Option value="Baseline">基线</Select.Option>
                <Select.Option value="Treatment">治疗</Select.Option>
                <Select.Option value="Follow-up">随访</Select.Option>
                <Select.Option value="Final">结束</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="ordinal" label="顺序" style={{ flex: 1 }}>
              <Input type="number" placeholder="1" />
            </Form.Item>
          </Space>
        </Form>
      </Modal>
    </div>
  );
}
