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
  Tag,
  message,
  Empty,
  Tabs,
} from "antd";

import { useParams, useNavigate } from "react-router-dom";
import { Model } from "survey-core";
import { Survey } from "survey-react-ui";
import { useAppQuery, useAppMutation, useQueryClient } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";
import QuestionnaireBuilder from "@/components/questionnaire-builder/QuestionnaireBuilder";
import "survey-core/survey-core.min.css";

const { Title, Text } = Typography;
const { TextArea } = Input;

interface Version {
  id: string;
  template_id: string;
  version_no: string;
  surveyjs_schema: Record<string, unknown>;
  validation_schema: Record<string, unknown> | null;
  scoring_schema: Record<string, unknown> | null;
  language: string;
  schema_hash: string;
  status: string;
  published_by: string | null;
  published_at: string | null;
  created_at: string;
  updated_at: string;
}

function useVersions(templateId: string) {
  return useAppQuery<Version[]>({
    queryKey: ["questionnaire-versions", templateId],
    queryFn: () =>
      apiClient.get<Version[]>(
        `/api/v1/questionnaires/templates/${templateId}/versions`,
      ),
    enabled: !!templateId,
  });
}

export default function QuestionnaireVersionEditor() {
  const { t } = useTranslation();
  const { templateId } = useParams<{ templateId: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { data: versions, isLoading } = useVersions(templateId!);
  const [previewVersion, setPreviewVersion] = useState<Version | null>(null);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [editVersionId, setEditVersionId] = useState<string | null>(null);
  const [editJson, setEditJson] = useState<Record<string, unknown> | null>(null);
  const [surveyJson, setSurveyJson] = useState("");
  const [builderJson, setBuilderJson] = useState<Record<string, unknown>>({});
  const [editMode, setEditMode] = useState<"builder" | "json">("builder");
  const [form] = Form.useForm();

  const createVersion = useAppMutation<Version, any>({
    mutationFn: (body) =>
      apiClient.post<Version>(
        `/api/v1/questionnaires/templates/${templateId}/versions`,
        body,
      ),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-versions", templateId] });
      message.success(t("version.created"));
      setCreateOpen(false);
      form.resetFields();
      setSurveyJson("");
      setBuilderJson({});
    },
  });

  const updateVersion = useAppMutation<void, { id: string; data: any }>({
    mutationFn: ({ id, data }) =>
      apiClient.patch(`/api/v1/questionnaires/versions/${id}`, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-versions", templateId] });
      message.success(t("version.updated"));
      setEditVersionId(null);
      setEditJson(null);
    },
  });

  const publishVersion = useAppMutation<void, string>({
    mutationFn: (id) =>
      apiClient.post(`/api/v1/questionnaires/versions/${id}/publish`, {
        published_by: "00000000-0000-0000-0000-000000000000",
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-versions", templateId] });
      message.success(t("version.published"));
    },
  });

  const retireVersion = useAppMutation<void, string>({
    mutationFn: (id) =>
      apiClient.post(`/api/v1/questionnaires/versions/${id}/retire`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-versions", templateId] });
      message.success(t("version.retired"));
    },
  });

  function handleOpenBuilder(version: Version) {
    setEditVersionId(version.id);
    setEditJson(version.surveyjs_schema);
  }

  function handleBuilderChange(json: Record<string, unknown>) {
    if (editVersionId) {
      setEditJson(json);
    } else {
      setBuilderJson(json);
    }
  }

  function handleSaveEdit() {
    if (editVersionId && editJson) {
      updateVersion.mutate({ id: editVersionId, data: { surveyjs_schema: editJson } });
    }
  }

  if (isLoading) return <SkeletonPage />;

  const handlePreview = (version: Version) => {
    setPreviewVersion(version);
    setPreviewOpen(true);
  };

  const statusClass: Record<string, string> = {
    draft: "status-default",
    published: "status-success",
    retired: "status-warning",
  };

  const columns = [
    { title: t("version.column.version"), dataIndex: "version_no", key: "version_no" },
    {
      title: t("version.column.language"),
      dataIndex: "language",
      key: "language",
      render: (l: string) => <Tag>{l}</Tag>,
    },
    {
      title: t("version.column.status"),
      dataIndex: "status",
      key: "status",
      render: (s: string) => <span className={`status ${statusClass[s] ?? "status-default"}`}>{s}</span>,
    },
    {
      title: t("version.column.published"),
      dataIndex: "published_at",
      key: "published_at",
      render: (d: string | null) =>
        d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: t("version.column.schemaHash"),
      dataIndex: "schema_hash",
      key: "schema_hash",
      render: (h: string) => (
        <Text copyable style={{ fontSize: 12, fontFamily: "var(--font-mono)" }}>
          {h.slice(0, 12)}...
        </Text>
      ),
    },
    {
      title: t("version.column.actions"),
      key: "actions",
      render: (_: unknown, r: Version) => (
        <Space>
          <Button size="small" onClick={() => handlePreview(r)}>
            {t("version.action.preview")}
          </Button>
          {r.status === "draft" && (
            <>
              <Button
                size="small"
                onClick={() => handleOpenBuilder(r)}
              >
                {t("version.action.edit")}
              </Button>
              <Button
                size="small"
                type="primary"
                onClick={() => publishVersion.mutate(r.id)}
              >
                {t("version.action.publish")}
              </Button>
            </>
          )}
          {r.status === "published" && (
            <Button
              size="small"
              onClick={() => retireVersion.mutate(r.id)}
            >
              {t("version.action.retire")}
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <Space>
          <Button onClick={() => navigate("/app/questionnaires/templates")}>
            {t("version.back")}
          </Button>
          <Title level={4} style={{ marginTop: 0, marginBottom: 0 }}>
            {t("version.management")}
          </Title>
        </Space>
        <Button
          type="primary"
          onClick={() => setCreateOpen(true)}
        >
          {t("version.new")}
        </Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={versions ?? []}
          columns={columns}
          rowKey="id"
          pagination={false}
          locale={{ emptyText: <Empty description={t("version.empty")} /> }}
        />
      </Card>

      <Modal
        title={t("version.create")}
        open={createOpen}
        width={900}
        onCancel={() => {
          setCreateOpen(false);
          form.resetFields();
          setSurveyJson("");
          setBuilderJson({});
        }}
        onOk={() => form.submit()}
        confirmLoading={createVersion.isPending}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => {
            const schema = editMode === "builder" ? builderJson : (() => {
              try { return JSON.parse(surveyJson); } catch { return null; }
            })();
            if (!schema?.pages) {
              message.error(t("version.buildFirst"));
              return;
            }
            createVersion.mutate({
              ...values,
              surveyjs_schema: schema,
            });
          }}
        >
          <Form.Item
            name="version_no"
            label={t("version.form.versionNo")}
            rules={[{ required: true }]}
          >
            <Input placeholder={t("version.form.versionNoPlaceholder")} />
          </Form.Item>
          <Form.Item label={t("version.form.content")} required>
            <Tabs
              activeKey={editMode}
              onChange={(k) => setEditMode(k as "builder" | "json")}
              items={[
                {
                  key: "builder",
                  label: t("version.form.builder"),
                  children: (
                    <div style={{ border: "1px solid var(--border)", borderRadius: 6, overflow: "hidden" }}>
                      <QuestionnaireBuilder value={builderJson} onChange={setBuilderJson} />
                    </div>
                  ),
                },
                {
                  key: "json",
                  label: t("version.form.jsonEditor"),
                  children: (
                    <TextArea
                      rows={16}
                      value={surveyJson}
                      onChange={(e) => setSurveyJson(e.target.value)}
                      placeholder={t("version.form.jsonPlaceholder")}
                      style={{ fontFamily: "var(--font-mono)", fontSize: 12 }}
                    />
                  ),
                },
              ]}
            />
          </Form.Item>
          <Form.Item name="language" label={t("version.form.language")} initialValue="zh-CN">
            <Input placeholder={t("version.form.languagePlaceholder")} />
          </Form.Item>
        </Form>
      </Modal>

      {/* Edit Version Modal */}
      <Modal
        title={t("version.edit")}
        open={!!editVersionId}
        width={900}
        onCancel={() => {
          setEditVersionId(null);
          setEditJson(null);
        }}
        onOk={handleSaveEdit}
        confirmLoading={updateVersion.isPending}
      >
        {editJson && (
          <Tabs
            defaultActiveKey="builder"
            items={[
              {
                key: "builder",
                label: "Visual Builder",
                children: (
                  <div style={{ border: "1px solid var(--border)", borderRadius: 6, overflow: "hidden" }}>
                    <QuestionnaireBuilder value={editJson} onChange={handleBuilderChange} />
                  </div>
                ),
              },
              {
                key: "preview",
                label: t("version.preview"),
                children: (
                  <div style={{ maxWidth: 600, margin: "0 auto", padding: 16 }}>
                    <Survey model={new Model(editJson)} />
                  </div>
                ),
              },
            ]}
          />
        )}
      </Modal>

      <Modal
        title={t("version.preview") + ": " + (previewVersion?.version_no ?? "")}
        open={previewOpen}
        onCancel={() => {
          setPreviewOpen(false);
          setPreviewVersion(null);
        }}
        width={700}
        footer={null}
      >
        {previewVersion && (
          <Survey
            model={new Model(
              previewVersion.surveyjs_schema,
            )}
          />
        )}
      </Modal>
    </div>
  );
}
