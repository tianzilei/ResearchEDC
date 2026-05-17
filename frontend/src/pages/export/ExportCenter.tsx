import { useState } from "react";
import { useTranslation } from "react-i18next";
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
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id ?? 0;
  const qc = useQueryClient();
  const { data: jobs, isLoading } = useExportJobs(studyId);
  const [modalOpen, setModalOpen] = useState(false);
  const [format, setFormat] = useState<string>("ODM_XML");
  const [name, setName] = useState("");

  const createJob = useAppMutation<ExportJob, any>({
    mutationFn: (body) => apiClient.post<ExportJob>(`/api/v1/exports`, body),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["exports", studyId] }); message.success(t("export.created")); setModalOpen(false); },
  });

  const cancelJob = useAppMutation<void, number>({
    mutationFn: (id) => apiClient.post(`/api/v1/exports/${id}/cancel`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["exports", studyId] }),
  });

  const retryJob = useAppMutation<void, number>({
    mutationFn: (id) => apiClient.post(`/api/v1/exports/${id}/retry`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["exports", studyId] }),
  });

  if (!currentStudy) return <Alert message={t("export.selectStudy")} type="info" showIcon />;
  if (isLoading) return <SkeletonPage />;

  const columns = [
    { title: t("export.column.name"), dataIndex: "name", key: "name" },
    { title: t("export.column.format"), dataIndex: "exportFormat", key: "exportFormat",
      render: (f: string) => <Tag>{f}</Tag>,
    },
    { title: t("export.column.status"), dataIndex: "status", key: "status",
      render: (s: string) => (
        <Space>
          <Tag color={statusColors[s]}>{s}</Tag>
          {s === "RUNNING" && <Progress size="small" type="circle" percent={50} width={20} />}
        </Space>
      ),
    },
    { title: t("export.column.requested"), dataIndex: "requestedDate", key: "requestedDate",
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
    { title: t("export.column.size"), dataIndex: "fileSize", key: "fileSize",
      render: (s: number) => s ? `${(s / 1024).toFixed(1)} KB` : "-",
    },
    {
      title: t("export.column.actions"), key: "actions",
      render: (_: any, r: ExportJob) => (
        <Space>
          {r.status === "COMPLETED" && r.filePath && (
            <Button size="small" icon={<DownloadOutlined />} type="link">{t("export.action.download")}</Button>
          )}
          {(r.status === "PENDING" || r.status === "RUNNING") && (
            <Button size="small" icon={<StopOutlined />} onClick={() => cancelJob.mutate(r.id)}>{t("export.action.cancel")}</Button>
          )}
          {r.status === "FAILED" && (
            <Button size="small" icon={<ReloadOutlined />} onClick={() => retryJob.mutate(r.id)}>{t("export.action.retry")}</Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <Title level={4} style={{ marginTop: 0 }}><ExportOutlined /> {t("export.title")}</Title>
        <Button type="primary" icon={<ExportOutlined />} onClick={() => setModalOpen(true)}>{t("export.newExport")}</Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table dataSource={jobs ?? []} columns={columns} rowKey="id" pagination={{ pageSize: 10 }}
          locale={{ emptyText: <Empty description={t("export.empty")} /> }} />
      </Card>

      <Modal title={t("export.modal.title")} open={modalOpen} onCancel={() => setModalOpen(false)}
        onOk={() => createJob.mutate({ studyId, name, exportFormat: format, requestedBy: 0 })}
        confirmLoading={createJob.isPending}>
        <Space direction="vertical" style={{ width: "100%" }}>
          <label>{t("export.modal.jobName")}</label>
          <Input value={name} onChange={(e) => setName(e.target.value)} placeholder={t("export.modal.jobNamePlaceholder")} />
          <label>{t("export.modal.format")}</label>
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
