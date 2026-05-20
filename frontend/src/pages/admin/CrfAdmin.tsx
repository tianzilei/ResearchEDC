import { useState } from "react";
import { Card, Table, Tag, Button, Typography, Space, Modal, List } from "antd";
import { FileTextOutlined, EyeOutlined, HistoryOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface CrfSummary {
  crfId: number;
  name: string;
  description: string | null;
  ocOid: string | null;
  status: string | null;
  versionCount: number;
  dateCreated: string;
  dateUpdated: string | null;
}

interface CrfVersion {
  crfVersionId: number;
  crfId: number;
  name: string;
  description: string | null;
  revisionNotes: string | null;
  ocOid: string | null;
  status: string | null;
  dateCreated: string;
  sections: any[] | null;
}

export default function CrfAdmin() {
  const navigate = useNavigate();
  const [crfs, setCrfs] = useState<CrfSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCrf, setSelectedCrf] = useState<CrfSummary | null>(null);
  const [versions, setVersions] = useState<CrfVersion[]>([]);
  const [versionsOpen, setVersionsOpen] = useState(false);
  const [versionLoading, setVersionLoading] = useState(false);

  useState(() => {
    fetch("/api/v1/crfs")
      .then(r => r.ok ? r.json() : [])
      .then(data => { setCrfs(data); setLoading(false); })
      .catch(() => setLoading(false));
  });

  const viewVersions = async (crf: CrfSummary) => {
    setSelectedCrf(crf);
    setVersionsOpen(true);
    setVersionLoading(true);
    setVersions([]);
    fetch(`/api/v1/crfs?crfId=${crf.crfId}`)
      .then(r => r.ok ? r.json() : [])
      .then(data => setVersions(data))
      .catch(() => {})
      .finally(() => setVersionLoading(false));
  };

  if (loading) return <SkeletonPage />;

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
    { title: "Versions", dataIndex: "versionCount", key: "versions", width: 80 },
    {
      title: "Created", dataIndex: "dateCreated", key: "created",
      render: (d: string) => d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "", key: "actions",
      render: (_: any, record: CrfSummary) => (
        <Space>
          <Button size="small" icon={<HistoryOutlined />} onClick={() => viewVersions(record)}>
            Versions
          </Button>
          <Button size="small" icon={<EyeOutlined />} onClick={() => navigate(`/app/crfs/${record.crfId}`)}>
            Preview
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: "24px 32px" }}>
      <div style={{ marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>CRF Library</Title>
        <Text type="secondary">{crfs.length} case report forms</Text>
      </div>

      <Card style={{ borderRadius: 14, border: "1px solid var(--color-border-light, #E5E0D8)" }} styles={{ body: { padding: 0 } }}>
        <Table dataSource={crfs} columns={columns} rowKey="crfId" pagination={{ pageSize: 20 }}
          locale={{ emptyText: "No CRFs found" }} />
      </Card>

      <Modal title={`Versions: ${selectedCrf?.name ?? ""}`} open={versionsOpen}
        onCancel={() => setVersionsOpen(false)} footer={null} width={600}>
        {versionLoading ? <Text>Loading...</Text> : (
          <List dataSource={versions} renderItem={(v: CrfVersion) => (
            <List.Item
              actions={[
                <Button size="small" onClick={() => navigate(`/app/crfs/${v.crfVersionId}`)}>
                  Preview
                </Button>,
              ]}
            >
              <List.Item.Meta
                title={<Space>{v.name} <Tag>{v.status ?? "unknown"}</Tag></Space>}
                description={v.description ?? v.revisionNotes ?? "No description"}
              />
            </List.Item>
          )} />
        )}
      </Modal>
    </div>
  );
}
