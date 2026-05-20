import { useEffect, useState } from "react";
import {
  Card, Table, Tag, Button, Typography, Space, Modal, List,
  Form, Input, message, Spin, Empty, Popconfirm,
} from "antd";
import {
  FileTextOutlined, EyeOutlined, HistoryOutlined,
  PlusOutlined, DeleteOutlined,
} from "@ant-design/icons";
import { useNavigate } from "react-router-dom";

const { Title, Text } = Typography;

interface CrfItem {
  crfId: number;
  name: string;
  description: string;
  ocOid: string;
  status: string;
  dateCreated: string;
}

interface CrfVersionItem {
  crfVersionId: number;
  crfId: number;
  name: string;
  description: string;
  revisionNotes: string;
  status: string;
  dateCreated: string;
}

export default function CrfAdmin() {
  const navigate = useNavigate();
  const [crfs, setCrfs] = useState<CrfItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCrf, setSelectedCrf] = useState<CrfItem | null>(null);
  const [versions, setVersions] = useState<CrfVersionItem[]>([]);
  const [versionsOpen, setVersionsOpen] = useState(false);
  const [versionLoading, setVersionLoading] = useState(false);

  const [createCrfOpen, setCreateCrfOpen] = useState(false);
  const [createVersionOpen, setCreateVersionOpen] = useState(false);
  const [crfForm] = Form.useForm();
  const [versionForm] = Form.useForm();

  const fetchCrfs = () => {
    fetch("/api/legacy/crfs")
      .then(r => r.ok ? r.json() : [])
      .then(data => { setCrfs(data); setLoading(false); })
      .catch(() => setLoading(false));
  };

  useEffect(() => { fetchCrfs(); }, []);

  const viewVersions = async (crf: CrfItem) => {
    setSelectedCrf(crf);
    setVersionsOpen(true);
    setVersionLoading(true);
    try {
      const res = await fetch(`/api/legacy/crfs/${crf.crfId}/versions`);
      if (res.ok) setVersions(await res.json());
    } catch { /* ignore */ }
    setVersionLoading(false);
  };

  const handleCreateCrf = async () => {
    try {
      const vals = await crfForm.validateFields();
      const res = await fetch("/api/legacy/crfs", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(vals),
      });
      if (!res.ok) { message.error("Failed to create CRF"); return; }
      message.success("CRF created");
      setCreateCrfOpen(false);
      crfForm.resetFields();
      fetchCrfs();
    } catch { /* validation failed */ }
  };

  const handleCreateVersion = async () => {
    if (!selectedCrf) return;
    try {
      const vals = await versionForm.validateFields();
      const res = await fetch(`/api/legacy/crfs/${selectedCrf.crfId}/versions`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(vals),
      });
      if (!res.ok) { message.error("Failed to create version"); return; }
      message.success("Version created");
      setCreateVersionOpen(false);
      versionForm.resetFields();
      viewVersions(selectedCrf);
    } catch { /* validation failed */ }
  };

  const handleDeleteVersion = async (versionId: number) => {
    const res = await fetch(`/api/legacy/crfs/versions/${versionId}`, {
      method: "DELETE",
    });
    if (!res.ok) { message.error("Failed to delete version"); return; }
    message.success("Version deleted");
    if (selectedCrf) viewVersions(selectedCrf);
  };

  if (loading) {
    return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  }

  const columns = [
    {
      title: "Name", dataIndex: "name", key: "name",
      render: (text: string) => <><FileTextOutlined style={{ marginRight: 8 }} />{text}</>,
    },
    { title: "OC OID", dataIndex: "ocOid", key: "ocOid", render: (v: string) => v || "-" },
    {
      title: "Status", dataIndex: "status", key: "status",
      render: (v: string) => <Tag color={v === "available" ? "green" : "default"}>{v ?? "unknown"}</Tag>,
    },
    {
      title: "Created", dataIndex: "dateCreated", key: "created",
      render: (d: string) => d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "", key: "actions",
      render: (_: any, record: CrfItem) => (
        <Space>
          <Button size="small" icon={<HistoryOutlined />} onClick={() => viewVersions(record)}>
            Versions
          </Button>
          <Button size="small" icon={<EyeOutlined />}
            onClick={() => {
              const firstVersion = versions.find(v => v.crfId === record.crfId);
              if (firstVersion) navigate(`/app/crfs/${firstVersion.crfVersionId}`);
              else navigate(`/app/crfs/${record.crfId}`);
            }}>
            Preview
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <FileTextOutlined style={{ fontSize: 22, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>CRF Library</Title>
              <Text type="secondary">{crfs.length} case report forms</Text>
            </div>
          </Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateCrfOpen(true)}>
            New CRF
          </Button>
        </div>
      </Card>

      <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
        <Table dataSource={crfs} columns={columns} rowKey="crfId" pagination={{ pageSize: 20 }}
          locale={{ emptyText: <Empty description="No CRFs found" /> }} />
      </Card>

      <Modal title="Create CRF" open={createCrfOpen}
        onOk={handleCreateCrf} onCancel={() => { setCreateCrfOpen(false); crfForm.resetFields(); }}>
        <Form form={crfForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="CRF Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Adverse Event Form" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={`Versions: ${selectedCrf?.name ?? ""}`} open={versionsOpen}
        onCancel={() => setVersionsOpen(false)} footer={null} width={640}>
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" size="small" icon={<PlusOutlined />}
            onClick={() => setCreateVersionOpen(true)}>
            New Version
          </Button>
        </Space>
        {versionLoading ? <div style={{ textAlign: "center", padding: 24 }}><Spin /></div> : (
          versions.length === 0 ? <Empty description="No versions" /> : (
            <List dataSource={versions} renderItem={(v: CrfVersionItem) => (
              <List.Item
                actions={[
                  <Button size="small" icon={<EyeOutlined />}
                    onClick={() => navigate(`/app/crfs/${v.crfVersionId}`)}>
                    Preview
                  </Button>,
                  <Popconfirm title="Delete this version?" onConfirm={() => handleDeleteVersion(v.crfVersionId)}>
                    <Button size="small" danger icon={<DeleteOutlined />} />
                  </Popconfirm>,
                ]}
              >
                <List.Item.Meta
                  title={<Space>{v.name} <Tag>{v.status ?? "unknown"}</Tag></Space>}
                  description={v.description || v.revisionNotes || "No description"}
                />
              </List.Item>
            )} />
          )
        )}
      </Modal>

      <Modal title="Create Version" open={createVersionOpen}
        onOk={handleCreateVersion}
        onCancel={() => { setCreateVersionOpen(false); versionForm.resetFields(); }}>
        <Form form={versionForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="Version Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. v1.0" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="revisionNotes" label="Revision Notes">
            <Input.TextArea rows={2} placeholder="Changes in this version" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
