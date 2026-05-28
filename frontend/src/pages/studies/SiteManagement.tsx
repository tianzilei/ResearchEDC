import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  Breadcrumb,
  Card,
  Table,
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

  const statusClass = (s: string) => {
    switch (s?.toLowerCase()) {
      case "available": return "status-success";
      case "pending": return "status-warning";
      case "frozen": return "status-info";
      case "locked": return "status-danger";
      default: return "status-default";
    }
  };

  const columns = [
    { title: "名称", dataIndex: "name", key: "name",
      render: (name: string, record: any) => (
        <Link to={`/app/studies/${record.studyId}`}>{name}</Link>
      ),
    },
    { title: "标识符", dataIndex: "uniqueIdentifier", key: "uid", render: (v: string) => v || "-" },
    { title: "主要研究者", dataIndex: "principalInvestigator", key: "pi", render: (v: string) => v || "-" },
    { title: "机构", dataIndex: "facilityName", key: "facility", render: (v: string) => v || "-" },
    { title: "位置", key: "location",
      render: (_: any, record: any) => [record.facilityCity, record.facilityState, record.facilityCountry].filter(Boolean).join(", ") || "-",
    },
    { title: "状态", dataIndex: "status", key: "status",
      render: (s: string) => <span className={`status ${statusClass(s)}`}>{s}</span>,
    },
  ];

  const sites = study?.sites ?? [];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/studies">研究</Link> },
          { title: <Link to={`/app/studies/${id}`}>{study?.name ?? `#${id}`}</Link> },
          { title: "站点" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>站点</Title>
            <Text type="secondary">{sites.length} 个站点 — {study?.name}</Text>
          </div>
          <Button type="primary" onClick={() => setCreateOpen(true)}>
            添加站点
          </Button>
        </div>
      </Card>

      {sites.length === 0 ? (
        <Card>
          <Empty description="尚未为此研究创建站点" />
        </Card>
      ) : (
        <Card styles={{ body: { padding: 0 } }}>
          <Table dataSource={sites} columns={columns} rowKey="studyId" pagination={false} />
        </Card>
      )}

      <Modal
        title="创建站点"
        open={createOpen}
        onOk={handleCreateSite}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        okText="创建站点"
        width={520}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="站点名称" rules={[{ required: true }]}>
            <Input placeholder="例如：北京医学中心" />
          </Form.Item>
          <Form.Item name="uniqueIdentifier" label="站点标识符">
            <Input placeholder="例如：SITE-001" />
          </Form.Item>
          <Form.Item name="principalInvestigator" label="主要研究者">
            <Input placeholder="例如：张教授" />
          </Form.Item>
          <Form.Item name="facilityName" label="机构名称">
            <Input placeholder="例如：北京总医院" />
          </Form.Item>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="facilityCity" label="城市" style={{ flex: 1 }}>
              <Input placeholder="例如：北京" />
            </Form.Item>
            <Form.Item name="facilityState" label="州/省" style={{ flex: 1 }}>
              <Input placeholder="例如：北京市" />
            </Form.Item>
          </Space>
          <Form.Item name="facilityCountry" label="国家">
            <Input placeholder="例如：中国" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
