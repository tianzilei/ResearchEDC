import { useState } from "react";
import { Card, Table, Tag, Space, Typography, Select } from "antd";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface AuditLogEntry {
  id: number;
  studyId: number | null;
  eventType: string;
  entityType: string | null;
  entityId: number | null;
  entityLabel: string | null;
  oldValue: string | null;
  newValue: string | null;
  performedBy: number | null;
  performedDate: string;
  details: string | null;
  sourceModule: string | null;
}

const EVENT_COLORS: Record<string, string> = {
  CREATE: "green", UPDATE: "blue", DELETE: "red",
  LOCK: "orange", UNLOCK: "purple", EXPORT: "cyan",
  SIGN: "gold", VIEW: "default", SYSTEM: "geekblue",
};

export default function AuditLogViewer() {
  const [logs, setLogs] = useState<AuditLogEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterModule, setFilterModule] = useState<string>("");

  useState(() => {
    fetch("/api/v1/audit")
      .then(r => r.ok ? r.json() : [])
      .then((data: any) => {
        const content = data.content ?? data;
        setLogs(Array.isArray(content) ? content : []);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  });

  if (loading) return <SkeletonPage />;

  const modules = [...new Set(logs.map(l => l.sourceModule).filter(Boolean))];
  const filtered = filterModule ? logs.filter(l => l.sourceModule === filterModule) : logs;

  const columns = [
    { title: "ID", dataIndex: "id", key: "id", width: 60 },
    {
      title: "Type", dataIndex: "eventType", key: "type", width: 90,
      render: (v: string) => <Tag color={EVENT_COLORS[v] ?? "default"}>{v}</Tag>,
    },
    { title: "Entity", key: "entity", render: (_: any, r: AuditLogEntry) =>
      `${r.entityType ?? "-"}#${r.entityId ?? ""}` },
    { title: "Label", dataIndex: "entityLabel", key: "label", ellipsis: true },
    { title: "Module", dataIndex: "sourceModule", key: "module", width: 100,
      render: (v: string) => <Tag>{v ?? "-"}</Tag> },
    { title: "User", dataIndex: "performedBy", key: "user", width: 70 },
    {
      title: "Date", dataIndex: "performedDate", key: "date", width: 170,
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
    { title: "Details", dataIndex: "details", key: "details", ellipsis: true },
  ];

  return (
    <div style={{ padding: "24px 32px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <div>
          <Title level={3} style={{ margin: 0 }}>Audit Log</Title>
          <Text type="secondary">{filtered.length} records</Text>
        </div>
        <Space>
          <Select
            allowClear placeholder="Filter by module"
            value={filterModule || undefined}
            onChange={(v) => setFilterModule(v ?? "")}
            style={{ width: 180 }}
          >
            {modules.map(m => <Select.Option key={m!} value={m!}>{m}</Select.Option>)}
          </Select>
        </Space>
      </div>

      <Card style={{ borderRadius: 14, border: "1px solid var(--color-border-light, #E5E0D8)" }} styles={{ body: { padding: 0 } }}>
        <Table
          dataSource={filtered} columns={columns} rowKey="id"
          pagination={{ pageSize: 25, showTotal: (t) => `${t} records` }}
          locale={{ emptyText: "No audit records" }}
          size="small"
        />
      </Card>
    </div>
  );
}
