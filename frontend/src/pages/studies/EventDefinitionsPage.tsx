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
import { PlusOutlined, FileTextOutlined } from "@ant-design/icons";
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
    { title: "Name", dataIndex: "name", key: "name" },
    { title: "Description", dataIndex: "description", key: "description", render: (v: string) => v || "-" },
    { title: "Category", dataIndex: "category", key: "category", render: (v: string) => v ? <Tag>{v}</Tag> : "-" },
    { title: "Ordinal", dataIndex: "ordinal", key: "ordinal", width: 80 },
    {
      title: "Status", dataIndex: "status", key: "status",
      render: (s: string) => (
        <Tag color={s === "available" ? "green" : "default"}>{s}</Tag>
      ),
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/studies">Studies</Link> },
          { title: <Link to={`/app/studies/${id}`}>{studyName || `#${id}`}</Link> },
          { title: "Event Definitions" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <FileTextOutlined style={{ fontSize: 22, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>Event Definitions</Title>
              <Text type="secondary">{definitions.length} event type{definitions.length !== 1 ? "s" : ""} defined</Text>
            </div>
          </Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
            New Event Definition
          </Button>
        </div>
      </Card>

      {definitions.length === 0 ? (
        <Card style={{ borderRadius: 14 }}>
          <Empty description="No event definitions" />
        </Card>
      ) : (
        <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
          <Table dataSource={definitions} columns={columns} rowKey="studyEventDefinitionId" pagination={false} />
        </Card>
      )}

      <Modal
        title="Create Event Definition"
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        okText="Create"
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Screening Visit" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="category" label="Category" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Screening">Screening</Select.Option>
                <Select.Option value="Baseline">Baseline</Select.Option>
                <Select.Option value="Treatment">Treatment</Select.Option>
                <Select.Option value="Follow-up">Follow-up</Select.Option>
                <Select.Option value="Final">Final</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="ordinal" label="Ordinal" style={{ flex: 1 }}>
              <Input type="number" placeholder="1" />
            </Form.Item>
          </Space>
        </Form>
      </Modal>
    </div>
  );
}
