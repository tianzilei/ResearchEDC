import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Card, Descriptions, Tag, Button, Space, Typography, Table,
  Modal, Form, Input, Select, message, Result,
} from "antd";
import {
  ArrowLeftOutlined, UserOutlined, CalendarOutlined,
  EditOutlined, CheckCircleOutlined, SwapOutlined, FileTextOutlined,
} from "@ant-design/icons";
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
        message.success("Subject signed successfully");
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
        title="Not Found"
        subTitle="Subject enrollment not found"
        extra={<Button onClick={() => navigate("/app/subjects")}>Back to Subjects</Button>}
      />
    );
  }

  const eventColumns = [
    { title: "Event ID", dataIndex: "studyEventId", key: "id" },
    { title: "Definition", dataIndex: "studyEventDefinitionId", key: "def" },
    { title: "Location", dataIndex: "location", key: "location", render: (v: string) => v || "-" },
    {
      title: "Start", dataIndex: "dateStart", key: "start",
      render: (d: string) => d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "End", dataIndex: "dateEnd", key: "end",
      render: (d: string) => d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "Status", key: "status",
      render: () => <Tag color="blue">Scheduled</Tag>,
    },
    {
      title: "", key: "actions",
      render: (_: any, record: StudyEvent) => (
        <Button size="small" icon={<FileTextOutlined />}
          onClick={() => navigate(`/app/subjects/${id}/events/${record.studyEventId}/crfs`)}>
          CRFs
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <Space style={{ width: "100%", justifyContent: "space-between" }} align="center">
          <Space>
            <div style={{
              width: 48, height: 48, borderRadius: 12,
              background: "linear-gradient(135deg, #099A87, #0B7A6B)",
              display: "flex", alignItems: "center", justifyContent: "center",
            }}>
              <UserOutlined style={{ fontSize: 22, color: "#fff" }} />
            </div>
            <div>
              <Title level={4} style={{ margin: 0 }}>{enrollment.label}</Title>
              <Text type="secondary">{subject?.uniqueIdentifier ?? `Subject #${enrollment.subjectId}`}</Text>
            </div>
          </Space>
          <Space>
            <Button icon={<CheckCircleOutlined />} onClick={() => setSignOpen(true)}>Sign</Button>
            <Button icon={<EditOutlined />} onClick={() => { updateForm.setFieldsValue(subject); setUpdateOpen(true); }}>
              Edit
            </Button>
            <Button icon={<SwapOutlined />}
              onClick={() => window.open(`/legacy/ReassignStudySubject?id=${id}`, "_blank")}>
              Reassign
            </Button>
            <Button icon={<ArrowLeftOutlined />} onClick={() => navigate("/app/subjects")}>Back</Button>
          </Space>
        </Space>
      </Card>

      <Card style={{ marginBottom: 16, borderRadius: 14 }}>
        <Descriptions column={2} size="small" bordered>
          <Descriptions.Item label="Study Label">{enrollment.label}</Descriptions.Item>
          <Descriptions.Item label="Subject ID">{enrollment.subjectId}</Descriptions.Item>
          <Descriptions.Item label="OC OID">{enrollment.ocOid ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Gender">
            {subject?.gender === "m" ? "Male" : subject?.gender === "f" ? "Female" : "Not specified"}
          </Descriptions.Item>
          <Descriptions.Item label="Date of Birth">
            {subject?.dateOfBirth ? new Date(subject.dateOfBirth).toLocaleDateString() : "-"}
          </Descriptions.Item>
          <Descriptions.Item label="Enrolled">
            {enrollment.enrollmentDate ? new Date(enrollment.enrollmentDate).toLocaleDateString() : "-"}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Card
        title={<span><CalendarOutlined style={{ marginRight: 8 }} />Study Events</span>}
        style={{ borderRadius: 14 }}
        styles={{ body: { padding: 0 } }}
        extra={
          <Button type="primary" size="small"
            onClick={() => window.open(`/legacy/CreateNewStudyEvent?studySubjectId=${id}`, "_blank")}>
            Schedule Event
          </Button>
        }
      >
        <Table dataSource={events} columns={eventColumns} rowKey="studyEventId" pagination={false}
          locale={{ emptyText: "No events scheduled" }} />
      </Card>

      <Modal title="Sign Subject Data" open={signOpen}
        onOk={handleSign} onCancel={() => setSignOpen(false)}>
        <Form form={signForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="reason" label="Reason for Signing" rules={[{ required: true }]}>
            <Input.TextArea rows={3} placeholder="I attest that this data is complete and accurate" />
          </Form.Item>
          <Form.Item name="password" label="Password">
            <Input.Password placeholder="Re-enter password to confirm" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="Update Subject" open={updateOpen}
        onOk={handleUpdate} onCancel={() => setUpdateOpen(false)}>
        <Form form={updateForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="gender" label="Gender">
            <Select allowClear>
              <Select.Option value="m">Male</Select.Option>
              <Select.Option value="f">Female</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="dateOfBirth" label="Date of Birth">
            <Input placeholder="YYYY-MM-DD" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
