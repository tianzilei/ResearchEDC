import { useState, useCallback } from "react";
import {
  Card, Button, Typography, Space, Upload, Steps, Table, Tag,
  Alert, Empty, Breadcrumb, message,
} from "antd";
import { Link } from "react-router-dom";
import { useCurrentStudy } from "@/hooks/useStudies";
import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface ImportTypeInfo {
  key: string;
  label: string;
  desc: string;
  accept: string;
}

interface ImportPreview {
  status: string;
  eventCrfs: number;
  totalItems: number;
  editCheckErrors: number;
  errors: string[];
  warnings: string[];
}

interface ImportResult {
  status: string;
  eventCrfs: number;
  items: number;
  warnings: string[];
  errors: string[];
}

interface ImportJob {
  id: number;
  studyId: number;
  importType: string;
  fileName: string;
  fileSize?: number;
  status: string;
  errorMessage?: string;
  summaryJson?: string;
  requestedDate?: string;
  completedDate?: string;
}

const IMPORT_TYPES: ImportTypeInfo[] = [
  { key: "CRF_DATA", label: "CRF 数据", desc: "从 ODM XML 文件导入 CRF 数据", accept: ".xml" },
  { key: "CRF_DEFINITION", label: "CRF 定义", desc: "上传 Excel/XML 文件导入 CRF 定义和元数据", accept: ".xml,.xls,.xlsx" },
];

const STEP_TITLES = ["选择类型", "上传文件", "管理任务"];

const STATUS_MAP: Record<string, { label: string; className: string }> = {
  STAGED: { label: "已暂存", className: "status-default" },
  VALIDATING: { label: "验证中", className: "status-info" },
  VALIDATED: { label: "已验证", className: "status-success" },
  INVALID: { label: "验证失败", className: "status-danger" },
  BLOCKED: { label: "已阻止", className: "status-warning" },
  COMMITTING: { label: "提交中", className: "status-info" },
  COMPLETED: { label: "已完成", className: "status-success" },
  FAILED: { label: "失败", className: "status-danger" },
};

function useImportJobs(studyId: number) {
  return useAppQuery<ImportJob[]>({
    queryKey: ["imports", studyId],
    queryFn: () => apiClient.get<ImportJob[]>("/api/v1/imports", { studyId }),
    enabled: studyId > 0,
    refetchInterval: 3000,
  });
}

