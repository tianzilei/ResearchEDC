import { useMemo, useState } from "react";
import { Alert, Button, Card, DatePicker, Empty, Form, Input, InputNumber, Modal, Space, Statistic, Table, Tag, Typography, message } from "antd";
import { CheckCircleOutlined, CloseCircleOutlined, LinkOutlined, PlusOutlined } from "@ant-design/icons";
import dayjs, { type Dayjs } from "dayjs";
import { useTranslation } from "react-i18next";
import { formatApiError } from "@/api/errors";
import { useCurrentStudy } from "@/hooks/useStudies";
import {
  useCancelEcoaAssignment,
  useCompleteEcoaAssignment,
  useCreateEcoaSchedule,
  useEcoaAdherence,
  useEcoaAssignments,
  useEcoaSchedules,
  type CreateEcoaScheduleRequest,
  type EcoaAssignmentDTO,
  type EcoaScheduleResultDTO,
} from "@/hooks/useEcoa";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface ScheduleFormValues {
  studySubjectId: number;
  studyEventId?: number;
  questionnaireVersionId: string;
  title: string;
  description?: string;
  dueAt: Dayjs;
  windowOpensAt?: Dayjs;
  windowClosesAt?: Dayjs;
}

const STATUS_COLORS: Record<string, string> = {
  PENDING: "default",
  IN_PROGRESS: "processing",
  SUBMITTED: "success",
  REVIEWED: "blue",
  OVERDUE: "error",
  CANCELLED: "warning",
};

function toIso(value?: Dayjs) {
  return value ? value.toISOString() : undefined;
}

