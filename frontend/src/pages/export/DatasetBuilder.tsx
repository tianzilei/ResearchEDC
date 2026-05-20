import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import {
  Card,
  Table,
  Tag,
  Button,
  Typography,
  Space,
  Modal,
  Form,
  Input,
  Select,
  Spin,
  message,
  Empty,
  Breadcrumb,
} from "antd";
import { PlusOutlined, DatabaseOutlined, FileTextOutlined } from "@ant-design/icons";

const { Title, Text } = Typography;

interface Dataset {
  datasetId: number;
  name: string;
  description: string;
  studyId: number;
  studyName: string;
  ownerId: number;
  status: string;
  dateCreated: string;
}

export default function DatasetBuilder() {
  const navigate = useNavigate();
  const [datasets, setDatasets] = useState<Dataset[]>([]);
  const [studies, setStudies] = useState<{ studyId: number; name: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = () => {
    setLoading(true);
    Promise.all([
      fetch("/api/legacy/datasets").then(r => r.ok ? r.json() : []),
      fetch("/api/v1/studies").then(r => r.ok ? r.json() : []),
    ]).then(([ds, ss]) => {
      setDatasets(ds);
      setStudies(Array.isArray(ss) ? ss.map((s: any) => ({ studyId: s.studyId ?? s.id, name: s.name })) : []);
      setLoading(false);
    }).catch(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  const handleCreate = async () => {
    try {
      const vals = await form.validateFields();
      const res = await fetch(`/api/legacy/datasets?name=${encodeURIComponent(vals.name)}&studyId=${vals.studyId}`, {
        method: "POST",
      });
      if (!res.ok) { message.error("Failed to create dataset"); return; }
      message.success("Dataset created");
      setCreateOpen(false);
      form.resetFields();
      fetchData();
    } catch { void 0; }
  };

  if (loading) {
    return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  }

  const columns = [
    { title: "Name", dataIndex: "name", key: "name" },
    { title: "Description", dataIndex: "description", key: "description", render: (v: string) => v || "-" },
    { title: "Study", dataIndex: "studyName", key: "study", render: (v: string) => v || "-" },
    { title: "Status", dataIndex: "status", key: "status", render: (s: string) => <Tag>{s || "available"}</Tag> },
    { title: "Created", dataIndex: "dateCreated", key: "created", render: (v: string) => v ? new Date(v).toLocaleDateString() : "-" },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/data-export">Export Center</Link> },
          { title: "Datasets" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <DatabaseOutlined style={{ fontSize: 22, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>Datasets</Title>
              <Text type="secondary">{datasets.length} dataset{datasets.length !== 1 ? "s" : ""}</Text>
            </div>
          </Space>
          <Space>
            <Button onClick={() => navigate("/app/data-export")} icon={<FileTextOutlined />}>
              Export Center
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
              New Dataset
            </Button>
          </Space>
        </div>
      </Card>

      {datasets.length === 0 ? (
        <Card style={{ borderRadius: 14 }}><Empty description="No datasets defined" /></Card>
      ) : (
        <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
          <Table dataSource={datasets} columns={columns} rowKey="datasetId" pagination={false} />
        </Card>
      )}

      <Modal
        title="Create Dataset"
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="Dataset Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Primary Analysis Dataset" />
          </Form.Item>
          <Form.Item name="studyId" label="Study" rules={[{ required: true }]}>
            <Select placeholder="Select study" showSearch>
              {studies.map((s) => (
                <Select.Option key={s.studyId} value={s.studyId}>{s.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
