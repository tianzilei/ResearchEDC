import { useEffect, useState } from "react";
import {
  Card, Table, Tag, Button, Typography, Space, Modal, Form, Select,
  Spin, message, Empty, Result, Statistic, Row, Col,
} from "antd";


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
      fetch("/api/v1/exports?studyId=1").then(r => r.ok ? r.json() : Promise.reject(new Error("API unavailable"))),
      fetch("/api/v1/studies").then(r => r.ok ? r.json() : []),
    ]).then(([data, ss]) => {
      setJobs(data);
      setStudies(Array.isArray(ss) ? ss.map((s: Record<string, unknown>) => ({ studyId: (s.studyId ?? s.id) as number, name: s.name as string })) : []);
      setLoading(false);
    }).catch((e: unknown) => { setError(e instanceof Error ? e.message : "Error"); setLoading(false); });
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
        title="任务 API 不可用"
        subTitle="导出任务服务可通过导出中心页面访问。"
        extra={<Button type="primary" onClick={() => window.location.href = "/app/data-export"}>前往导出中心</Button>}
      />
    );
  }

  const statusClass = (s: string) => {
    switch (s?.toLowerCase()) {
      case "completed": return "status-success";
      case "running": case "in_progress": return "status-info";
      case "failed": return "status-danger";
      case "cancelled": return "status-default";
      default: return "status-warning";
    }
  };

  const stats = {
    total: jobs.length,
    running: jobs.filter(j => j.status === "RUNNING" || j.status === "in_progress").length,
    completed: jobs.filter(j => j.status === "COMPLETED" || j.status === "completed").length,
    failed: jobs.filter(j => j.status === "FAILED" || j.status === "failed").length,
  };

  const columns = [
    { title: "任务 ID", dataIndex: "id", key: "id", width: 80 },
    {
      title: "格式", dataIndex: "format", key: "format",
      render: (v: string) => <Tag>{v}</Tag>,
    },
    {
      title: "状态", dataIndex: "status", key: "status",
      render: (s: string) => <span className={`status ${statusClass(s)}`}>{s}</span>,
    },
    {
      title: "创建时间", dataIndex: "dateCreated", key: "created",
      render: (v: string) => v ? new Date(v).toLocaleString() : "-",
    },
    {
      title: "完成时间", dataIndex: "dateCompleted", key: "completed",
      render: (v: string) => v ? new Date(v).toLocaleString() : "-",
    },
    {
      title: "", key: "actions",
      render: (_: unknown, record: ExportJob) => (
        <Space>
          {(record.status === "RUNNING" || record.status === "PENDING") && (
            <Button size="small" danger
              onClick={() => handleCancel(record.id)}>取消</Button>
          )}
          {record.status === "FAILED" && (
            <Button size="small"
              onClick={() => handleRetry(record.id)}>重试</Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card><Statistic title="总任务数" value={stats.total} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="运行中" value={stats.running} valueStyle={{ color: "var(--info)" }} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="已完成" value={stats.completed} valueStyle={{ color: "var(--success)" }} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="失败" value={stats.failed} valueStyle={{ color: "var(--danger)" }} /></Card>
        </Col>
      </Row>

      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>导出任务</Title>
              <Text type="secondary">{jobs.length} 个任务</Text>
            </div>
          </Space>
          <Space>
            <Button type="primary" onClick={() => setCreateOpen(true)}>
              新建导出任务
            </Button>
            <Button onClick={fetchJobs}>刷新</Button>
          </Space>
        </div>
      </Card>

      {jobs.length === 0 ? (
        <Card><Empty description="暂无导出任务" /></Card>
      ) : (
        <Card styles={{ body: { padding: 0 } }}>
          <Table dataSource={jobs} columns={columns} rowKey="id" pagination={false} />
        </Card>
      )}

      <Modal title="创建导出任务" open={createOpen}
        onOk={handleCreate} onCancel={() => { setCreateOpen(false); form.resetFields(); }}>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="studyId" label="研究" rules={[{ required: true }]}>
            <Select placeholder="选择研究" showSearch>
              {studies.map(s => <Select.Option key={s.studyId} value={s.studyId}>{s.name}</Select.Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="format" label="格式" rules={[{ required: true }]}>
            <Select placeholder="选择格式">
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
