import { useState } from "react";
import { Card, Table, Button, Space, Typography, Modal, Form, Input, Select, DatePicker, message } from "antd";
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
      if (!subRes.ok) { message.error("创建受试者失败"); return; }
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
      if (!enrollRes.ok) { message.error("入组失败"); return; }

      message.success("受试者已创建并入组");
      setCreateOpen(false);
      form.resetFields();
      const r = await fetch(`/api/v1/subjects/by-study?studyId=${currentStudy!.id}`);
      if (r.ok) setSubjects(await r.json());
    } catch { /* form validation error */ }
  };

  if (!currentStudy) {
    return (
      <div style={{ padding: 48, textAlign: "center" }}>
        <Text style={{ color: "var(--text-secondary)" }}>请先选择一个项目来管理受试者</Text>
      </div>
    );
  }

  if (loading) return <SkeletonPage />;

  const columns = [
    {
      title: "编号",
      dataIndex: "label",
      key: "label",
      render: (text: string, record: StudySubject) => (
        <a onClick={() => navigate(`/app/subjects/${record.studySubjectId}`)}>
          {text}
        </a>
      ),
    },
    { title: "受试者 ID", dataIndex: "subjectId", key: "subjectId" },
    { title: "OC OID", dataIndex: "ocOid", key: "ocOid", render: (v: string | null) => v ?? "-" },
    {
      title: "入组日期",
      dataIndex: "enrollmentDate",
      key: "enrollmentDate",
      render: (d: string | null) => d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "状态",
      key: "status",
      render: () => <span className="status status-success">正常</span>,
    },
  ];

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <div>
          <Title level={4} style={{ margin: 0 }}>受试者</Title>
          <Text style={{ color: "var(--text-secondary)", marginTop: 2, display: "block", fontSize: 13 }}>
            {currentStudy.name} · {subjects.length} 名已入组
          </Text>
        </div>
        <Space>
          <Button onClick={() => window.location.reload()}>
            刷新
          </Button>
          <Button type="primary" onClick={() => setCreateOpen(true)}>
            添加受试者
          </Button>
        </Space>
      </div>

      <Card styles={{ body: { padding: 0 } }}>
        <Table
          dataSource={subjects}
          columns={columns}
          rowKey="studySubjectId"
          pagination={{ pageSize: 20, showTotal: (t) => `共 ${t} 名` }}
          locale={{ emptyText: "该项目暂无受试者" }}
        />
      </Card>

      <Modal
        title="添加受试者到项目"
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        okText="创建并入组"
        width={480}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="uniqueIdentifier" label="受试者 ID" rules={[{ required: true, message: "必填" }]}>
            <Input placeholder="例如 SUBJ-001" />
          </Form.Item>
          <Form.Item name="label" label="研究编号" rules={[{ required: true, message: "必填" }]}>
            <Input placeholder="例如 SITE-A-001" />
          </Form.Item>
          <Form.Item name="gender" label="性别">
            <Select allowClear>
              <Select.Option value="m">男</Select.Option>
              <Select.Option value="f">女</Select.Option>
              <Select.Option value="u">未知</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="dateOfBirth" label="出生日期">
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="enrollmentDate" label="入组日期">
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}