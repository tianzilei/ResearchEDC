import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, Table, Tag, Button, Modal, Input, message, Space, Select, Empty, InputNumber } from "antd";
import { ArrowLeftOutlined, EyeOutlined } from "@ant-design/icons";
import { useUnblindingRequests, useRequestUnblinding } from "@/hooks/useRandomization";
import { SkeletonPage } from "@/components/SkeletonCard";
import { apiClient } from "@/api/client";
import { Divider } from "antd";
import type { UnblindingRequestDTO, UnblindingStatus } from "@/types/randomization";

const { TextArea } = Input;

export default function UnblindingPage() {
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
      message.error("Please enter an assignment ID");
      return;
    }
    try {
      await requestUnblinding.mutateAsync({ assignmentId, reason });
      message.success("Unblinding request submitted");
      setRequestModalVisible(false);
      setAssignmentId(undefined);
      setReason("");
      refetch();
    } catch (e: any) {
      message.error(e?.message ?? "Failed");
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
    { title: "ID", dataIndex: "id", key: "id" },
    { title: "Subject Assignment", dataIndex: "assignmentId", key: "assignmentId" },
    { title: "Arm", dataIndex: "armName", key: "armName",
      render: (name: string) => name ? <Tag color="blue">{name}</Tag> : "-",
    },
    { title: "Reason", dataIndex: "reason", key: "reason",
      render: (r: string) => r?.substring(0, 50) ?? "-",
    },
    { title: "Status", dataIndex: "status", key: "status",
      render: (s: string) => <Tag color={statusColors[s]}>{s}</Tag>,
    },
    { title: "Requested", dataIndex: "requestedDate", key: "requestedDate",
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
    {
      title: "Actions", key: "actions",
      render: (_: any, record: UnblindingRequestDTO) => (
        <Space>
          {record.status === "PENDING" && (
            <Button size="small" type="primary" onClick={() => setReviewTarget(record)}>
              Review
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
        <span style={{ fontSize: 18, fontWeight: 600 }}><EyeOutlined /> Unblinding Requests</span>
        <Button onClick={() => setRequestModalVisible(true)}>Request Unblinding</Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={requests ?? []}
          columns={columns}
          rowKey="id"
          pagination={false}
          size="small"
          locale={{ emptyText: <Empty description="No unblinding requests" /> }}
        />
      </Card>

      <Modal title="Request Unblinding" open={requestModalVisible}
             onOk={handleRequest} onCancel={() => setRequestModalVisible(false)}>
        <Space direction="vertical" style={{ width: "100%" }}>
          <label>Assignment ID</label>
          <InputNumber min={1} value={assignmentId}
                       onChange={(value) => setAssignmentId(value ?? undefined)}
                       style={{ width: "100%" }} />
          <label>Reason</label>
          <TextArea rows={3} value={reason} onChange={(e) => setReason(e.target.value)} />
        </Space>
      </Modal>

      <Modal
        title="Review Unblinding Request"
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
            <label>Decision</label>
            <Select value={reviewDecision} onChange={setReviewDecision} style={{ width: "100%" }}>
              <Select.Option value="APPROVED">Approve</Select.Option>
              <Select.Option value="REJECTED">Reject</Select.Option>
            </Select>
            <label>Review Notes</label>
            <TextArea rows={3} value={reviewNotes} onChange={(e) => setReviewNotes(e.target.value)} />
            <Button type="primary" onClick={handleReview} disabled={!reviewDecision}>
              Submit Review
            </Button>
          </Space>
        )}
      </Modal>
    </div>
  );
}
