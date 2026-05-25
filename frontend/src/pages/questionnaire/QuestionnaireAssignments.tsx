import { useState } from "react";
import { useTranslation } from "react-i18next";
import {
  Card,
  Table,
  Button,
  Space,
  Typography,
  Modal,
  Form,
  Select,
  DatePicker,
  message,
  Empty,
} from "antd";

import { useCurrentStudy } from "@/hooks/useStudies";
import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface Assignment {
  id: string;
  study_id: string;
  subject_id: string;
  visit_id: string | null;
  randomization_arm_id: string | null;
  questionnaire_version_id: string;
  status: string;
  due_at: string | null;
  has_token: boolean;
  token_expires_at: string | null;
  created_by: string;
  created_at: string;
  updated_at: string;
}

interface Template {
  id: string;
  code: string;
  name: string;
}

interface Version {
  id: string;
  template_id: string;
  version_no: string;
  status: string;
}

const statusClassMap: Record<string, string> = {
  pending: "default",
  in_progress: "warning",
  submitted: "success",
  reviewed: "info",
  locked: "info",
  expired: "warning",
  withdrawn: "danger",
};

function useAssignments(studyId: number) {
  return useAppQuery<Assignment[]>({
    queryKey: ["questionnaire-assignments", studyId],
    queryFn: () =>
      apiClient.get<Assignment[]>("/api/v1/questionnaires/assignments", {
        studyId: studyId > 0 ? studyId : undefined,
      }),
    enabled: true,
  });
}

function useTemplates() {
  return useAppQuery<Template[]>({
    queryKey: ["questionnaire-templates-all"],
    queryFn: () =>
      apiClient.get<Template[]>("/api/v1/questionnaires/templates"),
    enabled: true,
  });
}

export default function QuestionnaireAssignments() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id ?? 0;
  const qc = useQueryClient();
  const { data: assignments, isLoading } = useAssignments(studyId);
  const { data: templates } = useTemplates();
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();
  const [selectedTemplateId, setSelectedTemplateId] = useState<string | null>(null);

  const { data: versions } = useAppQuery<Version[]>({
    queryKey: ["questionnaire-versions-by-template", selectedTemplateId],
    queryFn: () =>
      apiClient.get<Version[]>(
        `/api/v1/questionnaires/templates/${selectedTemplateId}/versions`,
      ),
    enabled: !!selectedTemplateId,
  });

  const createAssignment = useAppMutation<Assignment, any>({
    mutationFn: (body) =>
      apiClient.post<Assignment>("/api/v1/questionnaires/assignments", body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-assignments"] });
      message.success(t("assignment.created"));
      setCreateOpen(false);
      form.resetFields();
    },
  });

  const publishedVersions = (versions ?? []).filter(
    (v) => v.status === "published",
  );

  if (isLoading) return <SkeletonPage />;

  const columns = [
    {
      title: t("assignment.column.subjectId"),
      dataIndex: "subject_id",
      key: "subject_id",
      render: (id: string) => (
        <Text code style={{ fontSize: 12 }}>
          {id.slice(0, 8)}...
        </Text>
      ),
    },
    {
      title: t("assignment.column.version"),
      dataIndex: "questionnaire_version_id",
      key: "questionnaire_version_id",
      render: (id: string) => (
        <Text code style={{ fontSize: 11 }}>{id.slice(0, 8)}...</Text>
      ),
    },
    {
      title: t("assignment.column.status"),
      dataIndex: "status",
      key: "status",
      render: (s: string) => <span className={`status status-${statusClassMap[s] ?? "default"}`}>{s}</span>,
    },
    {
      title: t("assignment.column.due"),
      dataIndex: "due_at",
      key: "due_at",
      render: (d: string | null) =>
        d ? (
          <span>{new Date(d).toLocaleDateString()}</span>
        ) : (
          "-"
        ),
    },
    {
      title: t("assignment.column.token"),
      dataIndex: "has_token",
      key: "has_token",
      render: (hasTok: boolean) =>
        hasTok ? <span className="status status-info">{t("assignment.token.issued")}</span> : <span className="status status-default">{t("assignment.token.none")}</span>,
    },
    {
      title: t("assignment.column.created"),
      dataIndex: "created_at",
      key: "created_at",
      render: (d: string) => new Date(d).toLocaleDateString(),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <Title level={4} style={{ marginTop: 0 }}>
          {t("questionnaire.assignments")}
        </Title>
        <Button type="primary" onClick={() => setCreateOpen(true)}>
          {t("assignment.new")}
        </Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={assignments ?? []}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 20 }}
          locale={{ emptyText: <Empty description={t("assignment.empty")} /> }}
        />
      </Card>

      <Modal
        title={t("assignment.create")}
        open={createOpen}
        onCancel={() => {
          setCreateOpen(false);
          form.resetFields();
          setSelectedTemplateId(null);
        }}
        onOk={() => form.submit()}
        confirmLoading={createAssignment.isPending}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => {
            createAssignment.mutate({
              ...values,
              study_id: studyId,
              subject_id: values.subject_id ?? "00000000-0000-0000-0000-000000000000",
            });
          }}
        >
          <Form.Item name="questionnaire_version_id" label={t("assignment.version")} rules={[{ required: true }]}>
            <Select
              placeholder={t("assignment.selectTemplate")}
              onSelect={(val: string) => setSelectedTemplateId(val)}
            >
              {(templates ?? []).map((t) => (
                <Select.Option key={t.id} value={t.id}>
                  {t.code} - {t.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          {publishedVersions.length > 0 && (
            <Form.Item label={t("assignment.availableVersions")}>
              <Select placeholder={t("assignment.selectVersion")}>
                {publishedVersions.map((v) => (
                  <Select.Option key={v.id} value={v.id}>
                    {v.version_no}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
          )}
          <Form.Item name="due_at" label={t("assignment.dueDate")}>
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
