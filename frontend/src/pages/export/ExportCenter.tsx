import { useState, useCallback, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Card, Table, Tag, Button, Space, Typography, Modal, Select, Input, message, Empty, Tooltip } from "antd";
import { Alert } from "antd";
import { DownloadOutlined, ReloadOutlined, CloseCircleOutlined } from "@ant-design/icons";
import { useCurrentStudy } from "@/hooks/useStudies";
import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";
import { useAuth } from "@/providers/AuthProvider";
import APP_CONFIG from "@/config";

const { Title } = Typography;

interface ExportJob {
  id: number;
  studyId: number;
  name: string;
  exportFormat: string;
  odmContractVersion?: string;
  status: string;
  requestedBy: number;
  requestedDate: string;
  completedDate?: string;
  filePath?: string;
  fileSize?: number;
  errorMessage?: string;
  failureCode?: string;
  retryable?: boolean;
  retryCount?: number;
}

interface CreateExportJob {
  studyId: number;
  name: string;
  exportFormat: string;
  odmContractVersion?: string;
  requestedBy: number;
}

const STATUS_CONFIG: Record<string, { color: string; label: string }> = {
  PENDING: { color: "default", label: "PENDING" },
  RUNNING: { color: "processing", label: "RUNNING" },
  COMPLETED: { color: "success", label: "COMPLETED" },
  FAILED: { color: "error", label: "FAILED" },
  CANCELLED: { color: "warning", label: "CANCELLED" },
};

const CONTRACT_VERSION_OPTIONS = [
  { value: "OC2_1", label: "OC2-1 (Email-free, recommended)" },
  { value: "OC2_0_COMPAT", label: "OC2-0 (Legacy-compatible)" },
];

interface ExportFilters {
  status?: string;
  exportFormat?: string;
  odmContractVersion?: string;
}

function useExportJobs(studyId: number, filters?: ExportFilters) {
  const params = useMemo(() => {
    const p: Record<string, string | number> = { studyId };
    if (filters?.status) p.status = filters.status;
    if (filters?.exportFormat) p.exportFormat = filters.exportFormat;
    if (filters?.odmContractVersion) p.odmContractVersion = filters.odmContractVersion;
    return p;
  }, [studyId, filters?.status, filters?.exportFormat, filters?.odmContractVersion]);

  return useAppQuery<ExportJob[]>({
    queryKey: ["exports", studyId, params],
    queryFn: () => apiClient.get<ExportJob[]>(`/api/v1/exports`, params),
    enabled: studyId > 0,
  });
}