export default function ImportManager() {
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id ?? 0;
  const qc = useQueryClient();

  const [currentStep, setCurrentStep] = useState(0);
  const [selectedType, setSelectedType] = useState<string | undefined>();
  const [uploadedJob, setUploadedJob] = useState<ImportJob | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [previews, setPreviews] = useState<Record<number, ImportPreview>>({});

  const { data: jobs, isLoading: jobsLoading } = useImportJobs(studyId);

  const validateJob = useAppMutation<ImportPreview, number>({
    mutationFn: (id) => apiClient.post<ImportPreview>("/api/v1/imports/" + id + "/validate"),
    onSuccess: (preview, id) => {
      setPreviews((current) => ({ ...current, [id]: preview }));
      qc.invalidateQueries({ queryKey: ["imports", studyId] });
      if (preview.status === "validated") {
        message.success("验证完成");
      } else if (preview.status === "blocked" || preview.status === "invalid") {
        message.warning("验证完成，请查看问题");
      } else {
        message.error("验证失败");
      }
    },
    onError: () => message.error("验证失败"),
  });

  const commitJob = useAppMutation<ImportResult, number>({
    mutationFn: (id) => apiClient.post<ImportResult>("/api/v1/imports/" + id + "/commit"),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["imports", studyId] });
      message.success("导入提交成功");
    },
    onError: () => message.error("提交失败"),
  });

  const currentImportType = IMPORT_TYPES.find(t => t.key === selectedType);

  const previewFor = useCallback((job: ImportJob): ImportPreview | undefined => {
    if (previews[job.id]) return previews[job.id];
    if (!job.summaryJson) return undefined;
    try {
      const parsed = JSON.parse(job.summaryJson) as ImportPreview;
      return parsed.status ? parsed : undefined;
    } catch {
      return undefined;
    }
  }, [previews]);

  const handleUpload = useCallback(async (file: File) => {
    if (!selectedType) { message.warning("请先选择导入类型"); return false; }
    setUploading(true);
    setUploadError(null);
    const formData = new FormData();
    formData.append("file", file);
    formData.append("importType", selectedType);
    if (studyId) formData.append("studyId", String(studyId));
    try {
      const res = await fetch("/api/v1/imports/upload", { method: "POST", body: formData });
      if (res.ok) {
        const job: ImportJob = await res.json();
        setUploadedJob(job);
        qc.invalidateQueries({ queryKey: ["imports", studyId] });
        message.success("文件上传成功，导入任务已创建");
        setCurrentStep(2);
      } else {
        setUploadError("文件上传失败，请重试");
      }
    } catch {
      setUploadError("网络错误，请检查连接后重试");
    } finally {
      setUploading(false);
    }
    return false;
  }, [selectedType, studyId, qc]);

  const handleReset = useCallback(() => {
    setSelectedType(undefined); setUploadedJob(null); setUploadError(null); setCurrentStep(0);
  }, []);

  if (!currentStudy) {
    return (
      <div>
        <Breadcrumb items={[{ title: <Link to="/app/admin">管理</Link> }, { title: "数据导入" }]} style={{ marginBottom: 16 }} />
        <Alert type="info" message="请先在顶部选择研究" />
      </div>
    );
  }

  const renderStepSelectType = () => (
    <div>
      <Space direction="vertical" style={{ width: "100%" }} size={12}>
        {IMPORT_TYPES.map((item) => {
          const isSelected = selectedType === item.key;
          return (
            <Card key={item.key} size="small" hoverable onClick={() => setSelectedType(item.key)}
              style={isSelected ? { borderColor: "var(--accent)", borderWidth: 2 } : undefined}>
              <Space style={{ width: "100%", justifyContent: "space-between" }}>
                <div>
                  <Text strong>{item.label}</Text>
                  <Tag style={{ marginLeft: 8 }}>{item.accept}</Tag>
                  <br />
                  <Text type="secondary">{item.desc}</Text>
                </div>
                {isSelected && <Tag color="blue">已选择</Tag>}
              </Space>
            </Card>
          );
        })}
      </Space>
    </div>
  );

  const renderStepUpload = () => (
    <Space direction="vertical" style={{ width: "100%" }}>
      <Alert type="info" message={`导入类型: ${currentImportType?.label}`} description={`支持格式: ${currentImportType?.accept}`} style={{ marginBottom: 8 }} />
      {uploadError && <Alert type="error" message={uploadError} showIcon style={{ marginBottom: 8 }} />}
      {uploadedJob ? (
        <div style={{ padding: "16px 20px", background: "var(--panel-muted)", borderRadius: "var(--radius-lg)" }}>
          <Text strong style={{ display: "block", marginBottom: 4 }}>上传完成 — 导入任务已创建</Text>
          <Text type="secondary" style={{ display: "block" }}>任务 ID: {uploadedJob.id}</Text>
          <Text type="secondary" style={{ display: "block" }}>文件名: {uploadedJob.fileName}</Text>
          <Text type="secondary" style={{ display: "block" }}>状态: {STATUS_MAP[uploadedJob.status]?.label ?? uploadedJob.status}</Text>
          <Button size="small" style={{ marginTop: 8 }} onClick={() => { setUploadedJob(null); setUploadError(null); }}>重新上传</Button>
        </div>
      ) : (
        <Upload.Dragger name="file" multiple={false} beforeUpload={handleUpload} showUploadList={false} accept={currentImportType?.accept} disabled={uploading}>
          <p className="ant-upload-text">{uploading ? "上传中..." : `点击或拖拽 ${currentImportType?.accept} 文件上传`}</p>
          <p className="ant-upload-hint">上传后将自动创建导入任务</p>
        </Upload.Dragger>
      )}
    </Space>
  );

  const renderStepManage = () => {
    if (jobsLoading && !jobs) return <SkeletonPage />;
    const jobList = jobs ?? [];
    const stagedCount = jobList.filter(j => j.status === "STAGED").length;
    const validatedCount = jobList.filter(j => j.status === "VALIDATED").length;
    const completedCount = jobList.filter(j => j.status === "COMPLETED").length;
    const failedCount = jobList.filter(j => j.status === "FAILED" || j.status === "INVALID" || j.status === "BLOCKED").length;

    const columns = [
      { title: "ID", dataIndex: "id", key: "id", width: 64, render: (v: number) => <span className="number-display">{v}</span> },
      { title: "导入类型", dataIndex: "importType", key: "importType", width: 120,
        render: (v: string) => <Tag>{IMPORT_TYPES.find(t => t.key === v)?.label ?? v}</Tag> },
      { title: "文件", dataIndex: "fileName", key: "fileName", ellipsis: true },
      { title: "状态", dataIndex: "status", key: "status", width: 100,
        render: (s: string) => {
          const info = STATUS_MAP[s];
          return <span className={`status ${info?.className ?? "status-default"}`}>{info?.label ?? s}</span>;
        }},
      { title: "验证摘要", key: "preview", width: 220,
        render: (_: unknown, record: ImportJob) => {
          const preview = previewFor(record);
          if (!preview) return <Text type="secondary">-</Text>;
          const hasIssues = preview.errors.length > 0 || preview.warnings.length > 0 || preview.editCheckErrors > 0;
          return (
            <Space direction="vertical" size={2}>
              <Text style={{ fontSize: "var(--font-size-xs)" }}>事件 CRF: {preview.eventCrfs} / 条目: {preview.totalItems}</Text>
              {hasIssues && (
                <Text type={preview.errors.length > 0 ? "danger" : "warning"} style={{ fontSize: "var(--font-size-xs)" }}>
                  {preview.errors[0] ?? preview.warnings[0] ?? `${preview.editCheckErrors} 个编辑检查问题`}
                </Text>
              )}
            </Space>
          );
        }},
      { title: "创建时间", dataIndex: "requestedDate", key: "requestedDate", width: 160,
        render: (d: string) => (d ? new Date(d).toLocaleString() : "-") },
      { title: "操作", key: "actions", width: 180,
        render: (_: unknown, record: ImportJob) => (
          <Space>
            {record.status === "STAGED" && <Button size="small" onClick={() => validateJob.mutate(record.id)} loading={validateJob.isPending}>验证</Button>}
            {record.status === "VALIDATED" && <Button size="small" type="primary" onClick={() => commitJob.mutate(record.id)} loading={commitJob.isPending}>提交</Button>}
            {(record.status === "FAILED" || record.status === "INVALID" || record.status === "BLOCKED") && record.errorMessage && <Text type="danger" style={{ fontSize: "var(--font-size-xs)" }}>{record.errorMessage}</Text>}
          </Space>
        )},
    ];

    return (
      <div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 12, marginBottom: 16 }}>
          {[{ l: "已暂存", n: stagedCount }, { l: "已验证", n: validatedCount }, { l: "已完成", n: completedCount }, { l: "失败", n: failedCount }].map(({ l, n }) => (
            <Card key={l} size="small" styles={{ body: { padding: "12px 16px" } }}>
              <Text type="secondary" style={{ fontSize: "var(--font-size-xs)" }}>{l}</Text><br />
              <Text strong style={{ fontSize: "var(--font-size-stat)" }}>{n}</Text>
            </Card>
          ))}
        </div>
        <Card styles={{ body: { padding: 0 } }}>
          {jobList.length === 0
            ? <div style={{ padding: "40px 0" }}><Empty description="暂无导入任务" /></div>
            : <Table dataSource={jobList} columns={columns} rowKey="id" pagination={{ pageSize: 10 }} locale={{ emptyText: <Empty description="暂无导入任务" /> }} />}
        </Card>
      </div>
    );
  };

  const stepContent = () => {
    switch (currentStep) {
      case 0: return renderStepSelectType();
      case 1: return renderStepUpload();
      case 2: return renderStepManage();
      default: return null;
    }
  };

  return (
    <div>
      <Breadcrumb items={[{ title: <Link to="/app/admin">管理</Link> }, { title: "数据导入" }]} style={{ marginBottom: 16 }} />
      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>数据导入</Title>
              <Text type="secondary">导入 CRF 数据和定义 — 选择类型、上传文件（自动创建任务）、验证并提交</Text>
            </div>
          </Space>
          <Space>
            {currentStep > 0 && <Button onClick={handleReset}>重新开始</Button>}
            {currentStep === 2 && <Button onClick={() => setCurrentStep(0)}>新建导入</Button>}
          </Space>
        </div>
      </Card>
      <Card style={{ marginBottom: 16 }}>
        <Steps current={currentStep} size="small" items={STEP_TITLES.map(t => ({ title: t }))} />
      </Card>
      <Card>
        {!selectedType && currentStep > 0 && <Alert type="warning" message="请选择导入类型" description="您跳过了类型选择步骤，请返回选择。" style={{ marginBottom: 16 }} />}
        {stepContent()}
      </Card>
      {currentStep < 2 && (
        <div style={{ marginTop: 16, display: "flex", justifyContent: "space-between" }}>
          <Button disabled={currentStep === 0} onClick={() => setCurrentStep(currentStep - 1)}>上一步</Button>
          <Button type="primary" disabled={(currentStep === 0 && !selectedType) || (currentStep === 1 && !uploadedJob)}
            onClick={() => { if (currentStep === 0 && selectedType) setCurrentStep(1); }}>
            下一步
          </Button>
        </div>
      )}
    </div>
  );
}
