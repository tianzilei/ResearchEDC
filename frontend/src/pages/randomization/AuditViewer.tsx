import { useParams, useNavigate } from "react-router-dom";
import { Card, Table, Typography, Space, Button, Empty } from "antd";

import { useTranslation } from "react-i18next";
import { useAuditLogs } from "@/hooks/useRandomization";
import { SkeletonPage } from "@/components/SkeletonCard";
import type {} from "@/types/randomization";

const { Title } = Typography;

export default function AuditViewer() {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const schemeId = Number(id);
  const { data: logs, isLoading } = useAuditLogs(schemeId);
  const navigate = useNavigate();

  if (isLoading) return <SkeletonPage />;

  const columns = [
    {
      title: t("audit.column.date"), dataIndex: "performedDate", key: "performedDate", width: 180,
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
    {
      title: t("audit.column.action"), dataIndex: "action", key: "action",
      render: (a: string) => <span className="status status-default">{a}</span>,
    },
    { title: t("audit.column.entity"), dataIndex: "entityType", key: "entityType" },
    { title: t("audit.column.entityId"), dataIndex: "entityId", key: "entityId" },
    {
      title: t("audit.column.details"), dataIndex: "details", key: "details",
      ellipsis: true,
    },
    {
      title: t("audit.column.oldValue"), dataIndex: "oldValue", key: "oldValue",
      ellipsis: true,
      render: (v: string) => v || "-",
    },
    {
      title: t("audit.column.newValue"), dataIndex: "newValue", key: "newValue",
      ellipsis: true,
      render: (v: string) => v || "-",
    },
    { title: t("audit.column.userId"), dataIndex: "performedBy", key: "performedBy" },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button onClick={() => { navigate(`/app/randomization/schemes/${schemeId}`); }}>{t("audit.back")}</Button>
      </Space>

      <Title level={4}>{t("audit.title")}</Title>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={logs ?? []}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 20 }}
          size="small"
          locale={{ emptyText: <Empty description={t("audit.empty")} /> }}
        />
      </Card>
    </div>
  );
}