export default function ExportCenter() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const { user } = useAuth();
  const studyId = currentStudy?.id ?? 0;
  const qc = useQueryClient();
  const [filters, setFilters] = useState<ExportFilters>({});
  const { data: jobs, isLoading } = useExportJobs(studyId, filters);
  const [modalOpen, setModalOpen] = useState(false);
  const [format, setFormat] = useState<string>("ODM_XML");
  const [contractVersion, setContractVersion] = useState<string>("OC2_1");
  const [name, setName] = useState("");

  const createJob = useAppMutation<ExportJob, CreateExportJob>({
    mutationFn: (body) => apiClient.post<ExportJob>(`/api/v1/exports`, body),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["exports", studyId] }); message.success(t("export.created")); setModalOpen(false); resetModal(); },
  });

  const cancelJob = useAppMutation<void, number>({
    mutationFn: (id) => apiClient.post(`/api/v1/exports/${id}/cancel`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["exports", studyId] }),
  });

  const retryJob = useAppMutation<void, number>({
    mutationFn: (id) => apiClient.post(`/api/v1/exports/${id}/retry`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["exports", studyId] }),
  });

  const resetModal = useCallback(() => {
    setName("");
    setFormat("ODM_XML");
    setContractVersion("OC2_1");
  }, []);

  const handleDownload = useCallback(async (job: ExportJob) => {
    const url = `${APP_CONFIG.apiBaseUrl}/api/v1/exports/${job.id}/download`;
    try {
      const res = await fetch(url, { credentials: "same-origin" });
      if (!res.ok) {
        const text = await res.text().catch(() => "");
        message.error(text || t("export.error.downloadFailed"));
        return;
      }
      const blob = await res.blob();
      const disposition = res.headers.get("Content-Disposition") ?? "";
      const filenameRe = /filename="?([^";\n]+)"?/;
      const match = filenameRe.exec(disposition);
      const filename = match?.[1] ?? `export_${job.id}.xml`;
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = filename;
      link.click();
      URL.revokeObjectURL(link.href);
    } catch {
      message.error(t("export.error.downloadFailed"));
    }
  }, [t]);

  if (!currentStudy) return <Alert message={t("export.selectStudy")} type="info" />;
  if (isLoading) return <SkeletonPage />;

  const columns = [
    { title: t("export.column.name"), dataIndex: "name", key: "name" },
    { title: t("export.column.format"), dataIndex: "exportFormat", key: "exportFormat",
      render: (f: string) => <Tag>{f}</Tag>,
    },
    { title: t("export.column.contract"), dataIndex: "odmContractVersion", key: "odmContractVersion",
      render: (v: string | undefined) => v ? <Tag color="blue">{v.replace("_", "-")}</Tag> : "-",
    },
    { title: t("export.column.status"), dataIndex: "status", key: "status",
      render: (s: string, record: ExportJob) => {
        const cfg = STATUS_CONFIG[s] ?? { color: "default", label: s };
        return (
          <Tooltip title={record.status === "FAILED" ? record.errorMessage : undefined}>
            <Tag color={cfg.color}>{cfg.label}</Tag>
          </Tooltip>
        );
      },
    },
    { title: t("export.column.requested"), dataIndex: "requestedDate", key: "requestedDate",
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
    { title: t("export.column.completed"), dataIndex: "completedDate", key: "completedDate",
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
    { title: t("export.column.duration"), key: "duration",
      render: (_: unknown, record: ExportJob) => {
        if (!record.requestedDate || !record.completedDate) return "-";
        const ms = new Date(record.completedDate).getTime() - new Date(record.requestedDate).getTime();
        if (ms < 1000) return `${ms}ms`;
        return `${(ms / 1000).toFixed(1)}s`;
      },
    },
    { title: t("export.column.size"), dataIndex: "fileSize", key: "fileSize",
      render: (s: number) => s ? `${(s / 1024).toFixed(1)} KB` : "-",
    },
    {
      title: t("export.column.actions"), key: "actions",
      render: (_: unknown, r: ExportJob) => (
        <Space>
          {r.status === "COMPLETED" && r.filePath && (
            <Tooltip title={t("export.tooltip.download")}>
              <Button size="small" icon={<DownloadOutlined />} onClick={() => handleDownload(r)}>
                {t("export.action.download")}
              </Button>
            </Tooltip>
          )}
          {r.status === "COMPLETED" && !r.filePath && (
            <Tooltip title={t("export.tooltip.noArtifact")}>
              <Button size="small" icon={<DownloadOutlined />} disabled>
                {t("export.action.download")}
              </Button>
            </Tooltip>
          )}
          {r.status !== "COMPLETED" && r.status !== "FAILED" && (
            <Tooltip title={t("export.tooltip.notCompleted")}>
              <Button size="small" icon={<DownloadOutlined />} disabled>
                {t("export.action.download")}
              </Button>
            </Tooltip>
          )}
          {(r.status === "PENDING" || r.status === "RUNNING") && (
            <Button size="small" danger icon={<CloseCircleOutlined />}
              onClick={() => cancelJob.mutate(r.id)}>{t("export.action.cancel")}</Button>
          )}
          {r.status === "FAILED" && r.retryable !== false && (
            <Button size="small" type="primary" icon={<ReloadOutlined />}
              onClick={() => retryJob.mutate(r.id)}>{t("export.action.retry")}</Button>
          )}
          {r.status === "FAILED" && r.retryable === false && (
            <Tooltip title={t("export.error.notRetryable")}>
              <Button size="small" type="primary" icon={<ReloadOutlined />} disabled>
                {t("export.action.retry")}
              </Button>
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <Title level={4} style={{ marginTop: 0 }}>{t("export.title")}</Title>
        <Button type="primary" onClick={() => setModalOpen(true)}>{t("export.newExport")}</Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Space style={{ marginBottom: 16 }} wrap>
          <Select
            placeholder={t("export.filter.status")}
            allowClear
            style={{ width: 140 }}
            value={filters.status}
            onChange={(v) => setFilters((f) => ({ ...f, status: v }))}
            options={[
              { value: "PENDING", label: "PENDING" },
              { value: "RUNNING", label: "RUNNING" },
              { value: "COMPLETED", label: "COMPLETED" },
              { value: "FAILED", label: "FAILED" },
              { value: "CANCELLED", label: "CANCELLED" },
            ]}
          />
          <Select
            placeholder={t("export.filter.format")}
            allowClear
            style={{ width: 140 }}
            value={filters.exportFormat}
            onChange={(v) => setFilters((f) => ({ ...f, exportFormat: v }))}
            options={[
              { value: "ODM_XML", label: "ODM XML" },
              { value: "CSV", label: "CSV" },
              { value: "EXCEL", label: "Excel" },
              { value: "SAS_XPORT", label: "SAS XPORT" },
            ]}
          />
          <Select
            placeholder={t("export.filter.contract")}
            allowClear
            style={{ width: 160 }}
            value={filters.odmContractVersion}
            onChange={(v) => setFilters((f) => ({ ...f, odmContractVersion: v }))}
            options={CONTRACT_VERSION_OPTIONS}
          />
        </Space>
        <Table dataSource={jobs ?? []} columns={columns} rowKey="id" pagination={{ pageSize: 10 }}
          locale={{ emptyText: <Empty description={t("export.empty")} /> }} />
      </Card>

      <Modal title={t("export.modal.title")} open={modalOpen}
        onCancel={() => { setModalOpen(false); resetModal(); }}
        onOk={() => createJob.mutate({
          studyId,
          name,
          exportFormat: format,
          odmContractVersion: format === "ODM_XML" ? contractVersion : undefined,
          requestedBy: user?.userId ?? 0,
        })}
        confirmLoading={createJob.isPending}>
        <Space direction="vertical" style={{ width: "100%" }}>
          <label>{t("export.modal.jobName")}</label>
          <Input value={name} onChange={(e) => setName(e.target.value)} placeholder={t("export.modal.jobNamePlaceholder")} />
          <label>{t("export.modal.format")}</label>
          <Select value={format} onChange={setFormat} style={{ width: "100%" }}>
            <Select.Option value="ODM_XML">{t("export.modal.formats.odm")}</Select.Option>
            <Select.Option value="CSV">{t("export.modal.formats.csv")}</Select.Option>
            <Select.Option value="EXCEL">{t("export.modal.formats.excel")}</Select.Option>
            <Select.Option value="SAS_XPORT">{t("export.modal.formats.sas")}</Select.Option>
          </Select>
          {format === "ODM_XML" && (
            <>
              <label>{t("export.modal.contractVersion")}</label>
              <Select value={contractVersion} onChange={setContractVersion} style={{ width: "100%" }}
                options={CONTRACT_VERSION_OPTIONS} />
              <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                {t("export.modal.contractHint")}
              </Typography.Text>
            </>
          )}
        </Space>
      </Modal>
    </div>
  );
}