export default function EcoaDashboard() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id;
  const [createOpen, setCreateOpen] = useState(false);
  const [issued, setIssued] = useState<EcoaScheduleResultDTO | null>(null);
  const [form] = Form.useForm<ScheduleFormValues>();

  const { data: schedules = [], isLoading: schedulesLoading } = useEcoaSchedules(studyId);
  const { data: assignments = [], isLoading: assignmentsLoading } = useEcoaAssignments(studyId);
  const { data: adherence } = useEcoaAdherence(studyId);
  const createSchedule = useCreateEcoaSchedule(studyId);
  const completeAssignment = useCompleteEcoaAssignment(studyId);
  const cancelAssignment = useCancelEcoaAssignment(studyId);

  const sortedAssignments = useMemo(
    () => [...assignments].sort((a, b) => new Date(a.dueAt).getTime() - new Date(b.dueAt).getTime()),
    [assignments],
  );

  if (!currentStudy) {
    return <Alert type="info" message={t("ecoa.selectStudy")} />;
  }
  if (schedulesLoading || assignmentsLoading) {
    return <SkeletonPage />;
  }

  const submitSchedule = async () => {
    try {
      const values = await form.validateFields();
      if (!studyId) return;
      const request: CreateEcoaScheduleRequest = {
        studyId,
        studySubjectId: values.studySubjectId,
        studyEventId: values.studyEventId,
        questionnaireVersionId: values.questionnaireVersionId,
        title: values.title,
        description: values.description,
        dueAt: values.dueAt.toISOString(),
        windowOpensAt: toIso(values.windowOpensAt),
        windowClosesAt: toIso(values.windowClosesAt),
      };
      const result = await createSchedule.mutateAsync(request);
      setIssued(result);
      message.success(t("ecoa.created"));
      form.resetFields();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("ecoa.error.create")));
    }
  };

  const assignmentColumns = [
    { title: t("ecoa.column.title"), dataIndex: "scheduleId", key: "scheduleId", width: 120,
      render: (_: number, record: EcoaAssignmentDTO) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.questionnaireAssignmentId ?? `Schedule ${record.scheduleId}`}</Text>
          <Text type="secondary">SS {record.studySubjectId}</Text>
        </Space>
      ),
    },
    { title: t("ecoa.column.status"), dataIndex: "status", key: "status", width: 140,
      render: (value: string) => <Tag color={STATUS_COLORS[value] ?? "default"}>{t(`ecoa.status.${value.toLowerCase()}`)}</Tag>,
    },
    { title: t("ecoa.column.due"), dataIndex: "dueAt", key: "dueAt", width: 190,
      render: (value: string) => new Date(value).toLocaleString(),
    },
    { title: t("ecoa.column.window"), key: "window", width: 250,
      render: (_: unknown, record: EcoaAssignmentDTO) => (
        <Text type="secondary">
          {record.windowOpensAt ? new Date(record.windowOpensAt).toLocaleDateString() : "-"} / {record.windowClosesAt ? new Date(record.windowClosesAt).toLocaleDateString() : "-"}
        </Text>
      ),
    },
    { title: t("ecoa.column.completed"), dataIndex: "completedAt", key: "completedAt", width: 190,
      render: (value: string | null) => value ? new Date(value).toLocaleString() : "-",
    },
    { title: t("ecoa.column.link"), dataIndex: "entryUrl", key: "entryUrl", width: 120,
      render: (value: string | null) => value ? (
        <Button size="small" icon={<LinkOutlined />} onClick={() => navigator.clipboard.writeText(`${window.location.origin}${value}`)}>
          {t("ecoa.copy")}
        </Button>
      ) : "-",
    },
    { title: t("ecoa.column.actions"), key: "actions", width: 220,
      render: (_: unknown, record: EcoaAssignmentDTO) => {
        const terminal = ["SUBMITTED", "REVIEWED", "CANCELLED"].includes(record.status);
        return (
          <Space>
            <Button
              size="small"
              icon={<CheckCircleOutlined />}
              disabled={terminal}
              loading={completeAssignment.isPending}
              onClick={() => completeAssignment.mutate({ assignmentId: record.id })}
            >
              {t("ecoa.complete")}
            </Button>
            <Button
              size="small"
              danger
              icon={<CloseCircleOutlined />}
              disabled={terminal}
              loading={cancelAssignment.isPending}
              onClick={() => cancelAssignment.mutate(record.id)}
            >
              {t("ecoa.cancel")}
            </Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }} align="start">
        <div>
          <Title level={4} style={{ marginTop: 0 }}>{t("ecoa.title")}</Title>
          <Text type="secondary">{currentStudy.name}</Text>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            setIssued(null);
            setCreateOpen(true);
          }}
        >
          {t("ecoa.newSchedule")}
        </Button>
      </Space>

      <Space style={{ width: "100%", marginTop: 16 }} size="middle" wrap>
        <Card style={{ minWidth: 160 }}><Statistic title={t("ecoa.metric.total")} value={adherence?.total ?? 0} /></Card>
        <Card style={{ minWidth: 160 }}><Statistic title={t("ecoa.metric.pending")} value={adherence?.pending ?? 0} /></Card>
        <Card style={{ minWidth: 160 }}><Statistic title={t("ecoa.metric.completed")} value={adherence?.completed ?? 0} /></Card>
        <Card style={{ minWidth: 160 }}><Statistic title={t("ecoa.metric.overdue")} value={adherence?.overdue ?? 0} valueStyle={{ color: "var(--ant-color-error)" }} /></Card>
        <Card style={{ minWidth: 180 }}><Statistic title={t("ecoa.metric.completionRate")} value={adherence?.completionRate ?? 0} suffix="%" precision={1} /></Card>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={sortedAssignments}
          columns={assignmentColumns}
          rowKey="id"
          pagination={{ pageSize: 20 }}
          locale={{ emptyText: <Empty description={t("ecoa.empty")} /> }}
        />
      </Card>

      <Card style={{ marginTop: 16 }} title={t("ecoa.schedules")}>
        <Table
          dataSource={schedules}
          rowKey="id"
          size="small"
          pagination={{ pageSize: 8 }}
          columns={[
            { title: t("ecoa.column.title"), dataIndex: "title", key: "title" },
            { title: t("ecoa.column.studySubject"), dataIndex: "studySubjectId", key: "studySubjectId" },
            { title: t("ecoa.column.version"), dataIndex: "questionnaireVersionId", key: "questionnaireVersionId" },
            { title: t("ecoa.column.due"), dataIndex: "dueAt", key: "dueAt", render: (value: string) => new Date(value).toLocaleString() },
          ]}
        />
      </Card>

      <Modal
        width={760}
        title={t("ecoa.modal.title")}
        open={createOpen}
        onCancel={() => {
          setCreateOpen(false);
          setIssued(null);
          form.resetFields();
        }}
        onOk={issued ? () => setCreateOpen(false) : submitSchedule}
        okText={issued ? t("ecoa.close") : t("ecoa.create")}
        confirmLoading={createSchedule.isPending}
      >
        {issued && (
          <Alert
            type="success"
            showIcon
            style={{ marginBottom: 16 }}
            message={t("ecoa.linkIssued")}
            description={`${window.location.origin}${issued.participantEntryUrl}`}
          />
        )}
        {!issued && (
          <Form form={form} layout="vertical" initialValues={{ dueAt: dayjs().add(7, "day") }}>
            <Form.Item
              name="studySubjectId"
              label={t("ecoa.form.studySubjectId")}
              rules={[{ required: true, message: t("ecoa.form.studySubjectRequired") }]}
            >
              <InputNumber min={1} style={{ width: "100%" }} />
            </Form.Item>
            <Form.Item name="studyEventId" label={t("ecoa.form.studyEventId")}>
              <InputNumber min={1} style={{ width: "100%" }} />
            </Form.Item>
            <Form.Item
              name="questionnaireVersionId"
              label={t("ecoa.form.version")}
              rules={[{ required: true, message: t("ecoa.form.versionRequired") }]}
            >
              <Input maxLength={80} />
            </Form.Item>
            <Form.Item
              name="title"
              label={t("ecoa.form.title")}
              rules={[{ required: true, message: t("ecoa.form.titleRequired") }]}
            >
              <Input maxLength={160} />
            </Form.Item>
            <Form.Item name="description" label={t("ecoa.form.description")}>
              <Input.TextArea rows={3} maxLength={1000} />
            </Form.Item>
            <Form.Item
              name="dueAt"
              label={t("ecoa.form.dueAt")}
              rules={[{ required: true, message: t("ecoa.form.dueRequired") }]}
            >
              <DatePicker showTime style={{ width: "100%" }} />
            </Form.Item>
            <Space style={{ width: "100%" }} size="middle">
              <Form.Item name="windowOpensAt" label={t("ecoa.form.windowOpens")} style={{ flex: 1 }}>
                <DatePicker showTime style={{ width: "100%" }} />
              </Form.Item>
              <Form.Item name="windowClosesAt" label={t("ecoa.form.windowCloses")} style={{ flex: 1 }}>
                <DatePicker showTime style={{ width: "100%" }} />
              </Form.Item>
            </Space>
          </Form>
        )}
      </Modal>
    </div>
  );
}
