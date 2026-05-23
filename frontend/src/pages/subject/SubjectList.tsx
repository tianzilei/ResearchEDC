import { useState } from "react";
import { Card, Table, Tag, Button, Space, Typography, Modal, Form, Input, Select, DatePicker, message, Tooltip } from "antd";
import { PlusOutlined, UserOutlined, ReloadOutlined, UnorderedListOutlined, AppstoreOutlined } from "@ant-design/icons";

import { useNavigate } from "react-router-dom";
import { useCurrentStudy } from "@/hooks/useStudies";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface StudySubject {
  studySubjectId: number;
  studyId: number;
  subjectId: number;
  label: string;
  secondaryLabel: string | null;
  ocOid: string | null;
  enrollmentDate: string | null;
  dateCreated: string;
  status?: string;
}

export default function SubjectList() {
  const navigate = useNavigate();
  const { currentStudy } = useCurrentStudy();
  const [loading, setLoading] = useState(true);
  const [subjects, setSubjects] = useState<StudySubject[]>([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();
  const [viewMode, setViewMode] = useState<"table" | "card">("table");

  useState(() => {
    if (!currentStudy?.id) return;
    fetch(`/api/v1/subjects/by-study?studyId=${currentStudy.id}`)
      .then(r => r.ok ? r.json() : [])
      .then(data => { setSubjects(data); setLoading(false); })
      .catch(() => setLoading(false));
  });

  const handleCreate = async () => {
    try {
      const vals = await form.validateFields();
      const subRes = await fetch("/api/v1/subjects", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          uniqueIdentifier: vals.uniqueIdentifier,
          gender: vals.gender,
          dateOfBirth: vals.dateOfBirth?.toISOString?.() ?? null,
        }),
      });
      if (!subRes.ok) { message.error("Failed to create subject"); return; }
      const newSubject = await subRes.json();

      const enrollRes = await fetch("/api/v1/subjects/enroll", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          studyId: currentStudy!.id,
          subjectId: newSubject.subjectId,
          label: vals.label ?? newSubject.uniqueIdentifier,
          enrollmentDate: vals.enrollmentDate?.toISOString?.() ?? null,
          eventDefinitionId: null,
        }),
      });
      if (!enrollRes.ok) { message.error("Failed to enroll subject"); return; }

      message.success("Subject created and enrolled");
      setCreateOpen(false);
      form.resetFields();
      const r = await fetch(`/api/v1/subjects/by-study?studyId=${currentStudy!.id}`);
      if (r.ok) setSubjects(await r.json());
    } catch { void 0; }
  };

  if (!currentStudy) {
    return (
      <div style={{ padding: 48, textAlign: "center" }}>
        <Text type="secondary">Select a study to manage subjects</Text>
      </div>
    );
  }

  if (loading) return <SkeletonPage />;

  const columns = [
    {
      title: "Label",
      dataIndex: "label",
      key: "label",
      render: (text: string, record: StudySubject) => (
        <a onClick={() => navigate(`/app/subjects/${record.studySubjectId}`)}>
          <UserOutlined style={{ marginRight: 8 }} />{text}
        </a>
      ),
    },
    { title: "Subject ID", dataIndex: "subjectId", key: "subjectId" },
    { title: "OC OID", dataIndex: "ocOid", key: "ocOid", render: (v: string) => v ?? "-" },
    {
      title: "Enrolled",
      dataIndex: "enrollmentDate",
      key: "enrollmentDate",
      render: (d: string) => d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "Status",
      key: "status",
      render: () => <Tag color="green">Active</Tag>,
    },
  ];

  return (
    <div style={{ padding: "24px 32px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <div>
          <Title level={3} style={{ margin: 0 }}>Subjects</Title>
          <Text type="secondary" style={{ marginTop: 4, display: "block" }}>
            {currentStudy.name} &middot; {subjects.length} enrolled
          </Text>
        </div>
        <Space>
          <Tooltip title="Toggle view">
            <Button
              icon={viewMode === "table" ? <AppstoreOutlined /> : <UnorderedListOutlined />}
              onClick={() => setViewMode(v => v === "table" ? "card" : "table")}
            />
          </Tooltip>
          <Button icon={<ReloadOutlined />} onClick={() => window.location.reload()}>
            Refresh
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
            Add Subject
          </Button>
        </Space>
      </div>

      <Card
        style={{ borderRadius: 14, border: "1px solid var(--color-border-light, #E5E0D8)" }}
        styles={{ body: { padding: 0 } }}
      >
        <Table
          dataSource={subjects}
          columns={columns}
          rowKey="studySubjectId"
          pagination={{ pageSize: 20, showTotal: (t) => `${t} subjects` }}
          locale={{ emptyText: "No subjects enrolled in this study" }}
        />
      </Card>

      <Modal
        title="Add Subject to Study"
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        okText="Create & Enroll"
        width={520}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="uniqueIdentifier" label="Subject ID" rules={[{ required: true, message: "Required" }]}>
            <Input placeholder="e.g. SUBJ-001" />
          </Form.Item>
          <Form.Item name="label" label="Study Label" rules={[{ required: true, message: "Required" }]}>
            <Input placeholder="e.g. SITE-A-001" />
          </Form.Item>
          <Form.Item name="gender" label="Gender">
            <Select allowClear>
              <Select.Option value="m">Male</Select.Option>
              <Select.Option value="f">Female</Select.Option>
              <Select.Option value="u">Unknown</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="dateOfBirth" label="Date of Birth">
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="enrollmentDate" label="Enrollment Date">
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
