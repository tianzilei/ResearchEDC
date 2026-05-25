import { useState } from "react";
import { useTranslation } from "react-i18next";
import {
  Card,
  Table,
  Button,
  Space,
  Typography,
  Modal,
  Descriptions,
  message,
  Empty,
  Input,
  Alert,
} from "antd";

import { useCurrentStudy } from "@/hooks/useStudies";
import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;
const { TextArea } = Input;

interface Response {
  id: string;
  assignment_id: string;
  subject_id: string;
  visit_id: string | null;
  questionnaire_version_id: string;
  status: string;
  started_at: string | null;
  submitted_at: string | null;
  raw_response_json: Record<string, unknown>;
  score_json: Record<string, unknown> | null;
  total_score: number | null;
  device_info: Record<string, unknown> | null;
  created_at: string;
}

const statusClassMap: Record<string, string> = {
  draft: "default",
  submitted: "success",
  reviewed: "info",
  locked: "info",
};

function useResponses(studyId: number) {
  return useAppQuery<Response[]>({
    queryKey: ["questionnaire-responses", studyId],
    queryFn: () =>
      apiClient.get<Response[]>("/api/v1/questionnaires/responses"),
    enabled: true,
  });
}

export default function QuestionnaireResponses() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id ?? 0;
  const qc = useQueryClient();
  const { data: responses, isLoading } = useResponses(studyId);
  const [detailResponse, setDetailResponse] = useState<Response | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [correctionReason, setCorrectionReason] = useState("");
  const [correctionData, setCorrectionData] = useState("");

  const reviewResponse = useAppMutation<void, { id: string; data: any }>({
    mutationFn: ({ id, data }) =>
      apiClient.post(`/api/v1/questionnaires/responses/${id}/review`, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-responses"] });
      message.success(t("response.reviewed"));
    },
  });

  const lockResponse = useAppMutation<void, string>({
    mutationFn: (id) =>
      apiClient.post(`/api/v1/questionnaires/responses/${id}/lock`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-responses"] });
      message.success(t("response.locked"));
    },
  });

  const correctResponse = useAppMutation<void, { id: string; data: any }>({
    mutationFn: ({ id, data }) =>
      apiClient.post(
        `/api/v1/questionnaires/responses/${id}/correction`,
        data,
      ),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-responses"] });
      message.success(t("response.correctionApplied"));
      setCorrectionReason("");
      setCorrectionData("");
      setDetailOpen(false);
    },
  });

  if (isLoading) return <SkeletonPage />;

  const handleViewDetail = (r: Response) => {
    setDetailResponse(r);
    setCorrectionData(JSON.stringify(r.raw_response_json, null, 2));
    setDetailOpen(true);
  };

  const columns = [
    {
      title: t("response.column.subject"),
      dataIndex: "subject_id",
      key: "subject_id",
      render: (id: string) => (
        <Text code style={{ fontSize: 12 }}>
          {id.slice(0, 8)}...
        </Text>
      ),
    },
    {
      title: t("response.column.status"),
      dataIndex: "status",
      key: "status",
      render: (s: string) => <span className={`status status-${statusClassMap[s] ?? "default"}`}>{s}</span>,
    },
    {
      title: t("response.column.score"),
      dataIndex: "total_score",
      key: "total_score",
      render: (s: number | null) =>
        s !== null ? <Text strong>{s}</Text> : "-",
    },
    {
      title: t("response.column.submitted"),
      dataIndex: "submitted_at",
      key: "submitted_at",
      render: (d: string | null) =>
        d ? new Date(d).toLocaleString() : "-",
    },
    {
      title: t("response.column.actions"),
      key: "actions",
      render: (_: unknown, r: Response) => (
        <Space>
          <Button
            size="small"
            onClick={() => handleViewDetail(r)}
          >
            {t("response.action.view")}
          </Button>
          {r.status === "submitted" && (
            <Button
              size="small"
              onClick={() =>
                reviewResponse.mutate({
                  id: r.id,
                  data: { status: "reviewed" },
                })
              }
            >
              {t("response.action.review")}
            </Button>
          )}
          {r.status === "reviewed" && (
            <Button
              size="small"
              onClick={() => lockResponse.mutate(r.id)}
            >
              {t("response.action.lock")}
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
          {t("response.title")}
        </Title>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={responses ?? []}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 20 }}
          locale={{ emptyText: <Empty description={t("response.empty")} /> }}
        />
      </Card>

      <Modal
        title={t("response.detail")}
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        width={800}
        footer={
          detailResponse?.status === "locked"
            ? [
                <Button
                  key="correct"
                  type="primary"
                  disabled={!correctionReason}
                  onClick={() =>
                    correctResponse.mutate({
                      id: detailResponse.id,
                      data: {
                        correction_data: JSON.parse(correctionData || "{}"),
                        reason: correctionReason,
                      },
                    })
                  }
                >
                  {t("response.action.applyCorrection")}
                </Button>,
              ]
            : null
        }
      >
        {detailResponse && (
          <>
            <Descriptions column={2} size="small" bordered>
              <Descriptions.Item label={t("response.detail.status")}>
                <span className={`status status-${statusClassMap[detailResponse.status] ?? "default"}`}>
                  {detailResponse.status}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label={t("response.detail.totalScore")}>
                {detailResponse.total_score ?? "-"}
              </Descriptions.Item>
              <Descriptions.Item label={t("response.detail.submitted")}>
                {detailResponse.submitted_at
                  ? new Date(detailResponse.submitted_at).toLocaleString()
                  : "-"}
              </Descriptions.Item>
              <Descriptions.Item label={t("response.detail.subjectId")}>
                {detailResponse.subject_id}
              </Descriptions.Item>
            </Descriptions>

            {detailResponse.score_json && (
              <Card
                title={t("response.detail.scoreDetails")}
                size="small"
                style={{ marginTop: 16 }}
              >
                <pre style={{ fontSize: 12, maxHeight: 200, overflow: "auto" }}>
                  {JSON.stringify(detailResponse.score_json, null, 2)}
                </pre>
              </Card>
            )}

            <Card title={t("response.detail.responseData")} size="small" style={{ marginTop: 16 }}>
              <pre style={{ fontSize: 12, maxHeight: 300, overflow: "auto" }}>
                {JSON.stringify(detailResponse.raw_response_json, null, 2)}
              </pre>
            </Card>

            {detailResponse.status === "locked" && (
              <>
                <Alert
                  message={t("response.lockedAlert")}
                  type="warning"
                  showIcon
                  style={{ marginTop: 16 }}
                />
                <TextArea
                  placeholder={t("response.correctionReasonPlaceholder")}
                  value={correctionReason}
                  onChange={(e) => setCorrectionReason(e.target.value)}
                  rows={2}
                  style={{ marginTop: 8 }}
                />
                <TextArea
                  placeholder={t("response.correctionDataPlaceholder")}
                  value={correctionData}
                  onChange={(e) => setCorrectionData(e.target.value)}
                  rows={6}
                  style={{
                    marginTop: 8,
                    fontFamily: "var(--font-mono)",
                    fontSize: 12,
                  }}
                />
              </>
            )}
          </>
        )}
      </Modal>
    </div>
  );
}
