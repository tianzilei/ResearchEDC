import { useMemo, useState } from "react";
import { Alert, Button, Card, DatePicker, Empty, Form, Input, InputNumber, Modal, Space, Table, Tag, Typography, message } from "antd";
import { CheckCircleOutlined, FileTextOutlined, LinkOutlined, PlusOutlined } from "@ant-design/icons";
import type { Dayjs } from "dayjs";
import { useTranslation } from "react-i18next";
import { formatApiError } from "@/api/errors";
import { econsentApi } from "@/api/econsent";
import { useCurrentStudy } from "@/hooks/useStudies";
import {
  useAssignConsent,
  useConsentAssignments,
  useConsentTemplates,
  useConsentVersions,
  useCountersignConsent,
  useCreateConsentTemplate,
  useCreateConsentVersion,
  usePublishConsentVersion,
  type ConsentAssignmentDTO,
  type ConsentAssignmentResultDTO,
  type ConsentTemplateDTO,
  type ConsentVersionDTO,
} from "@/hooks/useEconsent";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text, Paragraph } = Typography;

interface TemplateFormValues {
  code: string;
  name: string;
  description?: string;
}

interface VersionFormValues {
  versionLabel: string;
  bodyText: string;
}

interface AssignFormValues {
  studySubjectId: number;
  consentVersionId: number;
  dueAt?: Dayjs;
}

interface CountersignFormValues {
  countersignature: string;
}

const STATUS_COLORS: Record<string, string> = {
  ASSIGNED: "default",
  PARTICIPANT_SIGNED: "processing",
  COUNTERSIGNED: "success",
  SUPERSEDED: "warning",
  CANCELLED: "error",
};

