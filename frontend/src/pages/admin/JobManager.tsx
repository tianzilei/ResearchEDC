import { useMemo, useState } from "react";
import {
  Card, Table, Tag, Button, Typography, Space, Modal, Form, Select,
  Spin, message, Empty, Result, Statistic, Row, Col,
} from "antd";

import { useAppQuery } from "@/hooks/useQuery";
import { exportApi, type ExportJobDTO } from "@/api/exports";
import { studyApi } from "@/api/studies";

const { Title, Text } = Typography;

export default function JobManager() {
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();
  const [selectedStudyId, setSelectedStudyId] = useState<number | undefined>();

  const { data: studies = [], isLoading: loadingStudies } = useAppQuery({
    queryKey: ["studies", "list"],
    queryFn: () => studyApi.list(),
  });

  const activeStudyId = selectedStudyId ?? studies[0]?.studyId;

  const {
    data: jobs = [],
    isLoading: loadingJobs,
    refetch: refetchJobs,
    error,
  } = useAppQuery<ExportJobDTO[]>({
    queryKey: ["exports", "admin", activeStudyId],
    queryFn: () =>
      activeStudyId
        ? exportApi.listJobs(activeStudyId)
        : Promise.resolve([]),
    enabled: !!activeStudyId,
  });

  const handleCreate = async () => {
    try {
      const vals = await form.validateFields();
      await exportApi.createJob({
        studyId: vals.studyId,
        name: `Admin export ${new Date().toISOString()}`,
        exportFormat: vals.format,
      });
      message.success("Export job created");
      setCreateOpen(false);
      form.resetFields();
      setSelectedStudyId(vals.studyId);
      void refetchJobs();
    } catch { /* validation or API error */ }
  };

  const handleCancel = async (jobId: number) => {
    try {
      await exportApi.cancelJob(jobId);
      message.success("Job cancelled");
      void refetchJobs();
    } catch {
      message.error("Failed to cancel job");
    }
  };

  const handleRetry = async (jobId: number) => {
    try {
      await exportApi.retryJob(jobId);
      message.success("Job retrying");
      void refetchJobs();
    } catch {
      message.error("Failed to retry job");
    }
  };

  const loading = loadingStudies || loadingJobs;

  const studyOptions = useMemo(
    () => studies.map((s) => ({ studyId: s.studyId, name: s.name })),
    [studies],
  );

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
    running: jobs.filter(j => j.status === "RUNNING").length,
    completed: jobs.filter(j => j.status === "COMPLETED").length,
    failed: jobs.filter(j => j.status === "FAILED").length,
  };

  const columns = [
    { title: "任务 ID", dataIndex: "id", key: "id", width: 80 },
    {
      title: "格式", dataIndex: "exportFormat", key: "format",
      render: (v: string) => <Tag>{v}</Tag>,
    },
    {
      title: "状态", dataIndex: "status", key: "status",
      render: (s: string) => <span className={`status ${statusClass(s)}`}>{s}</span>,
    },
    {
      title: "创建时间", dataIndex: "requestedDate", key: "created",
      render: (v: string) => v ? new Date(v).toLocaleString() : "-",
    },
    {
      title: "完成时间", dataIndex: "completedDate", key: "completed",
      render: (v: string | null) => v ? new Date(v).toLocaleString() : "-",
    },
    {
      title: "", key: "actions",
      render: (_: unknown, record: ExportJobDTO) => (
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
            <Select
              placeholder="选择研究"
              value={activeStudyId}
              onChange={setSelectedStudyId}
              style={{ width: 220 }}
              options={studyOptions.map((s) => ({ value: s.studyId, label: s.name }))}
            />
            <Button type="primary" onClick={() => setCreateOpen(true)}>
              新建导出任务
            </Button>
            <Button onClick={() => void refetchJobs()}>刷新</Button>
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
          <Form.Item name="studyId" label="研究" rules={[{ required: true }]} initialValue={activeStudyId}>
            <Select
              placeholder="选择研究"
              showSearch
              options={studyOptions.map((s) => ({ value: s.studyId, label: s.name }))}
            />
          </Form.Item>
          <Form.Item name="format" label="格式" rules={[{ required: true }]}>
            <Select placeholder="选择格式">
              <Select.Option value="CSV">CSV</Select.Option>
              <Select.Option value="EXCEL">Excel</Select.Option>
              <Select.Option value="ODM_XML">ODM XML</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
