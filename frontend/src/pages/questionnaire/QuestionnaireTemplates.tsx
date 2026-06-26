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
  Input,
  Select,
  message,
  Empty,
} from "antd";

import { useNavigate } from "react-router-dom";
import { useCurrentStudy } from "@/hooks/useStudies";
import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title } = Typography;

interface Template {
  id: string;
  study_id: string | null;
  code: string;
  name: string;
  description: string | null;
  category: string | null;
  status: string;
  created_by: string;
  created_at: string;
  updated_at: string;
}

const categoryStatus: Record<string, string> = {
  SLEEP: "info",
  ANXIETY: "warning",
  DEPRESSION: "info",
  COGNITION: "info",
  SAFETY: "danger",
  QUALITY_OF_LIFE: "success",
};

function useTemplates(studyId: number | undefined) {
  return useAppQuery<Template[]>({
    queryKey: ["questionnaire-templates", studyId],
    queryFn: () =>
      studyId
        ? apiClient.get<Template[]>("/api/v1/questionnaires/templates", { studyId })
        : Promise.resolve([]),
    enabled: true,
  });
}

export default function QuestionnaireTemplates() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id;
  const qc = useQueryClient();
  const { data: templates, isLoading } = useTemplates(studyId);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const createTemplate = useAppMutation<Template, Template>({
    mutationFn: (body) =>
      apiClient.post<Template>("/api/v1/questionnaires/templates", body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-templates"] });
      message.success(t("questionnaire.template.created"));
      setModalOpen(false);
      form.resetFields();
    },
  });

  if (isLoading) return <SkeletonPage />;

  const columns = [
    {
      title: t("questionnaire.template.column.code"),
      dataIndex: "code",
      key: "code",
      render: (code: string) => <span className="status status-info">{code}</span>,
    },
    { title: t("questionnaire.template.column.name"), dataIndex: "name", key: "name" },
    {
      title: t("questionnaire.template.column.category"),
      dataIndex: "category",
      key: "category",
      render: (cat: string | null) =>
        cat ? <span className={`status status-${categoryStatus[cat] ?? "default"}`}>{cat}</span> : "-",
    },
    {
      title: t("questionnaire.template.column.status"),
      dataIndex: "status",
      key: "status",
      render: (s: string) => (
        <span className={s === "active" ? "status status-success" : "status status-default"}>{s}</span>
      ),
    },
    {
      title: t("questionnaire.template.column.updated"),
      dataIndex: "updated_at",
      key: "updated_at",
      render: (d: string) => (d ? new Date(d).toLocaleDateString() : "-"),
    },
    {
      title: t("questionnaire.template.column.actions"),
      key: "actions",
      render: (_: unknown, r: Template) => (
        <Button
          size="small"
          onClick={() => navigate(`/app/questionnaires/templates/${r.id}/versions`)}
        >
          {t("questionnaire.template.action.versions")}
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <Title level={4} style={{ marginTop: 0 }}>
          {t("questionnaire.templates")}
        </Title>
        <Button type="primary" onClick={() => setModalOpen(true)}>
          {t("questionnaire.template.new")}
        </Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={templates ?? []}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 20 }}
          locale={{ emptyText: <Empty description={t("questionnaire.template.empty")} /> }}
        />
      </Card>

      <Modal
        title={t("questionnaire.template.create")}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={createTemplate.isPending}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => {
            createTemplate.mutate({
              ...values,
              study_id: studyId ?? null,
            });
          }}
        >
          <Form.Item name="code" label={t("questionnaire.template.code")} rules={[{ required: true }]}>
            <Input placeholder={t("questionnaire.template.codePlaceholder")} />
          </Form.Item>
          <Form.Item name="name" label={t("questionnaire.template.name")} rules={[{ required: true }]}>
            <Input placeholder={t("questionnaire.template.namePlaceholder")} />
          </Form.Item>
          <Form.Item name="description" label={t("questionnaire.template.description")}>
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="category" label={t("questionnaire.template.category")}>
            <Select allowClear>
              <Select.Option value="SLEEP">{t("questionnaire.template.category.sleep")}</Select.Option>
              <Select.Option value="ANXIETY">{t("questionnaire.template.category.anxiety")}</Select.Option>
              <Select.Option value="DEPRESSION">{t("questionnaire.template.category.depression")}</Select.Option>
              <Select.Option value="SAFETY">{t("questionnaire.template.category.safety")}</Select.Option>
              <Select.Option value="QUALITY_OF_LIFE">{t("questionnaire.template.category.qualityOfLife")}</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
