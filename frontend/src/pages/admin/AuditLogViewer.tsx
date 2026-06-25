import { useState } from "react";
import { Card, Table, Tag, Space, Typography, Select } from "antd";
import { SkeletonPage } from "@/components/SkeletonCard";
import { useAppQuery } from "@/hooks/useQuery";
import { auditApi, type AuditLogEntry } from "@/api/audit";

const { Title, Text } = Typography;

export default function AuditLogViewer() {
  const [filterModule, setFilterModule] = useState<string>("");
  const { data: logs = [], isLoading } = useAppQuery<AuditLogEntry[]>({
    queryKey: ["audit", "logs"],
    queryFn: async () => {
      const data = await auditApi.listLogs();
      const content = Array.isArray(data) ? data : data.content;
      return Array.isArray(content) ? content : [];
    },
  });

  if (isLoading) return <SkeletonPage />;

  const modules = [...new Set(logs.map(l => l.sourceModule).filter(Boolean))];
  const filtered = filterModule ? logs.filter(l => l.sourceModule === filterModule) : logs;

  const columns = [
    { title: "ID", dataIndex: "id", key: "id", width: 60 },
    {
      title: "类型", dataIndex: "eventType", key: "type", width: 90,
      render: (v: string) => <span className="status status-default">{v}</span>,
    },
    { title: "实体", key: "entity", render: (_: unknown, r: AuditLogEntry) =>
      `${r.entityType ?? "-"}#${r.entityId ?? ""}` },
    { title: "标签", dataIndex: "entityLabel", key: "label", ellipsis: true },
    { title: "模块", dataIndex: "sourceModule", key: "module", width: 100,
      render: (v: string | null) => <Tag>{v ?? "-"}</Tag> },
    { title: "用户", dataIndex: "performedBy", key: "user", width: 70 },
    {
      title: "日期", dataIndex: "performedDate", key: "date", width: 170,
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
    { title: "详情", dataIndex: "details", key: "details", ellipsis: true },
  ];

  return (
    <div style={{ padding: "24px 32px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <div>
          <Title level={3} style={{ margin: 0 }}>审计日志</Title>
          <Text type="secondary">{filtered.length} 条记录</Text>
        </div>
        <Space>
          <Select
            allowClear
            placeholder="按模块筛选"
            value={filterModule || undefined}
            onChange={(v) => setFilterModule(v ?? "")}
            style={{ width: 180 }}
          >
            {modules.map(m => <Select.Option key={m} value={m}>{m}</Select.Option>)}
          </Select>
        </Space>
      </div>

      <Card style={{ borderRadius: "var(--radius-lg)", border: "1px solid var(--border)" }} styles={{ body: { padding: 0 } }}>
        <Table
          dataSource={filtered} columns={columns} rowKey="id"
          pagination={{ pageSize: 25, showTotal: (t) => `${t} 条记录` }}
          locale={{ emptyText: "暂无审计记录" }}
          size="small"
        />
      </Card>
    </div>
  );
}
