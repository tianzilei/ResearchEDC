import { useMemo, useState } from "react";
import { Button, Card, Descriptions, Form, Input, InputNumber, Modal, Space, Table, Tag, Typography, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { formatApiError } from "@/api/errors";
import { SkeletonPage } from "@/components/SkeletonCard";
import { useApplyStudyTemplate, useCreateStudyTemplate, useStudyTemplates, type StudyTemplateDTO } from "@/hooks/useStudyBuild";

const { Text, Title } = Typography;

function defaultsFromText(value?: string): Record<string, unknown> {
  if (!value?.trim()) return {};
  const parsed = JSON.parse(value) as unknown;
  if (!parsed || Array.isArray(parsed) || typeof parsed !== "object") {
    throw new Error("defaults");
  }
  return parsed as Record<string, unknown>;
}

export default function StudyBuildDashboard() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [templateOpen, setTemplateOpen] = useState(false);
  const [applying, setApplying] = useState<StudyTemplateDTO | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [templateForm] = Form.useForm();
  const [applyForm] = Form.useForm();
  const { data: templates = [], isLoading, refetch } = useStudyTemplates();
  const createTemplate = useCreateStudyTemplate();
  const applyTemplate = useApplyStudyTemplate();

  const selectedTemplate = useMemo(
    () => templates.find((template) => template.id === selectedId) ?? templates[0],
    [selectedId, templates],
  );

  const submitTemplate = async () => {
    try {
      const values = await templateForm.validateFields();
      await createTemplate.mutateAsync({
        name: values.name,
        description: values.description,
        category: values.category,
        protocolType: values.protocolType,
        phase: values.phase,
        defaults: defaultsFromText(values.defaultsJson),
      });
      message.success(t("studyBuild.templateCreated"));
      setTemplateOpen(false);
      templateForm.resetFields();
      await refetch();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("studyBuild.error.template")));
    }
  };

  const submitApply = async () => {
    if (!applying) return;
    try {
      const values = await applyForm.validateFields();
      const result = await applyTemplate.mutateAsync({ templateId: applying.id, request: values });
      message.success(t("studyBuild.studyCreated"));
      setApplying(null);
      applyForm.resetFields();
      navigate(`/app/studies/${result.study.studyId}`);
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("studyBuild.error.apply")));
    }
  };

  if (isLoading) {
    return <SkeletonPage />;
  }

  const columns: ColumnsType<StudyTemplateDTO> = [
    { title: t("studyBuild.column.name"), dataIndex: "name", key: "name" },
    { title: t("studyBuild.column.category"), dataIndex: "category", key: "category", render: (value: string | null) => value ? <Tag>{value}</Tag> : "-" },
    { title: t("studyBuild.column.protocol"), dataIndex: "protocolType", key: "protocolType", render: (value: string | null) => value || "-" },
    { title: t("studyBuild.column.phase"), dataIndex: "phase", key: "phase", render: (value: string | null) => value || "-" },
    {
      title: t("studyBuild.column.actions"),
      key: "actions",
      render: (_, template) => (
        <Space>
          <Button size="small" onClick={() => setSelectedId(template.id)}>{t("studyBuild.action.preview")}</Button>
          <Button size="small" type="primary" onClick={() => { setApplying(template); applyForm.resetFields(); }}>{t("studyBuild.action.apply")}</Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <div>
          <Title level={4} style={{ margin: 0 }}>{t("studyBuild.title")}</Title>
          <Text type="secondary">{templates.length} {t("studyBuild.templates")}</Text>
        </div>
        <Space>
          <Button onClick={() => void refetch()}>{t("common.refresh")}</Button>
          <Button type="primary" onClick={() => setTemplateOpen(true)}>{t("studyBuild.newTemplate")}</Button>
        </Space>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "minmax(0, 1.5fr) minmax(320px, 0.8fr)", gap: 16 }}>
        <Card styles={{ body: { padding: 0 } }}>
          <Table columns={columns} dataSource={templates} rowKey="id" pagination={{ pageSize: 12 }} />
        </Card>
        <Card title={t("studyBuild.preview")}>
          {selectedTemplate ? (
            <Descriptions column={1} size="small">
              <Descriptions.Item label={t("studyBuild.form.name")}>{selectedTemplate.name}</Descriptions.Item>
              <Descriptions.Item label={t("studyBuild.form.description")}>{selectedTemplate.description || "-"}</Descriptions.Item>
              <Descriptions.Item label={t("studyBuild.form.protocolType")}>{selectedTemplate.protocolType || "-"}</Descriptions.Item>
              <Descriptions.Item label={t("studyBuild.form.phase")}>{selectedTemplate.phase || "-"}</Descriptions.Item>
              <Descriptions.Item label={t("studyBuild.form.defaults")}>
                <pre style={{ whiteSpace: "pre-wrap", margin: 0, fontSize: 12 }}>{JSON.stringify(selectedTemplate.defaults ?? {}, null, 2)}</pre>
              </Descriptions.Item>
            </Descriptions>
          ) : (
            <Text type="secondary">{t("studyBuild.empty")}</Text>
          )}
        </Card>
      </div>

      <Modal title={t("studyBuild.modal.template")} open={templateOpen} onOk={submitTemplate} onCancel={() => setTemplateOpen(false)} confirmLoading={createTemplate.isPending} width={720}>
        <Form form={templateForm} layout="vertical">
          <Form.Item name="name" label={t("studyBuild.form.name")} rules={[{ required: true }]}>
            <Input maxLength={160} />
          </Form.Item>
          <Form.Item name="description" label={t("studyBuild.form.description")}>
            <Input.TextArea rows={3} maxLength={1000} />
          </Form.Item>
          <Form.Item name="category" label={t("studyBuild.form.category")}>
            <Input maxLength={80} />
          </Form.Item>
          <Form.Item name="protocolType" label={t("studyBuild.form.protocolType")}>
            <Input maxLength={30} />
          </Form.Item>
          <Form.Item name="phase" label={t("studyBuild.form.phase")}>
            <Input maxLength={30} />
          </Form.Item>
          <Form.Item name="defaultsJson" label={t("studyBuild.form.defaults")} initialValue={'{"typeId":1,"statusId":1}'}>
            <Input.TextArea rows={6} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={t("studyBuild.modal.apply")} open={!!applying} onOk={submitApply} onCancel={() => setApplying(null)} confirmLoading={applyTemplate.isPending}>
        <Form form={applyForm} layout="vertical">
          <Form.Item name="name" label={t("studyBuild.form.studyName")} rules={[{ required: true }]}>
            <Input maxLength={60} />
          </Form.Item>
          <Form.Item name="uniqueIdentifier" label={t("studyBuild.form.uniqueIdentifier")} rules={[{ required: true }]}>
            <Input maxLength={30} />
          </Form.Item>
          <Form.Item name="principalInvestigator" label={t("studyBuild.form.pi")}>
            <Input maxLength={255} />
          </Form.Item>
          <Form.Item name="facilityName" label={t("studyBuild.form.facility")}>
            <Input maxLength={255} />
          </Form.Item>
          <Form.Item name="sponsor" label={t("studyBuild.form.sponsor")}>
            <Input maxLength={255} />
          </Form.Item>
          <Form.Item name="expectedTotalEnrollment" label={t("studyBuild.form.enrollment")}>
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
