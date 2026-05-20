import { useEffect, useState } from "react";
import {
  Card, Table, Tag, Button, Typography, Space, Modal, Form, Select,
  Spin, message, Empty, Result, Statistic, Row, Col,
} from "antd";
import {
  ThunderboltOutlined, ReloadOutlined, PlusOutlined,
  StopOutlined, CheckCircleOutlined, CloseCircleOutlined,
} from "@ant-design/icons";

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
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();
  const [studies, setStudies] = useState<{ studyId: number; name: string }[]>([]);

  const fetchJobs = () => {
    setLoading(true);
    setError(null);
    Promise.all([
      fetch("/api/v1/exports?studyId=1").then(r => r.ok ? r.json() : Promise.reject("API unavailable")),
      fetch("/api/v1/studies").then(r => r.ok ? r.json() : []),
    ]).then(([data, ss]) => {
      setJobs(data);
      setStudies(Array.isArray(ss) ? ss.map((s: any) => ({ studyId: s.studyId ?? s.id, name: s.name })) : []);
      setLoading(false);
    }).catch((e) => { setError(e?.message ?? "Error"); setLoading(false); });
  };

  useEffect(() => { fetchJobs(); }, []);

  const handleCreate = async () => {
    try {
      const vals = await form.validateFields();
      const res = await fetch("/api/v1/exports", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ studyId: vals.studyId, format: vals.format }),
      });
      if (!res.ok) { message.error("Failed to create job"); return; }
      message.success("Export job created");
      setCreateOpen(false);
      form.resetFields();
      fetchJobs();
    } catch { /* validation */ }
  };

  const handleCancel = async (jobId: number) => {
    const res = await fetch(`/api/v1/exports/${jobId}/cancel`, { method: "POST" });
    if (res.ok) { message.success("Job cancelled"); fetchJobs(); }
    else message.error("Failed to cancel job");
  };

  const handleRetry = async (jobId: number) => {
    const res = await fetch(`/api/v1/exports/${jobId}/retry`, { method: "POST" });
    if (res.ok) { message.success("Job retrying"); fetchJobs(); }
    else message.error("Failed to retry job");
  };

  if (loading) return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;

  if (error) {
    return (
      <Result
        status="warning"
        title="Jobs API Unavailable"
        subTitle="The export job service is available via the Export Center page."
        extra={<Button type="primary" onClick={() => window.location.href = "/app/data-export"}>Go to Export Center</Button>}
      />
    );
  }

  const statusColor = (s: string) => {
    switch (s?.toLowerCase()) {
      case "completed": return "green";
      case "running": case "in_progress": return "blue";
      case "failed": return "red";
      case "cancelled": return "default";
      default: return "orange";
    }
  };

  const stats = {
    total: jobs.length,
    running: jobs.filter(j => j.status === "RUNNING" || j.status === "in_progress").length,
    completed: jobs.filter(j => j.status === "COMPLETED" || j.status === "completed").length,
    failed: jobs.filter(j => j.status === "FAILED" || j.status === "failed").length,
  };

  const columns = [
    { title: "Job ID", dataIndex: "id", key: "id", width: 80 },
    {
      title: "Format", dataIndex: "format", key: "format",
      render: (v: string) => <Tag>{v}</Tag>,
    },
    {
      title: "Status", dataIndex: "status", key: "status",
      render: (s: string) => <Tag color={statusColor(s)}>{s}</Tag>,
    },
    {
      title: "Created", dataIndex: "dateCreated", key: "created",
      render: (v: string) => v ? new Date(v).toLocaleString() : "-",
    },
    {
      title: "Completed", dataIndex: "dateCompleted", key: "completed",
      render: (v: string) => v ? new Date(v).toLocaleString() : "-",
    },
    {
      title: "", key: "actions",
      render: (_: any, record: ExportJob) => (
        <Space>
          {(record.status === "RUNNING" || record.status === "PENDING") && (
            <Button size="small" danger icon={<StopOutlined />}
              onClick={() => handleCancel(record.id)}>Cancel</Button>
          )}
          {record.status === "FAILED" && (
            <Button size="small" icon={<ReloadOutlined />}
              onClick={() => handleRetry(record.id)}>Retry</Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card style={{ borderRadius: 14 }}><Statistic title="Total Jobs" value={stats.total} prefix={<ThunderboltOutlined />} /></Card>
        </Col>
        <Col span={6}>
          <Card style={{ borderRadius: 14 }}><Statistic title="Running" value={stats.running} valueStyle={{ color: "#1677ff" }} /></Card>
        </Col>
        <Col span={6}>
          <Card style={{ borderRadius: 14 }}><Statistic title="Completed" value={stats.completed} valueStyle={{ color: "#52c41a" }} prefix={<CheckCircleOutlined />} /></Card>
        </Col>
        <Col span={6}>
          <Card style={{ borderRadius: 14 }}><Statistic title="Failed" value={stats.failed} valueStyle={{ color: "#ff4d4f" }} prefix={<CloseCircleOutlined />} /></Card>
        </Col>
      </Row>

      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <ThunderboltOutlined style={{ fontSize: 22, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>Export Jobs</Title>
              <Text type="secondary">{jobs.length} job{jobs.length !== 1 ? "s" : ""}</Text>
            </div>
          </Space>
          <Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
              New Export Job
            </Button>
            <Button icon={<ReloadOutlined />} onClick={fetchJobs}>Refresh</Button>
          </Space>
        </div>
      </Card>

      {jobs.length === 0 ? (
        <Card style={{ borderRadius: 14 }}><Empty description="No export jobs" /></Card>
      ) : (
        <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
          <Table dataSource={jobs} columns={columns} rowKey="id" pagination={false} />
        </Card>
      )}

      <Modal title="Create Export Job" open={createOpen}
        onOk={handleCreate} onCancel={() => { setCreateOpen(false); form.resetFields(); }}>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="studyId" label="Study" rules={[{ required: true }]}>
            <Select placeholder="Select study" showSearch>
              {studies.map(s => <Select.Option key={s.studyId} value={s.studyId}>{s.name}</Select.Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="format" label="Format" rules={[{ required: true }]}>
            <Select placeholder="Select format">
              <Select.Option value="CSV">CSV</Select.Option>
              <Select.Option value="XLSX">Excel (XLSX)</Select.Option>
              <Select.Option value="ODM">ODM XML</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
