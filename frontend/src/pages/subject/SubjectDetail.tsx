import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Card, Descriptions, Button, Space, Typography, Table,
  Modal, Form, Input, Select, message, Result,
} from "antd";

import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface StudySubjectDetail {
  studySubjectId: number;
  studyId: number;
  subjectId: number;
  label: string;
  secondaryLabel: string | null;
  ocOid: string | null;
  enrollmentDate: string | null;
  dateCreated: string;
  dateUpdated: string | null;
}

interface SubjectDetail {
  subjectId: number;
  uniqueIdentifier: string;
  dateOfBirth: string | null;
  gender: string | null;
  dateCreated: string;
}

interface StudyEvent {
  studyEventId: number;
  studySubjectId: number;
  studyEventDefinitionId: number;
  location: string | null;
  dateStart: string | null;
  dateEnd: string | null;
  statusId: number;
}

export default function SubjectDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [enrollment, setEnrollment] = useState<StudySubjectDetail | null>(null);
  const [subject, setSubject] = useState<SubjectDetail | null>(null);
  const [events, setEvents] = useState<StudyEvent[]>([]);

  const [signOpen, setSignOpen] = useState(false);
  const [updateOpen, setUpdateOpen] = useState(false);
  const [signForm] = Form.useForm();
  const [updateForm] = Form.useForm();

  const fetchData = useCallback(() => {
    if (!id) return;
    Promise.all([
      fetch(`/api/v1/subjects/enrollment/${id}`).then(r => r.ok ? r.json() : null),
      fetch(`/api/v1/events/by-subject?studySubjectId=${id}`).then(r => r.ok ? r.json() : []),
    ]).then(([enrollData, eventsData]) => {
      setEnrollment(enrollData);
      setEvents(eventsData);
      if (enrollData?.subjectId) {
        fetch(`/api/v1/subjects/${enrollData.subjectId}`)
          .then(r => r.ok ? r.json() : null)
          .then(setSubject);
      }
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [id]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleSign = async () => {
    try {
      const vals = await signForm.validateFields();
      const res = await fetch(`/api/legacy/subjects/${id}/sign`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(vals),
      });
      if (res.ok) {
        message.success("受试者已签名");
        setSignOpen(false);
        signForm.resetFields();
      } else {
        window.open(`/legacy/SignStudySubject?id=${id}`, "_blank");
        setSignOpen(false);
      }
    } catch { void 0; }
  };

  const handleUpdate = async () => {
    try {
      const vals = await updateForm.validateFields();
      const res = await fetch(`/api/v1/subjects/${enrollment?.subjectId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(vals),
      });
      if (res.ok) {
        message.success("Subject updated");
        setUpdateOpen(false);
        updateForm.resetFields();
        fetchData();
      } else {
        message.error("Failed to update subject");
      }
    } catch { void 0; }
  };

  if (loading) return <SkeletonPage />;
  if (!enrollment) {
    return (
      <Result
        status="404"
        title="未找到"
        subTitle="未找到受试者入组信息"
        extra={<Button onClick={() => navigate("/app/subjects")}>返回受试者列表</Button>}
      />
    );
  }

  const eventColumns = [
    { title: "事件 ID", dataIndex: "studyEventId", key: "id" },
    { title: "定义", dataIndex: "studyEventDefinitionId", key: "def" },
    { title: "地点", dataIndex: "location", key: "location", render: (v: string) => v || "-" },
    {
      title: "开始", dataIndex: "dateStart", key: "start",
      render: (d: string) => d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "结束", dataIndex: "dateEnd", key: "end",
      render: (d: string) => d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "状态", key: "status",
      render: () => <span className="status status-info">已安排</span>,
    },
    {
      title: "", key: "actions",
      render: (_: any, record: StudyEvent) => (
        <Button size="small"
          onClick={() => navigate(`/app/subjects/${id}/events/${record.studyEventId}/crfs`)}>
          CRF
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Card style={{ marginBottom: 16, borderRadius: "var(--radius-lg)" }} styles={{ body: { padding: "16px 24px" } }}>
        <Space style={{ width: "100%", justifyContent: "space-between" }} align="center">
          <Space>

            <div>
              <Title level={4} style={{ margin: 0 }}>{enrollment.label}</Title>
              <Text type="secondary">{subject?.uniqueIdentifier ?? `受试者 #${enrollment.subjectId}`}</Text>
            </div>
          </Space>
          <Space>
            <Button onClick={() => setSignOpen(true)}>签名</Button>
            <Button onClick={() => { updateForm.setFieldsValue(subject); setUpdateOpen(true); }}>
              编辑
            </Button>
            <Button
              onClick={() => window.open(`/legacy/ReassignStudySubject?id=${id}`, "_blank")}>
              重新分配
            </Button>
            <Button onClick={() => navigate("/app/subjects")}>返回</Button>
          </Space>
        </Space>
      </Card>

      <Card style={{ marginBottom: 16, borderRadius: "var(--radius-lg)" }}>
        <Descriptions column={2} size="small" bordered>
          <Descriptions.Item label="研究标签">{enrollment.label}</Descriptions.Item>
          <Descriptions.Item label="受试者 ID">{enrollment.subjectId}</Descriptions.Item>
          <Descriptions.Item label="OC OID">{enrollment.ocOid ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="性别">
            {subject?.gender === "m" ? "男" : subject?.gender === "f" ? "女" : "未指定"}
          </Descriptions.Item>
          <Descriptions.Item label="出生日期">
            {subject?.dateOfBirth ? new Date(subject.dateOfBirth).toLocaleDateString() : "-"}
          </Descriptions.Item>
          <Descriptions.Item label="入组日期">
            {enrollment.enrollmentDate ? new Date(enrollment.enrollmentDate).toLocaleDateString() : "-"}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Card
        title="研究访视"
        style={{ borderRadius: "var(--radius-lg)" }}
        styles={{ body: { padding: 0 } }}
        extra={
          <Button type="primary" size="small"
            onClick={() => window.open(`/legacy/CreateNewStudyEvent?studySubjectId=${id}`, "_blank")}>
            安排访视
          </Button>
        }
      >
        <Table dataSource={events} columns={eventColumns} rowKey="studyEventId" pagination={false}
          locale={{ emptyText: "暂无安排访视" }} />
      </Card>

      <Modal title="签署受试者数据" open={signOpen}
        onOk={handleSign} onCancel={() => setSignOpen(false)}>
        <Form form={signForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="reason" label="签署原因" rules={[{ required: true }]}>
            <Input.TextArea rows={3} placeholder="本人确认该数据完整准确" />
          </Form.Item>
          <Form.Item name="password" label="密码">
            <Input.Password placeholder="重新输入密码确认" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="更新受试者" open={updateOpen}
        onOk={handleUpdate} onCancel={() => setUpdateOpen(false)}>
        <Form form={updateForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="gender" label="性别">
            <Select allowClear>
              <Select.Option value="m">男</Select.Option>
              <Select.Option value="f">女</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="dateOfBirth" label="出生日期">
            <Input placeholder="YYYY-MM-DD" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