export default function EconsentPage() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id;
  const [selectedTemplate, setSelectedTemplate] = useState<ConsentTemplateDTO | null>(null);
  const [templateOpen, setTemplateOpen] = useState(false);
  const [versionOpen, setVersionOpen] = useState(false);
  const [assignOpen, setAssignOpen] = useState(false);
  const [countersignOpen, setCountersignOpen] = useState<ConsentAssignmentDTO | null>(null);
  const [issued, setIssued] = useState<ConsentAssignmentResultDTO | null>(null);
  const [artifact, setArtifact] = useState<string | null>(null);
  const [templateForm] = Form.useForm<TemplateFormValues>();
  const [versionForm] = Form.useForm<VersionFormValues>();
  const [assignForm] = Form.useForm<AssignFormValues>();
  const [countersignForm] = Form.useForm<CountersignFormValues>();

  const { data: templates = [], isLoading: templatesLoading } = useConsentTemplates(studyId);
  const { data: versions = [], isLoading: versionsLoading } = useConsentVersions(selectedTemplate?.id);
  const { data: assignments = [], isLoading: assignmentsLoading } = useConsentAssignments(studyId);
  const createTemplate = useCreateConsentTemplate(studyId);
  const createVersion = useCreateConsentVersion(selectedTemplate?.id);
  const publishVersion = usePublishConsentVersion(selectedTemplate?.id);
  const assignConsent = useAssignConsent(studyId);
  const countersignConsent = useCountersignConsent(studyId);

  const publishedVersions = useMemo(() => versions.filter((v) => v.status === "PUBLISHED"), [versions]);

  if (!currentStudy) {
    return <Alert type="info" message={t("econsent.selectStudy")} />;
  }
  if (templatesLoading || assignmentsLoading) {
    return <SkeletonPage />;
  }

  const submitTemplate = async () => {
    try {
      const values = await templateForm.validateFields();
      if (!studyId) return;
      const created = await createTemplate.mutateAsync({ ...values, studyId });
      message.success(t("econsent.templateCreated"));
      setSelectedTemplate(created);
      setTemplateOpen(false);
      templateForm.resetFields();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("econsent.error.template")));
    }
  };

  const submitVersion = async () => {
    try {
      const values = await versionForm.validateFields();
      await createVersion.mutateAsync(values);
      message.success(t("econsent.versionCreated"));
      setVersionOpen(false);
      versionForm.resetFields();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("econsent.error.version")));
    }
  };

  const submitAssignment = async () => {
    try {
      const values = await assignForm.validateFields();
      const result = await assignConsent.mutateAsync({
        studySubjectId: values.studySubjectId,
        consentVersionId: values.consentVersionId,
        dueAt: values.dueAt?.toISOString(),
      });
      setIssued(result);
      message.success(t("econsent.assigned"));
      assignForm.resetFields();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("econsent.error.assign")));
    }
  };

  const submitCountersign = async () => {
    if (!countersignOpen) return;
    try {
      const values = await countersignForm.validateFields();
      await countersignConsent.mutateAsync({
        assignmentId: countersignOpen.id,
        request: values,
      });
      message.success(t("econsent.countersigned"));
      setCountersignOpen(null);
      countersignForm.resetFields();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("econsent.error.countersign")));
    }
  };

  const loadArtifact = async (assignmentId: number) => {
    try {
      const result = await econsentApi.artifact(assignmentId);
      setArtifact(result.content);
    } catch (err) {
      message.error(formatApiError(err, t("econsent.error.artifact")));
    }
  };

  const templateColumns = [
    { title: t("econsent.column.code"), dataIndex: "code", key: "code", width: 120 },
    { title: t("econsent.column.name"), dataIndex: "name", key: "name" },
    { title: t("econsent.column.created"), dataIndex: "createdDate", key: "createdDate", width: 180,
      render: (value: string) => new Date(value).toLocaleString(),
    },
  ];

  const versionColumns = [
    { title: t("econsent.column.version"), dataIndex: "versionLabel", key: "versionLabel", width: 140 },
    { title: t("econsent.column.status"), dataIndex: "status", key: "status", width: 140,
      render: (value: string) => <Tag color={value === "PUBLISHED" ? "success" : "default"}>{t(`econsent.versionStatus.${value.toLowerCase()}`)}</Tag>,
    },
    { title: t("econsent.column.created"), dataIndex: "createdDate", key: "createdDate",
      render: (value: string) => new Date(value).toLocaleString(),
    },
    { title: t("econsent.column.actions"), key: "actions", width: 140,
      render: (_: unknown, record: ConsentVersionDTO) => (
        <Button
          size="small"
          disabled={record.status === "PUBLISHED"}
          loading={publishVersion.isPending}
          onClick={() => publishVersion.mutate(record.id)}
        >
          {t("econsent.publish")}
        </Button>
      ),
    },
  ];

  const assignmentColumns = [
    { title: t("econsent.column.studySubject"), dataIndex: "studySubjectId", key: "studySubjectId", width: 140 },
    { title: t("econsent.column.version"), dataIndex: "consentVersionId", key: "consentVersionId", width: 140 },
    { title: t("econsent.column.status"), dataIndex: "status", key: "status", width: 170,
      render: (value: string) => <Tag color={STATUS_COLORS[value] ?? "default"}>{t(`econsent.status.${value.toLowerCase()}`)}</Tag>,
    },
    { title: t("econsent.column.signed"), dataIndex: "participantSignedAt", key: "participantSignedAt", width: 190,
      render: (value: string | null) => value ? new Date(value).toLocaleString() : "-",
    },
    { title: t("econsent.column.countersigned"), dataIndex: "countersignedAt", key: "countersignedAt", width: 190,
      render: (value: string | null) => value ? new Date(value).toLocaleString() : "-",
    },
    { title: t("econsent.column.link"), dataIndex: "entryUrl", key: "entryUrl", width: 120,
      render: (value: string | null) => value ? (
        <Button size="small" icon={<LinkOutlined />} onClick={() => navigator.clipboard.writeText(`${window.location.origin}${value}`)}>
          {t("econsent.copy")}
        </Button>
      ) : "-",
    },
    { title: t("econsent.column.actions"), key: "actions", width: 260,
      render: (_: unknown, record: ConsentAssignmentDTO) => (
        <Space>
          <Button
            size="small"
            icon={<CheckCircleOutlined />}
            disabled={record.status !== "PARTICIPANT_SIGNED"}
            onClick={() => setCountersignOpen(record)}
          >
            {t("econsent.countersign")}
          </Button>
          <Button
            size="small"
            icon={<FileTextOutlined />}
            disabled={!record.artifactName}
            onClick={() => loadArtifact(record.id)}
          >
            {t("econsent.artifact")}
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }} align="start">
        <div>
          <Title level={4} style={{ marginTop: 0 }}>{t("econsent.title")}</Title>
          <Text type="secondary">{currentStudy.name}</Text>
        </div>
        <Space>
          <Button icon={<PlusOutlined />} onClick={() => setTemplateOpen(true)}>{t("econsent.newTemplate")}</Button>
          <Button type="primary" icon={<PlusOutlined />} disabled={publishedVersions.length === 0} onClick={() => {
            setIssued(null);
            setAssignOpen(true);
          }}>
            {t("econsent.assign")}
          </Button>
        </Space>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={templates}
          columns={templateColumns}
          rowKey="id"
          size="small"
          rowSelection={{
            type: "radio",
            selectedRowKeys: selectedTemplate ? [selectedTemplate.id] : [],
            onChange: (_, rows) => setSelectedTemplate(rows[0] as ConsentTemplateDTO),
          }}
          pagination={{ pageSize: 8 }}
          locale={{ emptyText: <Empty description={t("econsent.templateEmpty")} /> }}
        />
      </Card>

      <Card
        style={{ marginTop: 16 }}
        title={selectedTemplate ? t("econsent.versionsFor", { name: selectedTemplate.name }) : t("econsent.versions")}
        extra={<Button size="small" disabled={!selectedTemplate} onClick={() => setVersionOpen(true)}>{t("econsent.newVersion")}</Button>}
      >
        {versionsLoading ? <SkeletonPage /> : (
          <Table
            dataSource={versions}
            columns={versionColumns}
            rowKey="id"
            size="small"
            pagination={{ pageSize: 8 }}
            locale={{ emptyText: <Empty description={t("econsent.versionEmpty")} /> }}
          />
        )}
      </Card>

      <Card style={{ marginTop: 16 }} title={t("econsent.assignments")}>
        <Table
          dataSource={assignments}
          columns={assignmentColumns}
          rowKey="id"
          pagination={{ pageSize: 12 }}
          locale={{ emptyText: <Empty description={t("econsent.assignmentEmpty")} /> }}
        />
      </Card>

      <Modal title={t("econsent.modal.template")} open={templateOpen} onCancel={() => setTemplateOpen(false)} onOk={submitTemplate} confirmLoading={createTemplate.isPending}>
        <Form form={templateForm} layout="vertical">
          <Form.Item name="code" label={t("econsent.form.code")} rules={[{ required: true }]}>
            <Input maxLength={80} />
          </Form.Item>
          <Form.Item name="name" label={t("econsent.form.name")} rules={[{ required: true }]}>
            <Input maxLength={160} />
          </Form.Item>
          <Form.Item name="description" label={t("econsent.form.description")}>
            <Input.TextArea rows={3} maxLength={1000} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={t("econsent.modal.version")} open={versionOpen} onCancel={() => setVersionOpen(false)} onOk={submitVersion} confirmLoading={createVersion.isPending}>
        <Form form={versionForm} layout="vertical">
          <Form.Item name="versionLabel" label={t("econsent.form.versionLabel")} rules={[{ required: true }]}>
            <Input maxLength={40} />
          </Form.Item>
          <Form.Item name="bodyText" label={t("econsent.form.body")} rules={[{ required: true }]}>
            <Input.TextArea rows={8} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={t("econsent.modal.assign")}
        open={assignOpen}
        onCancel={() => {
          setAssignOpen(false);
          setIssued(null);
          assignForm.resetFields();
        }}
        onOk={issued ? () => setAssignOpen(false) : submitAssignment}
        okText={issued ? t("econsent.close") : t("econsent.assign")}
        confirmLoading={assignConsent.isPending}
      >
        {issued ? (
          <Alert type="success" showIcon message={t("econsent.linkIssued")} description={`${window.location.origin}${issued.participantEntryUrl}`} />
        ) : (
          <Form form={assignForm} layout="vertical">
            <Form.Item name="studySubjectId" label={t("econsent.form.studySubjectId")} rules={[{ required: true }]}>
              <InputNumber min={1} style={{ width: "100%" }} />
            </Form.Item>
            <Form.Item name="consentVersionId" label={t("econsent.form.version")} rules={[{ required: true }]}>
              <InputNumber min={1} style={{ width: "100%" }} />
            </Form.Item>
            <Form.Item name="dueAt" label={t("econsent.form.dueAt")}>
              <DatePicker showTime style={{ width: "100%" }} />
            </Form.Item>
          </Form>
        )}
      </Modal>

      <Modal title={t("econsent.modal.countersign")} open={!!countersignOpen} onCancel={() => setCountersignOpen(null)} onOk={submitCountersign} confirmLoading={countersignConsent.isPending}>
        <Form form={countersignForm} layout="vertical">
          <Form.Item name="countersignature" label={t("econsent.form.countersignature")} rules={[{ required: true }]}>
            <Input maxLength={240} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={t("econsent.artifact")} open={!!artifact} onCancel={() => setArtifact(null)} footer={<Button onClick={() => setArtifact(null)}>{t("econsent.close")}</Button>} width={760}>
        <Paragraph style={{ whiteSpace: "pre-wrap", fontFamily: "monospace" }}>{artifact}</Paragraph>
      </Modal>
    </div>
  );
}
