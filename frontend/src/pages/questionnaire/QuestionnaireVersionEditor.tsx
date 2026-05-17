import { useState } from "react";
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
import {
  FileAddOutlined,
  CheckCircleOutlined,
  StopOutlined,
  EyeOutlined,
  CodeOutlined,
  BuildOutlined,
  EditOutlined,
} from "@ant-design/icons";
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
      message.success("Version created");
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
      message.success("Version updated");
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
      message.success("Version published");
    },
  });

  const retireVersion = useAppMutation<void, string>({
    mutationFn: (id) =>
      apiClient.post(`/api/v1/questionnaires/versions/${id}/retire`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-versions", templateId] });
      message.success("Version retired");
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

  const statusColor: Record<string, string> = {
    draft: "default",
    published: "success",
    retired: "warning",
  };

  const columns = [
    { title: "Version", dataIndex: "version_no", key: "version_no" },
    {
      title: "Language",
      dataIndex: "language",
      key: "language",
      render: (l: string) => <Tag>{l}</Tag>,
    },
    {
      title: "Status",
      dataIndex: "status",
      key: "status",
      render: (s: string) => <Tag color={statusColor[s]}>{s}</Tag>,
    },
    {
      title: "Published",
      dataIndex: "published_at",
      key: "published_at",
      render: (d: string | null) =>
        d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "Schema Hash",
      dataIndex: "schema_hash",
      key: "schema_hash",
      render: (h: string) => (
        <Text copyable style={{ fontSize: 11, fontFamily: "monospace" }}>
          {h.slice(0, 12)}...
        </Text>
      ),
    },
    {
      title: "Actions",
      key: "actions",
      render: (_: unknown, r: Version) => (
        <Space>
          <Button size="small" icon={<EyeOutlined />} onClick={() => handlePreview(r)}>
            Preview
          </Button>
          {r.status === "draft" && (
            <>
              <Button
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleOpenBuilder(r)}
              >
                Edit
              </Button>
              <Button
                size="small"
                type="primary"
                icon={<CheckCircleOutlined />}
                onClick={() => publishVersion.mutate(r.id)}
              >
                Publish
              </Button>
            </>
          )}
          {r.status === "published" && (
            <Button
              size="small"
              icon={<StopOutlined />}
              onClick={() => retireVersion.mutate(r.id)}
            >
              Retire
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
            Back
          </Button>
          <Title level={4} style={{ marginTop: 0, marginBottom: 0 }}>
            <CodeOutlined /> Version Management
          </Title>
        </Space>
        <Button
          type="primary"
          icon={<FileAddOutlined />}
          onClick={() => setCreateOpen(true)}
        >
          New Version
        </Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={versions ?? []}
          columns={columns}
          rowKey="id"
          pagination={false}
          locale={{ emptyText: <Empty description="No versions yet" /> }}
        />
      </Card>

      <Modal
        title="Create New Version"
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
            if (!schema || !schema.pages) {
              message.error("Please build the questionnaire first");
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
            label="Version Number"
            rules={[{ required: true }]}
          >
            <Input placeholder="e.g., 1.0.0" />
          </Form.Item>
          <Form.Item label="Questionnaire Content" required>
            <Tabs
              activeKey={editMode}
              onChange={(k) => setEditMode(k as "builder" | "json")}
              items={[
                {
                  key: "builder",
                  label: <span><BuildOutlined /> Visual Builder</span>,
                  children: (
                    <div style={{ border: "1px solid #d9d9d9", borderRadius: 6, overflow: "hidden" }}>
                      <QuestionnaireBuilder value={builderJson} onChange={setBuilderJson} />
                    </div>
                  ),
                },
                {
                  key: "json",
                  label: <span><CodeOutlined /> JSON Editor</span>,
                  children: (
                    <TextArea
                      rows={16}
                      value={surveyJson}
                      onChange={(e) => setSurveyJson(e.target.value)}
                      placeholder="Paste SurveyJS JSON here..."
                      style={{ fontFamily: "monospace", fontSize: 12 }}
                    />
                  ),
                },
              ]}
            />
          </Form.Item>
          <Form.Item name="language" label="Language" initialValue="zh-CN">
            <Input placeholder="zh-CN" />
          </Form.Item>
        </Form>
      </Modal>

      {/* Edit Version Modal */}
      <Modal
        title="Edit Version"
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
                label: <span><BuildOutlined /> Visual Builder</span>,
                children: (
                  <div style={{ border: "1px solid #d9d9d9", borderRadius: 6, overflow: "hidden" }}>
                    <QuestionnaireBuilder value={editJson} onChange={handleBuilderChange} />
                  </div>
                ),
              },
              {
                key: "preview",
                label: <span><EyeOutlined /> Preview</span>,
                children: (
                  <div style={{ maxWidth: 600, margin: "0 auto", padding: 16 }}>
                    <Survey model={new Model(editJson as Record<string, unknown>)} />
                  </div>
                ),
              },
            ]}
          />
        )}
      </Modal>

      <Modal
        title={`Preview: ${previewVersion?.version_no ?? ""}`}
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
