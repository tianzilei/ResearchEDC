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
  Select,
  Tag,
  message,
  Empty,
} from "antd";
import {
  FileTextOutlined,
  PlusOutlined,
  EyeOutlined,
} from "@ant-design/icons";
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

const categoryColors: Record<string, string> = {
  SLEEP: "geekblue",
  ANXIETY: "volcano",
  DEPRESSION: "purple",
  COGNITION: "cyan",
  SAFETY: "red",
  QUALITY_OF_LIFE: "green",
};

function useTemplates(studyId: number) {
  return useAppQuery<Template[]>({
    queryKey: ["questionnaire-templates", studyId],
    queryFn: () =>
      apiClient.get<Template[]>("/api/v1/questionnaires/templates", {
        studyId: studyId > 0 ? studyId : undefined,
      }),
    enabled: true,
  });
}

export default function QuestionnaireTemplates() {
  const navigate = useNavigate();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id ?? 0;
  const qc = useQueryClient();
  const { data: templates, isLoading } = useTemplates(studyId);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const createTemplate = useAppMutation<Template, Template>({
    mutationFn: (body) =>
      apiClient.post<Template>("/api/v1/questionnaires/templates", body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["questionnaire-templates"] });
      message.success("Template created");
      setModalOpen(false);
      form.resetFields();
    },
  });

  if (isLoading) return <SkeletonPage />;

  const columns = [
    {
      title: "Code",
      dataIndex: "code",
      key: "code",
      render: (code: string) => <Tag color="blue">{code}</Tag>,
    },
    { title: "Name", dataIndex: "name", key: "name" },
    {
      title: "Category",
      dataIndex: "category",
      key: "category",
      render: (cat: string | null) =>
        cat ? <Tag color={categoryColors[cat] ?? "default"}>{cat}</Tag> : "-",
    },
    {
      title: "Status",
      dataIndex: "status",
      key: "status",
      render: (s: string) => (
        <Tag color={s === "active" ? "success" : "default"}>{s}</Tag>
      ),
    },
    {
      title: "Updated",
      dataIndex: "updated_at",
      key: "updated_at",
      render: (d: string) => (d ? new Date(d).toLocaleDateString() : "-"),
    },
    {
      title: "Actions",
      key: "actions",
      render: (_: unknown, r: Template) => (
        <Button
          size="small"
          icon={<EyeOutlined />}
          onClick={() => navigate(`/app/questionnaires/templates/${r.id}/versions`)}
        >
          Versions
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <Title level={4} style={{ marginTop: 0 }}>
          <FileTextOutlined /> Questionnaire Templates
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          New Template
        </Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={templates ?? []}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 20 }}
          locale={{ emptyText: <Empty description="No templates yet" /> }}
        />
      </Card>

      <Modal
        title="Create Questionnaire Template"
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
              study_id: studyId > 0 ? studyId : null,
            });
          }}
        >
          <Form.Item name="code" label="Code" rules={[{ required: true }]}>
            <Input placeholder="e.g., ISI, GAD7, PHQ9" />
          </Form.Item>
          <Form.Item name="name" label="Name" rules={[{ required: true }]}>
            <Input placeholder="e.g., Insomnia Severity Index" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="category" label="Category">
            <Select allowClear>
              <Select.Option value="SLEEP">Sleep</Select.Option>
              <Select.Option value="ANXIETY">Anxiety</Select.Option>
              <Select.Option value="DEPRESSION">Depression</Select.Option>
              <Select.Option value="SAFETY">Safety</Select.Option>
              <Select.Option value="QUALITY_OF_LIFE">Quality of Life</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
