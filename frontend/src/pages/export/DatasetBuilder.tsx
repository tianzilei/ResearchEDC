import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  Card,
  Table,
  Tag,
  Button,
  Typography,
  Space,
  Modal,
  Form,
  Input,
  Select,
  Spin,
  message,
  Empty,
  Breadcrumb,
} from "antd";
import { apiClient } from "@/api/client";

const { Title, Text } = Typography;

interface Dataset {
  datasetId: number;
  name: string;
  description: string;
  studyId: number;
  studyName: string;
  ownerId: number;
  status: string;
  dateCreated: string;
}

export default function DatasetBuilder() {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [datasets, setDatasets] = useState<Dataset[]>([]);
  const [studies, setStudies] = useState<{ studyId: number; name: string }[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = () => {
    setLoading(true);
    Promise.all([
      apiClient.get<Dataset[]>("/api/legacy/datasets").catch(() => []),
      apiClient.get<{ studyId?: number; id?: number; name: string }[]>("/api/v1/studies").catch(() => []),
    ]).then(([ds, ss]) => {
      setDatasets(ds);
      setStudies(Array.isArray(ss) ? ss.map((s) => ({ studyId: s.studyId ?? s.id ?? 0, name: s.name })) : []);
      setLoading(false);
    }).catch(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  const handleCreate = async () => {
    try {
      const vals = await form.validateFields();
      await apiClient.post<Dataset>("/api/legacy/datasets", undefined, {
        name: vals.name,
        studyId: vals.studyId,
      });
      message.success(t("dataset.created"));
      setCreateOpen(false);
      form.resetFields();
      fetchData();
    } catch {
      message.error(t("dataset.createFailed"));
    }
  };

  if (loading) {
    return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  }

  const columns = [
    { title: t("dataset.column.name"), dataIndex: "name", key: "name" },
    { title: t("dataset.column.description"), dataIndex: "description", key: "description", render: (v: string) => v || "-" },
    { title: t("dataset.column.study"), dataIndex: "studyName", key: "study", render: (v: string) => v || "-" },
    { title: t("dataset.column.status"), dataIndex: "status", key: "status", render: (s: string) => <Tag>{s || "available"}</Tag> },
    { title: t("dataset.column.created"), dataIndex: "dateCreated", key: "created", render: (v: string) => v ? new Date(v).toLocaleDateString() : "-" },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/data-export">{t("export.title")}</Link> },
          { title: t("dataset.title") },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16, borderRadius: 6 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>{t("dataset.title")}</Title>
              <Text type="secondary">{t("dataset.count", { count: datasets.length })}</Text>
            </div>
          </Space>
          <Space>
            <Button onClick={() => navigate("/app/data-export")}>
              {t("export.title")}
            </Button>
            <Button type="primary" onClick={() => setCreateOpen(true)}>
              {t("dataset.new")}
            </Button>
          </Space>
        </div>
      </Card>

      {datasets.length === 0 ? (
        <Card style={{ borderRadius: 6 }}><Empty description={t("dataset.empty")} /></Card>
      ) : (
        <Card style={{ borderRadius: 6 }} styles={{ body: { padding: 0 } }}>
          <Table dataSource={datasets} columns={columns} rowKey="datasetId" pagination={false} />
        </Card>
      )}

      <Modal
        title={t("dataset.create")}
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label={t("dataset.form.name")} rules={[{ required: true }]}>
            <Input placeholder={t("dataset.form.namePlaceholder")} />
          </Form.Item>
          <Form.Item name="studyId" label={t("dataset.form.study")} rules={[{ required: true }]}>
            <Select placeholder={t("dataset.form.studyPlaceholder")} showSearch>
              {studies.map((s) => (
                <Select.Option key={s.studyId} value={s.studyId}>{s.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
