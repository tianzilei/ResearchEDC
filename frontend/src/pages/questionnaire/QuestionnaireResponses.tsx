import { useState } from "react";
import {
  Card,
  Table,
  Button,
  Space,
  Typography,
  Tag,
  Modal,
  Descriptions,
  message,
  Empty,
  Input,
  Alert,
} from "antd";
import {
  FileTextOutlined,
  CheckCircleOutlined,
  LockOutlined,
  EyeOutlined,
} from "@ant-design/icons";
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

const statusColors: Record<string, string> = {
  draft: "default",
  submitted: "success",
  reviewed: "cyan",
  locked: "purple",
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
      message.success("Response reviewed");
    },
  });

  const lockResponse = useAppMutation<void, string>({
    mutationFn: (id) =>
      apiClient.post(`/api/v1/questionnaires/responses/${id}/lock`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-responses"] });
      message.success("Response locked");
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
      message.success("Correction applied");
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
      title: "Subject",
      dataIndex: "subject_id",
      key: "subject_id",
      render: (id: string) => (
        <Text code style={{ fontSize: 12 }}>
          {id.slice(0, 8)}...
        </Text>
      ),
    },
    {
      title: "Status",
      dataIndex: "status",
      key: "status",
      render: (s: string) => <Tag color={statusColors[s]}>{s}</Tag>,
    },
    {
      title: "Score",
      dataIndex: "total_score",
      key: "total_score",
      render: (s: number | null) =>
        s !== null ? <Text strong>{s}</Text> : "-",
    },
    {
      title: "Submitted",
      dataIndex: "submitted_at",
      key: "submitted_at",
      render: (d: string | null) =>
        d ? new Date(d).toLocaleString() : "-",
    },
    {
      title: "Actions",
      key: "actions",
      render: (_: unknown, r: Response) => (
        <Space>
          <Button
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(r)}
          >
            View
          </Button>
          {r.status === "submitted" && (
            <Button
              size="small"
              icon={<CheckCircleOutlined />}
              onClick={() =>
                reviewResponse.mutate({
                  id: r.id,
                  data: { status: "reviewed" },
                })
              }
            >
              Review
            </Button>
          )}
          {r.status === "reviewed" && (
            <Button
              size="small"
              icon={<LockOutlined />}
              onClick={() => lockResponse.mutate(r.id)}
            >
              Lock
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
          <FileTextOutlined /> Questionnaire Responses
        </Title>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={responses ?? []}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 20 }}
          locale={{ emptyText: <Empty description="No responses yet" /> }}
        />
      </Card>

      <Modal
        title="Response Detail"
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
                  Apply Correction
                </Button>,
              ]
            : null
        }
      >
        {detailResponse && (
          <>
            <Descriptions column={2} size="small" bordered>
              <Descriptions.Item label="Status">
                <Tag color={statusColors[detailResponse.status]}>
                  {detailResponse.status}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Total Score">
                {detailResponse.total_score ?? "-"}
              </Descriptions.Item>
              <Descriptions.Item label="Submitted">
                {detailResponse.submitted_at
                  ? new Date(detailResponse.submitted_at).toLocaleString()
                  : "-"}
              </Descriptions.Item>
              <Descriptions.Item label="Subject ID">
                {detailResponse.subject_id}
              </Descriptions.Item>
            </Descriptions>

            {detailResponse.score_json && (
              <Card
                title="Score Details"
                size="small"
                style={{ marginTop: 16 }}
              >
                <pre style={{ fontSize: 12, maxHeight: 200, overflow: "auto" }}>
                  {JSON.stringify(detailResponse.score_json, null, 2)}
                </pre>
              </Card>
            )}

            <Card title="Response Data" size="small" style={{ marginTop: 16 }}>
              <pre style={{ fontSize: 12, maxHeight: 300, overflow: "auto" }}>
                {JSON.stringify(detailResponse.raw_response_json, null, 2)}
              </pre>
            </Card>

            {detailResponse.status === "locked" && (
              <>
                <Alert
                  message="This response is locked. Corrections require a reason."
                  type="warning"
                  showIcon
                  style={{ marginTop: 16 }}
                />
                <TextArea
                  placeholder="Reason for correction (required)"
                  value={correctionReason}
                  onChange={(e) => setCorrectionReason(e.target.value)}
                  rows={2}
                  style={{ marginTop: 8 }}
                />
                <TextArea
                  placeholder="Correction data (JSON)"
                  value={correctionData}
                  onChange={(e) => setCorrectionData(e.target.value)}
                  rows={6}
                  style={{
                    marginTop: 8,
                    fontFamily: "monospace",
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
