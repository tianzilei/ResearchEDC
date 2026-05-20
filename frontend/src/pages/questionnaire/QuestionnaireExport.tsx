import { useState } from "react";
import { useTranslation } from "react-i18next";
import {
  Card,
  Table,
  Button,
  Space,
  Typography,
  Tag,
  Select,
  message,
  Empty,
  Modal,
  Checkbox,
} from "antd";
import {
  ExportOutlined,
  DownloadOutlined,
  ReloadOutlined,
} from "@ant-design/icons";
import { useCurrentStudy } from "@/hooks/useStudies";
import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface ExportJob {
  id: string;
  study_id: string;
  requested_by: string;
  status: string;
  export_type: string;
  export_format: string;
  query_params: Record<string, unknown>;
  file_path: string | null;
  error_message: string | null;
  created_at: string;
  started_at: string | null;
  finished_at: string | null;
}

const statusColors: Record<string, string> = {
  pending: "default",
  running: "processing",
  completed: "success",
  failed: "error",
};

const FORMAT_OPTIONS = [
  { value: "csv", label: "CSV" },
  { value: "xlsx", label: "Excel (XLSX)" },
  { value: "json", label: "JSON" },
];

const LAYOUT_OPTIONS = [
  { value: "wide" },
  { value: "long" },
  { value: "score" },
];

function useExportJobs(studyId: number) {
  return useAppQuery<ExportJob[]>({
    queryKey: ["questionnaire-export-jobs", studyId],
    queryFn: () =>
      apiClient.get<ExportJob[]>("/api/v1/questionnaires/export"),
    enabled: true,
  });
}

export default function QuestionnaireExport() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id ?? 0;
  const qc = useQueryClient();
  const { data: jobs, isLoading } = useExportJobs(studyId);
  const [exportOpen, setExportOpen] = useState(false);
  const [exportFormat, setExportFormat] = useState("xlsx");
  const [layout, setLayout] = useState("wide");
  const [includeScores, setIncludeScores] = useState(true);

  const createExport = useAppMutation<ExportJob, any>({
    mutationFn: (body) =>
      apiClient.post<ExportJob>("/api/v1/questionnaires/export", body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-export-jobs"] });
      message.success(t("qexport.created"));
      setExportOpen(false);
    },
  });

  if (isLoading) return <SkeletonPage />;

  const columns = [
    {
      title: t("qexport.column.format"),
      dataIndex: "export_format",
      key: "export_format",
      render: (f: string) => <Tag>{f.toUpperCase()}</Tag>,
    },
    {
      title: t("qexport.column.layout"),
      dataIndex: "export_type",
      key: "export_type",
      render: (t: string) => <Tag color="blue">{t}</Tag>,
    },
    {
      title: t("qexport.column.status"),
      dataIndex: "status",
      key: "status",
      render: (s: string) => <Tag color={statusColors[s]}>{s}</Tag>,
    },
    {
      title: t("qexport.column.requested"),
      dataIndex: "created_at",
      key: "created_at",
      render: (d: string) => new Date(d).toLocaleString(),
    },
    {
      title: t("qexport.column.completed"),
      dataIndex: "finished_at",
      key: "finished_at",
      render: (d: string | null) =>
        d ? new Date(d).toLocaleString() : "-",
    },
    {
      title: t("qexport.column.actions"),
      key: "actions",
      render: (_: unknown, r: ExportJob) => (
        <Space>
          {r.status === "completed" && r.file_path && (
            <Button size="small" icon={<DownloadOutlined />} type="link">
              {t("qexport.action.download")}
            </Button>
          )}
          {r.status === "failed" && (
            <Button
              size="small"
              icon={<ReloadOutlined />}
              onClick={() => {
                createExport.mutate(r.query_params);
              }}
            >
              {t("qexport.action.retry")}
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <Title level={4} style={{ marginTop: 0 }}>
          <ExportOutlined /> {t("qexport.title")}
        </Title>
        <Button
          type="primary"
          icon={<ExportOutlined />}
          onClick={() => setExportOpen(true)}
        >
          {t("qexport.new")}
        </Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={jobs ?? []}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: <Empty description={t("qexport.empty")} /> }}
        />
      </Card>

      <Modal
        title={t("qexport.create")}
        open={exportOpen}
        onCancel={() => setExportOpen(false)}
        onOk={() =>
          createExport.mutate({
            study_id: studyId,
            questionnaire_codes: [],
            export_format: exportFormat,
            layout,
            include_scores: includeScores,
            include_raw: true,
          })
        }
        confirmLoading={createExport.isPending}
      >
        <Space direction="vertical" style={{ width: "100%" }}>
          <div>
            <Text strong>{t("qexport.format")}</Text>
            <Select
              value={exportFormat}
              onChange={setExportFormat}
              style={{ width: "100%", marginTop: 4 }}
            >
              {FORMAT_OPTIONS.map((o) => (
                <Select.Option key={o.value} value={o.value}>
                  {o.label}
                </Select.Option>
              ))}
            </Select>
          </div>
          <div>
            <Text strong>{t("qexport.layout")}</Text>
            <Select
              value={layout}
              onChange={setLayout}
              style={{ width: "100%", marginTop: 4 }}
            >
              {LAYOUT_OPTIONS.map((o) => (
                <Select.Option key={o.value} value={o.value}>
                  {t("qexport.layout." + o.value)}
                </Select.Option>
              ))}
            </Select>
          </div>
          <div>
            <Checkbox
              checked={includeScores}
              onChange={(e) => setIncludeScores(e.target.checked)}
            >
                {t("qexport.includeScores")}
            </Checkbox>
          </div>
        </Space>
      </Modal>
    </div>
  );
}
