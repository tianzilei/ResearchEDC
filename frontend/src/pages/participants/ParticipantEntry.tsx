import { useState } from "react";
import { useParams } from "react-router-dom";
import {
  Alert,
  Button,
  Card,
  Empty,
  Flex,
  Form,
  Input,
  Modal,
  Result,
  Space,
  Statistic,
  Tag,
  Typography,
  message,
} from "antd";
import { CheckCircleOutlined, FileDoneOutlined, FormOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import { ecoaApi } from "@/api/ecoa";
import { econsentApi } from "@/api/econsent";
import { formatApiError } from "@/api/errors";
import { SkeletonPage } from "@/components/SkeletonCard";
import { useParticipantPortal, type ParticipantPortalTaskDTO } from "@/hooks/useParticipantPortal";

const { Paragraph, Text, Title } = Typography;

const STATUS_COLORS: Record<string, string> = {
  PENDING: "default",
  IN_PROGRESS: "processing",
  SUBMITTED: "success",
  REVIEWED: "blue",
  OVERDUE: "error",
  CANCELLED: "warning",
  ASSIGNED: "processing",
  PARTICIPANT_SIGNED: "blue",
  COUNTERSIGNED: "success",
  SUPERSEDED: "default",
};

export default function ParticipantEntry() {
  const { t } = useTranslation();
  const { token } = useParams<{ token: string }>();
  const [signingTask, setSigningTask] = useState<ParticipantPortalTaskDTO | null>(null);
  const [signForm] = Form.useForm<{ participantName: string; signature: string }>();
  const { data, isLoading, error, refetch } = useParticipantPortal(token);

  const completeQuestionnaire = async (task: ParticipantPortalTaskDTO) => {
    if (!token) return;
    try {
      await ecoaApi.completeParticipantAssignment(task.assignmentId, token, {
        completedAt: new Date().toISOString(),
        scoreSummary: t("participantEntry.completedThroughPortal"),
      });
      message.success(t("participantEntry.completed"));
      await refetch();
    } catch (err) {
      message.error(formatApiError(err, t("participantEntry.completeFailed")));
    }
  };

  const submitConsentSignature = async () => {
    if (!token || !signingTask) return;
    try {
      const values = await signForm.validateFields();
      await econsentApi.signParticipantConsent(signingTask.assignmentId, token, {
        ...values,
        evidence: `browser=${navigator.userAgent}`,
      });
      message.success(t("participantEntry.consentSigned"));
      setSigningTask(null);
      signForm.resetFields();
      await refetch();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("participantEntry.consentSignFailed")));
    }
  };

  if (!token) {
    return <Result status="403" title={t("participantEntry.invalid")} />;
  }
  if (isLoading) {
    return <SkeletonPage />;
  }
  if (error || !data) {
    return <Result status="403" title={t("participantEntry.denied")} subTitle={formatApiError(error, t("participantEntry.deniedDetail"))} />;
  }

  const participant = data.participant;

  return (
    <div style={{ maxWidth: 980, margin: "32px auto", padding: "0 16px 48px" }}>
      <section style={{ marginBottom: 20 }}>
        <Text type="secondary">{t("participantEntry.portalLabel")}</Text>
        <Title level={2} style={{ marginTop: 4, marginBottom: 8 }}>
          {t("participantEntry.title", { label: participant.displayLabel })}
        </Title>
        <Text type="secondary">{t("participantEntry.subtitle")}</Text>
      </section>

      <Alert
        type="info"
        showIcon
        message={t("participantEntry.linkStatus")}
        description={`${t("participantEntry.scope")}: ${participant.scope} · ${t("participantEntry.expires")}: ${new Date(participant.expiresAt).toLocaleString()}`}
        style={{ marginBottom: 16 }}
      />

      <Flex gap={12} wrap="wrap" style={{ marginBottom: 16 }}>
        <Card style={{ flex: "1 1 160px" }}>
          <Statistic title={t("participantEntry.summary.total")} value={data.summary.totalTasks} />
        </Card>
        <Card style={{ flex: "1 1 160px" }}>
          <Statistic title={t("participantEntry.summary.ready")} value={data.summary.actionableTasks} />
        </Card>
        <Card style={{ flex: "1 1 160px" }}>
          <Statistic title={t("participantEntry.summary.questionnaires")} value={data.summary.questionnaireTasks} />
        </Card>
        <Card style={{ flex: "1 1 160px" }}>
          <Statistic title={t("participantEntry.summary.consents")} value={data.summary.consentTasks} />
        </Card>
      </Flex>

      <Card>
        <Flex justify="space-between" align="center" gap={12} wrap="wrap" style={{ marginBottom: 12 }}>
          <Title level={4} style={{ margin: 0 }}>{t("participantEntry.taskInbox")}</Title>
          <Tag color={data.summary.overdueTasks > 0 ? "error" : "success"}>
            {t("participantEntry.summary.overdue", { count: data.summary.overdueTasks })}
          </Tag>
        </Flex>
        {data.tasks.length === 0 ? (
          <Empty description={t("participantEntry.noTasks")} />
        ) : (
          <Space direction="vertical" style={{ width: "100%" }} size={12}>
            {data.tasks.map((task) => (
              <TaskCard
                key={task.id}
                task={task}
                onCompleteQuestionnaire={completeQuestionnaire}
                onSignConsent={setSigningTask}
              />
            ))}
          </Space>
        )}
      </Card>

      <Modal
        title={t("participantEntry.signConsent")}
        open={!!signingTask}
        onCancel={() => {
          setSigningTask(null);
          signForm.resetFields();
        }}
        onOk={submitConsentSignature}
      >
        {signingTask?.consentBodyText && (
          <Paragraph style={{ whiteSpace: "pre-wrap", maxHeight: 240, overflow: "auto" }}>
            {signingTask.consentBodyText}
          </Paragraph>
        )}
        <Form form={signForm} layout="vertical">
          <Form.Item name="participantName" label={t("participantEntry.participantName")} rules={[{ required: true }]}>
            <Input maxLength={160} />
          </Form.Item>
          <Form.Item name="signature" label={t("participantEntry.signature")} rules={[{ required: true }]}>
            <Input maxLength={240} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

function TaskCard({
  task,
  onCompleteQuestionnaire,
  onSignConsent,
}: {
  task: ParticipantPortalTaskDTO;
  onCompleteQuestionnaire: (task: ParticipantPortalTaskDTO) => void;
  onSignConsent: (task: ParticipantPortalTaskDTO) => void;
}) {
  const { t } = useTranslation();
  const isConsent = task.type === "CONSENT";
  const icon = isConsent ? <FileDoneOutlined /> : <FormOutlined />;
  const statusKey = isConsent ? `econsent.status.${task.status.toLowerCase()}` : `ecoa.status.${task.status.toLowerCase()}`;

  return (
    <Card size="small">
      <Flex justify="space-between" gap={12} align="flex-start" wrap="wrap">
        <Space align="start">
          <Button shape="circle" icon={icon} aria-label={task.type} />
          <Space direction="vertical" size={2}>
            <Text strong>{task.title}</Text>
            {task.subtitle && <Text type="secondary">{task.subtitle}</Text>}
            {task.dueAt && <Text type="secondary">{t("participantEntry.due")}: {new Date(task.dueAt).toLocaleString()}</Text>}
          </Space>
        </Space>
        <Tag color={STATUS_COLORS[task.status] ?? "default"}>{t(statusKey)}</Tag>
      </Flex>
      {task.type === "CONSENT" && task.consentBodyText && (
        <Paragraph ellipsis={{ rows: 3, expandable: true, symbol: t("participantEntry.expand") }} style={{ marginTop: 12, whiteSpace: "pre-wrap" }}>
          {task.consentBodyText}
        </Paragraph>
      )}
      <Flex justify="flex-end" style={{ marginTop: 12 }}>
        {task.type === "ECOA" ? (
          <Button
            type="primary"
            icon={<CheckCircleOutlined />}
            disabled={!task.actionable}
            onClick={() => onCompleteQuestionnaire(task)}
          >
            {t("participantEntry.markComplete")}
          </Button>
        ) : (
          <Button type="primary" icon={<FileDoneOutlined />} disabled={!task.actionable} onClick={() => onSignConsent(task)}>
            {t("participantEntry.signConsent")}
          </Button>
        )}
      </Flex>
    </Card>
  );
}
