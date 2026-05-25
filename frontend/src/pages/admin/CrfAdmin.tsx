import { useEffect, useState } from "react";
import {
  Card, Table, Button, Typography, Space, Modal, List,
  Form, Input, message, Spin, Empty, Popconfirm,
} from "antd";
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
      if (!res.ok) { message.error("创建 CRF 失败"); return; }
      message.success("CRF 已创建");
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
      if (!res.ok) { message.error("创建版本失败"); return; }
      message.success("版本已创建");
      setCreateVersionOpen(false);
      versionForm.resetFields();
      viewVersions(selectedCrf);
    } catch { /* validation failed */ }
  };

  const handleDeleteVersion = async (versionId: number) => {
    const res = await fetch(`/api/legacy/crfs/versions/${versionId}`, {
      method: "DELETE",
    });
    if (!res.ok) { message.error("删除版本失败"); return; }
    message.success("版本已删除");
    if (selectedCrf) viewVersions(selectedCrf);
  };

  if (loading) {
    return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  }

  const columns = [
    {
      title: "名称", dataIndex: "name", key: "name",
      render: (text: string) => <>{text}</>,
    },
    { title: "OID", dataIndex: "ocOid", key: "ocOid", render: (v: string) => v || "-" },
    {
      title: "状态", dataIndex: "status", key: "status",
      render: (v: string) => <span className={v === "available" ? "status status-success" : "status status-default"}>{v ?? "unknown"}</span>,
    },
    {
      title: "创建时间", dataIndex: "dateCreated", key: "created",
      render: (d: string) => d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "", key: "actions",
      render: (_: any, record: CrfItem) => (
        <Space>
          <Button size="small" onClick={() => viewVersions(record)}>
            版本
          </Button>
          <Button size="small"
            onClick={() => {
              const firstVersion = versions.find(v => v.crfId === record.crfId);
              if (firstVersion) navigate(`/app/crfs/${firstVersion.crfVersionId}`);
              else navigate(`/app/crfs/${record.crfId}`);
            }}>
            预览
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>CRF 库</Title>
              <Text type="secondary">{crfs.length} 个病例报告表</Text>
            </div>
          </Space>
          <Button type="primary" onClick={() => setCreateCrfOpen(true)}>
            新建 CRF
          </Button>
        </div>
      </Card>

      <Card>
        <Table dataSource={crfs} columns={columns} rowKey="crfId" pagination={{ pageSize: 20 }}
          locale={{ emptyText: <Empty description="暂无 CRF" /> }} />
      </Card>

      <Modal title="创建 CRF" open={createCrfOpen}
        onOk={handleCreateCrf} onCancel={() => { setCreateCrfOpen(false); crfForm.resetFields(); }}>
        <Form form={crfForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="CRF 名称" rules={[{ required: true }]}>
            <Input placeholder="例如：不良事件表" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={`版本：${selectedCrf?.name ?? ""}`} open={versionsOpen}
        onCancel={() => setVersionsOpen(false)} footer={null} width={640}>
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" size="small"
            onClick={() => setCreateVersionOpen(true)}>
            新建版本
          </Button>
        </Space>
        {versionLoading ? <div style={{ textAlign: "center", padding: 24 }}><Spin /></div> : (
            versions.length === 0 ? <Empty description="暂无版本" /> : (
            <List dataSource={versions} renderItem={(v: CrfVersionItem) => (
              <List.Item
                actions={[
                  <Button size="small"
                    onClick={() => navigate(`/app/crfs/${v.crfVersionId}`)}>
                    预览
                  </Button>,
                  <Popconfirm title="确定删除此版本？" onConfirm={() => handleDeleteVersion(v.crfVersionId)}>
                    <Button size="small" danger />
                  </Popconfirm>,
                ]}
              >
                <List.Item.Meta
                  title={<Space>{v.name} <span className="status status-default">{v.status ?? "unknown"}</span></Space>}
                  description={v.description || v.revisionNotes || "无描述"}
                />
              </List.Item>
            )} />
          )
        )}
      </Modal>

      <Modal title="创建版本" open={createVersionOpen}
        onOk={handleCreateVersion}
        onCancel={() => { setCreateVersionOpen(false); versionForm.resetFields(); }}>
        <Form form={versionForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="版本名称" rules={[{ required: true }]}>
            <Input placeholder="例如：v1.0" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="revisionNotes" label="修订说明">
            <Input.TextArea rows={2} placeholder="此版本的变更说明" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
