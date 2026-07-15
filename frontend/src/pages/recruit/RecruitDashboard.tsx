import { useState } from "react";
import { Button, Card, DatePicker, Form, Input, InputNumber, Modal, Select, Space, Table, Tag, Typography, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useTranslation } from "react-i18next";
import { formatApiError } from "@/api/errors";
import { useCurrentStudy } from "@/hooks/useStudies";
import { useConvertCandidate, useCreateCandidate, useRecruitCandidates, useRecordPrescreen, useRejectCandidate, type CandidateDTO, type CandidateStatus } from "@/hooks/useRecruit";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Text, Title } = Typography;

const STATUS_COLORS: Record<CandidateStatus, string> = {
  NEW: "default",
  PRESCREENED: "processing",
  ELIGIBLE: "success",
  INELIGIBLE: "error",
  CONVERTED: "blue",
  REJECTED: "warning",
};

export default function RecruitDashboard() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const [status, setStatus] = useState<CandidateStatus | undefined>();
  const [createOpen, setCreateOpen] = useState(false);
  const [prescreenCandidate, setPrescreenCandidate] = useState<CandidateDTO | null>(null);
  const [convertCandidate, setConvertCandidate] = useState<CandidateDTO | null>(null);
  const [createForm] = Form.useForm();
  const [prescreenForm] = Form.useForm();
  const [convertForm] = Form.useForm();
  const { data: candidates = [], isLoading, refetch } = useRecruitCandidates(currentStudy?.id, status);
  const createCandidate = useCreateCandidate();
  const recordPrescreen = useRecordPrescreen();
  const rejectCandidate = useRejectCandidate();
  const convertCandidateMutation = useConvertCandidate();

  const submitCreate = async () => {
    if (!currentStudy?.id) return;
    try {
      const values = await createForm.validateFields();
      await createCandidate.mutateAsync({ ...values, studyId: currentStudy.id });
      message.success(t("recruit.created"));
      setCreateOpen(false);
      createForm.resetFields();
      await refetch();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("recruit.error.create")));
    }
  };

  const submitPrescreen = async () => {
    if (!prescreenCandidate) return;
    try {
      const values = await prescreenForm.validateFields();
      await recordPrescreen.mutateAsync({ candidateId: prescreenCandidate.id, request: values });
      message.success(t("recruit.prescreened"));
      setPrescreenCandidate(null);
      prescreenForm.resetFields();
      await refetch();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("recruit.error.prescreen")));
    }
  };

  const submitConvert = async () => {
    if (!convertCandidate) return;
    try {
      const values = await convertForm.validateFields();
      await convertCandidateMutation.mutateAsync({
        candidateId: convertCandidate.id,
        request: {
          ...values,
          dateOfBirth: values.dateOfBirth?.toISOString?.(),
          enrollmentDate: values.enrollmentDate?.toISOString?.(),
        },
      });
      message.success(t("recruit.converted"));
      setConvertCandidate(null);
      convertForm.resetFields();
      await refetch();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("recruit.error.convert")));
    }
  };

  const reject = async (candidate: CandidateDTO) => {
    try {
      await rejectCandidate.mutateAsync({ candidateId: candidate.id, reason: t("recruit.rejectedFromQueue") });
      message.success(t("recruit.rejected"));
      await refetch();
    } catch (err) {
      message.error(formatApiError(err, t("recruit.error.reject")));
    }
  };

  if (!currentStudy) {
    return <Text type="secondary">{t("recruit.selectStudy")}</Text>;
  }
  if (isLoading) {
    return <SkeletonPage />;
  }

  const columns: ColumnsType<CandidateDTO> = [
    { title: t("recruit.column.code"), dataIndex: "candidateCode", key: "candidateCode" },
    { title: t("recruit.column.name"), dataIndex: "displayName", key: "displayName", render: (v: string) => v || "-" },
    { title: t("recruit.column.source"), dataIndex: "source", key: "source", render: (v: string) => v || "-" },
    {
      title: t("recruit.column.status"),
      dataIndex: "status",
      key: "status",
      render: (value: CandidateStatus) => <Tag color={STATUS_COLORS[value]}>{t(`recruit.status.${value.toLowerCase()}`)}</Tag>,
    },
    {
      title: t("recruit.column.created"),
      dataIndex: "createdDate",
      key: "createdDate",
      render: (value: string) => new Date(value).toLocaleString(),
    },
    {
      title: t("recruit.column.actions"),
      key: "actions",
      render: (_, candidate) => (
        <Space wrap>
          <Button size="small" onClick={() => setPrescreenCandidate(candidate)} disabled={["CONVERTED", "REJECTED"].includes(candidate.status)}>
            {t("recruit.action.prescreen")}
          </Button>
          <Button size="small" type="primary" onClick={() => setConvertCandidate(candidate)} disabled={candidate.status !== "ELIGIBLE"}>
            {t("recruit.action.convert")}
          </Button>
          <Button size="small" danger onClick={() => reject(candidate)} disabled={["CONVERTED", "REJECTED"].includes(candidate.status)}>
            {t("recruit.action.reject")}
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <div>
          <Title level={4} style={{ margin: 0 }}>{t("recruit.title")}</Title>
          <Text type="secondary">{currentStudy.name} · {candidates.length} {t("recruit.candidates")}</Text>
        </div>
        <Space>
          <Select
            allowClear
            placeholder={t("recruit.filter.status")}
            value={status}
            style={{ width: 180 }}
            onChange={(value) => setStatus(value)}
            options={(["NEW", "PRESCREENED", "ELIGIBLE", "INELIGIBLE", "CONVERTED", "REJECTED"] as CandidateStatus[]).map((value) => ({
              value,
              label: t(`recruit.status.${value.toLowerCase()}`),
            }))}
          />
          <Button onClick={() => void refetch()}>{t("common.refresh")}</Button>
          <Button type="primary" onClick={() => setCreateOpen(true)}>{t("recruit.newCandidate")}</Button>
        </Space>
      </div>

      <Card styles={{ body: { padding: 0 } }}>
        <Table columns={columns} dataSource={candidates} rowKey="id" pagination={{ pageSize: 20 }} />
      </Card>

      <Modal title={t("recruit.modal.create")} open={createOpen} onOk={submitCreate} onCancel={() => setCreateOpen(false)} confirmLoading={createCandidate.isPending}>
        <Form form={createForm} layout="vertical">
          <Form.Item name="candidateCode" label={t("recruit.form.code")} rules={[{ required: true }]}>
            <Input maxLength={80} />
          </Form.Item>
          <Form.Item name="displayName" label={t("recruit.form.name")}>
            <Input maxLength={160} />
          </Form.Item>
          <Form.Item name="contactEmail" label={t("recruit.form.email")}>
            <Input maxLength={255} />
          </Form.Item>
          <Form.Item name="contactPhone" label={t("recruit.form.phone")}>
            <Input maxLength={80} />
          </Form.Item>
          <Form.Item name="source" label={t("recruit.form.source")}>
            <Input maxLength={120} />
          </Form.Item>
          <Form.Item name="notes" label={t("recruit.form.notes")}>
            <Input.TextArea rows={3} maxLength={2000} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={t("recruit.modal.prescreen")} open={!!prescreenCandidate} onOk={submitPrescreen} onCancel={() => setPrescreenCandidate(null)} confirmLoading={recordPrescreen.isPending}>
        <Form form={prescreenForm} layout="vertical">
          <Form.Item name="decision" label={t("recruit.form.decision")} rules={[{ required: true }]}>
            <Select options={(["ELIGIBLE", "INELIGIBLE", "NEEDS_REVIEW"] as const).map((value) => ({ value, label: t(`recruit.decision.${value.toLowerCase()}`) }))} />
          </Form.Item>
          <Form.Item name="score" label={t("recruit.form.score")}>
            <InputNumber min={0} max={100} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="criteriaSummary" label={t("recruit.form.criteria")}>
            <Input.TextArea rows={3} maxLength={2000} />
          </Form.Item>
          <Form.Item name="reviewNotes" label={t("recruit.form.reviewNotes")}>
            <Input.TextArea rows={3} maxLength={2000} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={t("recruit.modal.convert")} open={!!convertCandidate} onOk={submitConvert} onCancel={() => setConvertCandidate(null)} confirmLoading={convertCandidateMutation.isPending}>
        <Form form={convertForm} layout="vertical" initialValues={{ subjectUniqueIdentifier: convertCandidate?.candidateCode, studySubjectLabel: convertCandidate?.candidateCode }}>
          <Form.Item name="subjectUniqueIdentifier" label={t("recruit.form.subjectId")}>
            <Input maxLength={30} />
          </Form.Item>
          <Form.Item name="studySubjectLabel" label={t("recruit.form.studySubjectLabel")}>
            <Input maxLength={80} />
          </Form.Item>
          <Form.Item name="gender" label={t("recruit.form.gender")}>
            <Select allowClear options={[
              { value: "m", label: t("recruit.gender.male") },
              { value: "f", label: t("recruit.gender.female") },
              { value: "u", label: t("recruit.gender.unknown") },
            ]} />
          </Form.Item>
          <Form.Item name="dateOfBirth" label={t("recruit.form.dob")}>
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="enrollmentDate" label={t("recruit.form.enrollmentDate")}>
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
