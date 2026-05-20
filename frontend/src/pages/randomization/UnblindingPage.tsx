import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, Table, Tag, Button, Modal, Input, message, Space, Select, Empty, InputNumber } from "antd";
import { ArrowLeftOutlined, EyeOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import { useUnblindingRequests, useRequestUnblinding } from "@/hooks/useRandomization";
import { SkeletonPage } from "@/components/SkeletonCard";
import { apiClient } from "@/api/client";
import { Divider } from "antd";
import type { UnblindingRequestDTO, UnblindingStatus } from "@/types/randomization";

const { TextArea } = Input;

export default function UnblindingPage() {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const schemeId = Number(id);
  const { data: requests, isLoading, refetch } = useUnblindingRequests(schemeId);
  const requestUnblinding = useRequestUnblinding();
  const navigate = useNavigate();
  const [requestModalVisible, setRequestModalVisible] = useState(false);
  const [reviewTarget, setReviewTarget] = useState<UnblindingRequestDTO | null>(null);
  const [assignmentId, setAssignmentId] = useState<number | undefined>();
  const [reason, setReason] = useState("");
  const [reviewDecision, setReviewDecision] = useState<UnblindingStatus | null>(null);
  const [reviewNotes, setReviewNotes] = useState("");

  const handleRequest = async () => {
    if (!assignmentId) {
      message.error(t("unbliding.enterAssignmentId"));
      return;
    }
    try {
      await requestUnblinding.mutateAsync({ assignmentId, reason });
      message.success(t("unblinding.submitted"));
      setRequestModalVisible(false);
      setAssignmentId(undefined);
      setReason("");
      refetch();
    } catch (e: any) {
      message.error(e?.message ?? t("unblinding.failed"));
    }
  };

  const handleReview = async () => {
    if (!reviewTarget || !reviewDecision) return;
    try {
      const url = `/api/v1/randomization/unblinding/${reviewTarget.id}/review`
        + `?decision=${reviewDecision}&reviewedBy=0`
        + (reviewNotes ? `&reviewNotes=${encodeURIComponent(reviewNotes)}` : "");
      await apiClient.post(url);
      message.success(`Request ${reviewDecision.toLowerCase()}`);
      setReviewTarget(null);
      setReviewDecision(null);
      setReviewNotes("");
      refetch();
    } catch (e: any) {
      message.error(e?.message ?? "Failed");
    }
  };

  if (isLoading) return <SkeletonPage />;

  const statusColors: Record<string, string> = {
    PENDING: "warning", APPROVED: "success", REJECTED: "error",
  };

  const columns = [
    { title: t("unblinding.column.id"), dataIndex: "id", key: "id" },
    { title: t("unblinding.column.assignment"), dataIndex: "assignmentId", key: "assignmentId" },
    { title: t("unblinding.column.arm"), dataIndex: "armName", key: "armName",
      render: (name: string) => name ? <Tag color="blue">{name}</Tag> : "-",
    },
    { title: t("unblinding.column.reason"), dataIndex: "reason", key: "reason",
      render: (r: string) => r?.substring(0, 50) ?? "-",
    },
    { title: t("unblinding.column.status"), dataIndex: "status", key: "status",
      render: (s: string) => <Tag color={statusColors[s]}>{s}</Tag>,
    },
    { title: t("unblinding.column.requested"), dataIndex: "requestedDate", key: "requestedDate",
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
    {
      title: t("unblinding.column.actions"), key: "actions",
      render: (_: any, record: UnblindingRequestDTO) => (
        <Space>
          {record.status === "PENDING" && (
            <Button size="small" type="primary" onClick={() => setReviewTarget(record)}>
              {t("unblinding.review")}
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(`/app/randomization/schemes/${schemeId}`)}>Back</Button>
      </Space>

      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <span style={{ fontSize: 18, fontWeight: 600 }}><EyeOutlined /> {t("unblinding.title")}</span>
        <Button onClick={() => setRequestModalVisible(true)}>{t("unblinding.request")}</Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={requests ?? []}
          columns={columns}
          rowKey="id"
          pagination={false}
          size="small"
          locale={{ emptyText: <Empty description={t("unblinding.empty")} /> }}
        />
      </Card>

      <Modal title={t("unblinding.modalTitle")} open={requestModalVisible}
             onOk={handleRequest} onCancel={() => setRequestModalVisible(false)}>
        <Space direction="vertical" style={{ width: "100%" }}>
          <label>{t("unblinding.assignmentId")}</label>
          <InputNumber min={1} value={assignmentId}
                       onChange={(value) => setAssignmentId(value ?? undefined)}
                       style={{ width: "100%" }} />
          <label>{t("unblinding.reason")}</label>
          <TextArea rows={3} value={reason} onChange={(e) => setReason(e.target.value)} />
        </Space>
      </Modal>

      <Modal
        title={t("unblinding.reviewTitle")}
        open={!!reviewTarget}
        onCancel={() => setReviewTarget(null)}
        footer={null}
      >
        {reviewTarget && (
          <Space direction="vertical" style={{ width: "100%" }}>
            <p><strong>Assignment ID:</strong> {reviewTarget.assignmentId}</p>
            <p><strong>Arm:</strong> {reviewTarget.armName}</p>
            <p><strong>Reason:</strong> {reviewTarget.reason}</p>
            <Divider />
            <label>{t("unblinding.decision")}</label>
            <Select value={reviewDecision} onChange={setReviewDecision} style={{ width: "100%" }}>
              <Select.Option value="APPROVED">{t("unblinding.approve")}</Select.Option>
              <Select.Option value="REJECTED">{t("unblinding.reject")}</Select.Option>
            </Select>
            <label>{t("unblinding.reviewNotes")}</label>
            <TextArea rows={3} value={reviewNotes} onChange={(e) => setReviewNotes(e.target.value)} />
            <Button type="primary" onClick={handleReview} disabled={!reviewDecision}>
              {t("unblinding.submitReview")}
            </Button>
          </Space>
        )}
      </Modal>
    </div>
  );
}
