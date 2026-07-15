import { useState } from "react";
import { Button, Card, Form, Input, InputNumber, Modal, Select, Space, Table, Tag, Typography, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useTranslation } from "react-i18next";
import { formatApiError } from "@/api/errors";
import { useCurrentStudy } from "@/hooks/useStudies";
import { useCreateSdvReview, useSdvReviews, useUpdateSdvReview, type SdvReviewDTO, type SdvStatus } from "@/hooks/useSdv";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Text, Title } = Typography;

const STATUS_COLORS: Record<SdvStatus, string> = {
  PENDING: "default",
  VERIFIED: "success",
  REQUIRES_CHANGES: "error",
};

export default function SdvDashboard() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const [status, setStatus] = useState<SdvStatus | undefined>();
  const [queueOpen, setQueueOpen] = useState(false);
  const [reviewing, setReviewing] = useState<SdvReviewDTO | null>(null);
  const [queueForm] = Form.useForm<{ eventCrfId: number }>();
  const [reviewForm] = Form.useForm<{ status: SdvStatus; reviewNotes?: string }>();
  const { data: reviews = [], isLoading, refetch } = useSdvReviews(currentStudy?.id, status);
  const createReview = useCreateSdvReview();
  const updateReview = useUpdateSdvReview();

  const submitQueue = async () => {
    try {
      const values = await queueForm.validateFields();
      await createReview.mutateAsync(values);
      message.success(t("sdv.queued"));
      setQueueOpen(false);
      queueForm.resetFields();
      await refetch();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("sdv.error.queue")));
    }
  };

  const submitReview = async () => {
    if (!reviewing) return;
    try {
      const values = await reviewForm.validateFields();
      await updateReview.mutateAsync({ reviewId: reviewing.id, request: values });
      message.success(t("sdv.updated"));
      setReviewing(null);
      reviewForm.resetFields();
      await refetch();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("sdv.error.update")));
    }
  };

  if (!currentStudy) {
    return <Text type="secondary">{t("sdv.selectStudy")}</Text>;
  }
  if (isLoading) {
    return <SkeletonPage />;
  }

  const columns: ColumnsType<SdvReviewDTO> = [
    { title: t("sdv.column.eventCrf"), dataIndex: "eventCrfId", key: "eventCrfId" },
    { title: t("sdv.column.studySubject"), dataIndex: "studySubjectId", key: "studySubjectId", render: (v: number | null) => v ?? "-" },
    {
      title: t("sdv.column.status"),
      dataIndex: "status",
      key: "status",
      render: (value: SdvStatus) => <Tag color={STATUS_COLORS[value]}>{t(`sdv.status.${value.toLowerCase()}`)}</Tag>,
    },
    { title: t("sdv.column.reviewed"), dataIndex: "reviewedDate", key: "reviewedDate", render: (v: string | null) => v ? new Date(v).toLocaleString() : "-" },
    {
      title: t("sdv.column.actions"),
      key: "actions",
      render: (_, review) => (
        <Button size="small" type="primary" onClick={() => {
          setReviewing(review);
          reviewForm.setFieldsValue({ status: review.status, reviewNotes: review.reviewNotes ?? "" });
        }}>
          {t("sdv.action.review")}
        </Button>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <div>
          <Title level={4} style={{ margin: 0 }}>{t("sdv.title")}</Title>
          <Text type="secondary">{currentStudy.name} · {reviews.length} {t("sdv.reviews")}</Text>
        </div>
        <Space>
          <Select
            allowClear
            placeholder={t("sdv.filter.status")}
            value={status}
            style={{ width: 190 }}
            onChange={(value) => setStatus(value)}
            options={(["PENDING", "VERIFIED", "REQUIRES_CHANGES"] as SdvStatus[]).map((value) => ({
              value,
              label: t(`sdv.status.${value.toLowerCase()}`),
            }))}
          />
          <Button onClick={() => void refetch()}>{t("common.refresh")}</Button>
          <Button type="primary" onClick={() => setQueueOpen(true)}>{t("sdv.queueReview")}</Button>
        </Space>
      </div>

      <Card styles={{ body: { padding: 0 } }}>
        <Table columns={columns} dataSource={reviews} rowKey="id" pagination={{ pageSize: 20 }} />
      </Card>

      <Modal title={t("sdv.modal.queue")} open={queueOpen} onOk={submitQueue} onCancel={() => setQueueOpen(false)} confirmLoading={createReview.isPending}>
        <Form form={queueForm} layout="vertical">
          <Form.Item name="eventCrfId" label={t("sdv.form.eventCrfId")} rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={t("sdv.modal.review")} open={!!reviewing} onOk={submitReview} onCancel={() => setReviewing(null)} confirmLoading={updateReview.isPending}>
        <Form form={reviewForm} layout="vertical">
          <Form.Item name="status" label={t("sdv.form.status")} rules={[{ required: true }]}>
            <Select options={(["VERIFIED", "REQUIRES_CHANGES", "PENDING"] as SdvStatus[]).map((value) => ({ value, label: t(`sdv.status.${value.toLowerCase()}`) }))} />
          </Form.Item>
          <Form.Item name="reviewNotes" label={t("sdv.form.notes")}>
            <Input.TextArea rows={4} maxLength={2000} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
