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
  Spin,
  message,
  Empty,
} from "antd";
import { PlusOutlined, BranchesOutlined } from "@ant-design/icons";
import type { StudyDetail } from "@/types/study";

const { Title, Text } = Typography;

export default function SiteManagement() {
  const { id } = useParams<{ id: string }>();
  const [study, setStudy] = useState<StudyDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchStudy = () => {
    if (!id) return;
    fetch(`/api/v1/studies/${id}`)
      .then((r) => (r.ok ? r.json() : null))
      .then((data) => { setStudy(data); setLoading(false); })
      .catch(() => setLoading(false));
  };

  useEffect(() => { fetchStudy(); }, [id]);

  const handleCreateSite = async () => {
    if (!id) return;
    try {
      const vals = await form.validateFields();
      const res = await fetch("/api/v1/studies", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: vals.name,
          uniqueIdentifier: vals.uniqueIdentifier,
          principalInvestigator: vals.principalInvestigator,
          facilityName: vals.facilityName,
          facilityCity: vals.facilityCity,
          facilityState: vals.facilityState,
          facilityCountry: vals.facilityCountry,
          typeId: 2,
          parentStudyId: Number(id),
          statusId: 1,
        }),
      });
      if (!res.ok) { message.error("Failed to create site"); return; }
      message.success("Site created");
      setCreateOpen(false);
      form.resetFields();
      fetchStudy();
    } catch { void 0; }
  };

  if (loading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 80 }}><Spin size="large" /></div>;
  }

  const statusColor = (s: string) => {
    switch (s?.toLowerCase()) {
      case "available": return "green";
      case "pending": return "orange";
      case "frozen": return "blue";
      case "locked": return "red";
      default: return "default";
    }
  };

  const columns = [
    { title: "Name", dataIndex: "name", key: "name",
      render: (name: string, record: any) => (
        <Link to={`/app/studies/${record.studyId}`}>{name}</Link>
      ),
    },
    { title: "Identifier", dataIndex: "uniqueIdentifier", key: "uid", render: (v: string) => v || "-" },
    { title: "PI", dataIndex: "principalInvestigator", key: "pi", render: (v: string) => v || "-" },
    { title: "Facility", dataIndex: "facilityName", key: "facility", render: (v: string) => v || "-" },
    { title: "Location", key: "location",
      render: (_: any, record: any) => [record.facilityCity, record.facilityState, record.facilityCountry].filter(Boolean).join(", ") || "-",
    },
    { title: "Status", dataIndex: "status", key: "status",
      render: (s: string) => <Tag color={statusColor(s)}>{s}</Tag>,
    },
  ];

  const sites = study?.sites ?? [];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/studies">Studies</Link> },
          { title: <Link to={`/app/studies/${id}`}>{study?.name ?? `#${id}`}</Link> },
          { title: "Sites" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <BranchesOutlined style={{ fontSize: 22, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>Sites</Title>
              <Text type="secondary">{sites.length} site{sites.length !== 1 ? "s" : ""} for {study?.name}</Text>
            </div>
          </Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
            Add Site
          </Button>
        </div>
      </Card>

      {sites.length === 0 ? (
        <Card style={{ borderRadius: 14 }}>
          <Empty description="No sites have been created for this study" />
        </Card>
      ) : (
        <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
          <Table dataSource={sites} columns={columns} rowKey="studyId" pagination={false} />
        </Card>
      )}

      <Modal
        title="Create Site"
        open={createOpen}
        onOk={handleCreateSite}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        okText="Create Site"
        width={520}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="Site Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Boston Medical Center" />
          </Form.Item>
          <Form.Item name="uniqueIdentifier" label="Site Identifier">
            <Input placeholder="e.g. SITE-001" />
          </Form.Item>
          <Form.Item name="principalInvestigator" label="Principal Investigator">
            <Input placeholder="e.g. Dr. Jones" />
          </Form.Item>
          <Form.Item name="facilityName" label="Facility Name">
            <Input placeholder="e.g. Boston General Hospital" />
          </Form.Item>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="facilityCity" label="City" style={{ flex: 1 }}>
              <Input placeholder="e.g. Boston" />
            </Form.Item>
            <Form.Item name="facilityState" label="State" style={{ flex: 1 }}>
              <Input placeholder="e.g. MA" />
            </Form.Item>
          </Space>
          <Form.Item name="facilityCountry" label="Country">
            <Input placeholder="e.g. USA" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
