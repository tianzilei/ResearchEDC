import { useState } from "react";
import { Card, Table, Tag, Button, Space, Typography, Modal, Select, Input, message, Empty, Progress } from "antd";
import { ExportOutlined, ReloadOutlined, StopOutlined, DownloadOutlined } from "@ant-design/icons";
import { Alert } from "antd";
import { useCurrentStudy } from "@/hooks/useStudies";
import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title } = Typography;

interface ExportJob {
  id: number;
  studyId: number;
  name: string;
  exportFormat: string;
  status: string;
  requestedBy: number;
  requestedDate: string;
  completedDate?: string;
  filePath?: string;
  fileSize?: number;
  errorMessage?: string;
  retryCount?: number;
}

const statusColors: Record<string, string> = {
  PENDING: "default", RUNNING: "processing", COMPLETED: "success", FAILED: "error", CANCELLED: "warning",
};

function useExportJobs(studyId: number) {
  return useAppQuery<ExportJob[]>({
    queryKey: ["exports", studyId],
    queryFn: () => apiClient.get<ExportJob[]>(`/api/v1/exports`, { studyId }),
    enabled: studyId > 0,
  });
}

export default function ExportCenter() {
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id ?? 0;
  const qc = useQueryClient();
  const { data: jobs, isLoading } = useExportJobs(studyId);
  const [modalOpen, setModalOpen] = useState(false);
  const [format, setFormat] = useState<string>("ODM_XML");
  const [name, setName] = useState("");

  const createJob = useAppMutation<ExportJob, any>({
    mutationFn: (body) => apiClient.post<ExportJob>(`/api/v1/exports`, body),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["exports", studyId] }); message.success("Export job created"); setModalOpen(false); },
  });

  const cancelJob = useAppMutation<void, number>({
    mutationFn: (id) => apiClient.post(`/api/v1/exports/${id}/cancel`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["exports", studyId] }),
  });

  const retryJob = useAppMutation<void, number>({
    mutationFn: (id) => apiClient.post(`/api/v1/exports/${id}/retry`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["exports", studyId] }),
  });

  if (!currentStudy) return <Alert message="Please select a study" type="info" showIcon />;
  if (isLoading) return <SkeletonPage />;

  const columns = [
    { title: "Name", dataIndex: "name", key: "name" },
    { title: "Format", dataIndex: "exportFormat", key: "exportFormat",
      render: (f: string) => <Tag>{f}</Tag>,
    },
    { title: "Status", dataIndex: "status", key: "status",
      render: (s: string) => (
        <Space>
          <Tag color={statusColors[s]}>{s}</Tag>
          {s === "RUNNING" && <Progress size="small" type="circle" percent={50} width={20} />}
        </Space>
      ),
    },
    { title: "Requested", dataIndex: "requestedDate", key: "requestedDate",
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
    { title: "Size", dataIndex: "fileSize", key: "fileSize",
      render: (s: number) => s ? `${(s / 1024).toFixed(1)} KB` : "-",
    },
    {
      title: "Actions", key: "actions",
      render: (_: any, r: ExportJob) => (
        <Space>
          {r.status === "COMPLETED" && r.filePath && (
            <Button size="small" icon={<DownloadOutlined />} type="link">Download</Button>
          )}
          {(r.status === "PENDING" || r.status === "RUNNING") && (
            <Button size="small" icon={<StopOutlined />} onClick={() => cancelJob.mutate(r.id)}>Cancel</Button>
          )}
          {r.status === "FAILED" && (
            <Button size="small" icon={<ReloadOutlined />} onClick={() => retryJob.mutate(r.id)}>Retry</Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <Title level={4} style={{ marginTop: 0 }}><ExportOutlined /> Export Center</Title>
        <Button type="primary" icon={<ExportOutlined />} onClick={() => setModalOpen(true)}>New Export</Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table dataSource={jobs ?? []} columns={columns} rowKey="id" pagination={{ pageSize: 10 }}
          locale={{ emptyText: <Empty description="No export jobs. Create one to start." /> }} />
      </Card>

      <Modal title="Create Export Job" open={modalOpen} onCancel={() => setModalOpen(false)}
        onOk={() => createJob.mutate({ studyId, name, exportFormat: format, requestedBy: 0 })}
        confirmLoading={createJob.isPending}>
        <Space direction="vertical" style={{ width: "100%" }}>
          <label>Job Name</label>
          <Input value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g., Full Study Export" />
          <label>Export Format</label>
          <Select value={format} onChange={setFormat} style={{ width: "100%" }}>
            <Select.Option value="ODM_XML">ODM XML</Select.Option>
            <Select.Option value="CSV">CSV</Select.Option>
            <Select.Option value="EXCEL">Excel</Select.Option>
            <Select.Option value="SAS_XPORT">SAS XPORT</Select.Option>
          </Select>
        </Space>
      </Modal>
    </div>
  );
}
