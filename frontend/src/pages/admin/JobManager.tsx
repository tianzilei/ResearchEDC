import { useEffect, useState } from "react";
import { Card, Table, Tag, Button, Typography, Spin, Empty, Space, Result } from "antd";
import { ThunderboltOutlined, ReloadOutlined } from "@ant-design/icons";

const { Title, Text } = Typography;

interface ExportJob {
  id: number;
  studyId: number;
  format: string;
  status: string;
  dateCreated: string;
  dateCompleted: string | null;
}

export default function JobManager() {
  const [jobs, setJobs] = useState<ExportJob[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchJobs = () => {
    setLoading(true);
    setError(null);
    fetch("/api/v1/exports?studyId=1")
      .then((r) => {
        if (!r.ok) throw new Error("API not available");
        return r.json();
      })
      .then((data) => { setJobs(data); setLoading(false); })
      .catch((e) => { setError(e.message); setLoading(false); });
  };

  useEffect(() => { fetchJobs(); }, []);

  const statusColor = (s: string) => {
    switch (s?.toLowerCase()) {
      case "completed": return "green";
      case "running": case "in_progress": return "blue";
      case "failed": return "red";
      case "cancelled": return "default";
      default: return "orange";
    }
  };

  if (loading) return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  if (error) {
    return (
      <Result
        status="warning"
        title="Jobs API Unavailable"
        subTitle="The export job module is available via the Export Center page."
        extra={<Button type="primary" onClick={() => window.location.href = "/app/data-export"}>Go to Export Center</Button>}
      />
    );
  }

  const columns = [
    { title: "Job ID", dataIndex: "id", key: "id" },
    { title: "Format", dataIndex: "format", key: "format", render: (v: string) => <Tag>{v}</Tag> },
    { title: "Status", dataIndex: "status", key: "status", render: (s: string) => <Tag color={statusColor(s)}>{s}</Tag> },
    { title: "Created", dataIndex: "dateCreated", key: "created", render: (v: string) => v ? new Date(v).toLocaleString() : "-" },
    { title: "Completed", dataIndex: "dateCompleted", key: "completed", render: (v: string) => v ? new Date(v).toLocaleString() : "-" },
  ];

  return (
    <div>
      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <ThunderboltOutlined style={{ fontSize: 22, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>Export Jobs</Title>
              <Text type="secondary">{jobs.length} job{jobs.length !== 1 ? "s" : ""}</Text>
            </div>
          </Space>
          <Button icon={<ReloadOutlined />} onClick={fetchJobs}>Refresh</Button>
        </div>
      </Card>
      {jobs.length === 0 ? (
        <Card style={{ borderRadius: 14 }}><Empty description="No export jobs found" /></Card>
      ) : (
        <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
          <Table dataSource={jobs} columns={columns} rowKey="id" pagination={false} />
        </Card>
      )}
    </div>
  );
}
