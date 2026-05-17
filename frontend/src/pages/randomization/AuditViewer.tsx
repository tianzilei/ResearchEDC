import { useParams, useNavigate } from "react-router-dom";
import { Card, Table, Tag, Typography, Space, Button, Empty } from "antd";
import { ArrowLeftOutlined, AuditOutlined } from "@ant-design/icons";
import { useAuditLogs } from "@/hooks/useRandomization";
import { SkeletonPage } from "@/components/SkeletonCard";
import type {} from "@/types/randomization";

const { Title } = Typography;

const actionColors: Record<string, string> = {
  SCHEME_CREATED: "green",
  SCHEME_ACTIVATED: "blue",
  SCHEME_PAUSED: "orange",
  SCHEME_CLOSED: "red",
  SUBJECT_ASSIGNED: "purple",
  UNBLINDING_REQUESTED: "volcano",
  UNBLINDING_APPROVED: "lime",
  UNBLINDING_REJECTED: "red",
};

export default function AuditViewer() {
  const { id } = useParams<{ id: string }>();
  const schemeId = Number(id);
  const { data: logs, isLoading } = useAuditLogs(schemeId);
  const navigate = useNavigate();

  if (isLoading) return <SkeletonPage />;

  const columns = [
    {
      title: "Date", dataIndex: "performedDate", key: "performedDate", width: 180,
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
    {
      title: "Action", dataIndex: "action", key: "action",
      render: (a: string) => <Tag color={actionColors[a] ?? "default"}>{a}</Tag>,
    },
    { title: "Entity", dataIndex: "entityType", key: "entityType" },
    { title: "Entity ID", dataIndex: "entityId", key: "entityId" },
    {
      title: "Details", dataIndex: "details", key: "details",
      ellipsis: true,
    },
    {
      title: "Old Value", dataIndex: "oldValue", key: "oldValue",
      ellipsis: true,
      render: (v: string) => v || "-",
    },
    {
      title: "New Value", dataIndex: "newValue", key: "newValue",
      ellipsis: true,
      render: (v: string) => v || "-",
    },
    { title: "User ID", dataIndex: "performedBy", key: "performedBy" },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(`/app/randomization/schemes/${schemeId}`)}>Back</Button>
      </Space>

      <Title level={4}><AuditOutlined /> Audit Log</Title>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={logs ?? []}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 20 }}
          size="small"
          locale={{ emptyText: <Empty description="No audit entries" /> }}
        />
      </Card>
    </div>
  );
}
