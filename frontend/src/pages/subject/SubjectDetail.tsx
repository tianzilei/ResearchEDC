import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, Descriptions, Tag, Button, Space, Typography, Table } from "antd";
import { ArrowLeftOutlined, UserOutlined, CalendarOutlined } from "@ant-design/icons";
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

  useState(() => {
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
  });

  if (loading) return <SkeletonPage />;
  if (!enrollment) {
    return (
      <div style={{ padding: 48, textAlign: "center" }}>
        <Text type="secondary">Subject enrollment not found</Text>
      </div>
    );
  }

  const eventColumns = [
    { title: "Event ID", dataIndex: "studyEventId", key: "id" },
    { title: "Definition", dataIndex: "studyEventDefinitionId", key: "def" },
    { title: "Location", dataIndex: "location", key: "location", render: (v: string) => v || "-" },
    { title: "Start", dataIndex: "dateStart", key: "start", render: (d: string) => d ? new Date(d).toLocaleDateString() : "-" },
    { title: "End", dataIndex: "dateEnd", key: "end", render: (d: string) => d ? new Date(d).toLocaleDateString() : "-" },
    {
      title: "Status", key: "status",
      render: () => <Tag color="blue">Scheduled</Tag>,
    },
  ];

  return (
    <div style={{ padding: "24px 32px", maxWidth: 960 }}>
      <Space style={{ marginBottom: 24 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>Back</Button>
      </Space>

      <Card style={{ borderRadius: 14, border: "1px solid var(--color-border-light, #E5E0D8)", marginBottom: 24 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 24 }}>
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
        </div>

        <Descriptions column={2} size="small" bordered>
          <Descriptions.Item label="Study Label">{enrollment.label}</Descriptions.Item>
          <Descriptions.Item label="Subject ID">{enrollment.subjectId}</Descriptions.Item>
          <Descriptions.Item label="OC OID">{enrollment.ocOid || "-"}</Descriptions.Item>
          <Descriptions.Item label="Gender">
            {subject?.gender === "m" ? "Male" : subject?.gender === "f" ? "Female" : "Not specified"}
          </Descriptions.Item>
          <Descriptions.Item label="Date of Birth">
            {subject?.dateOfBirth ? new Date(subject.dateOfBirth).toLocaleDateString() : "-"}
          </Descriptions.Item>
          <Descriptions.Item label="Enrolled">
            {enrollment.enrollmentDate ? new Date(enrollment.enrollmentDate).toLocaleDateString() : "-"}
          </Descriptions.Item>
          <Descriptions.Item label="Created">
            {enrollment.dateCreated ? new Date(enrollment.dateCreated).toLocaleDateString() : "-"}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Card
        title={<span><CalendarOutlined style={{ marginRight: 8 }} />Study Events</span>}
        style={{ borderRadius: 14, border: "1px solid var(--color-border-light, #E5E0D8)" }}
        styles={{ body: { padding: 0 } }}
        extra={
          <Button type="primary" size="small" onClick={() => navigate(`/app/events/schedule?subject=${id}`)}>
            Schedule Event
          </Button>
        }
      >
        <Table
          dataSource={events}
          columns={eventColumns}
          rowKey="studyEventId"
          pagination={false}
          locale={{ emptyText: "No events scheduled for this subject" }}
        />
      </Card>
    </div>
  );
}
