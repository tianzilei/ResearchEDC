import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, Table, Tag, Button, Space, Typography, Modal, Form, Input, Select, DatePicker, message } from "antd";
import { PlusOutlined, CheckCircleOutlined, ArrowLeftOutlined } from "@ant-design/icons";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface StudyEvent {
  studyEventId: number;
  studySubjectId: number;
  studyEventDefinitionId: number;
  location: string | null;
  dateStart: string | null;
  dateEnd: string | null;
  statusId: number;
  subjectEventStatusId: number;
}

interface EventDefinition {
  studyEventDefinitionId: number;
  studyId: number;
  name: string;
  description: string | null;
  repeating: boolean | null;
  type: string | null;
}

export default function EventList() {
  const { subjectId } = useParams<{ subjectId: string }>();
  const navigate = useNavigate();
  const [events, setEvents] = useState<StudyEvent[]>([]);
  const [definitions, setDefinitions] = useState<EventDefinition[]>([]);
  const [loading, setLoading] = useState(true);
  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [form] = Form.useForm();

  useState(() => {
    if (!subjectId) return;
    Promise.all([
      fetch(`/api/v1/events/by-subject?studySubjectId=${subjectId}`).then(r => r.ok ? r.json() : []),
    ]).then(([eventData]) => {
      setEvents(eventData);
      setLoading(false);
    }).catch(() => setLoading(false));
  });

  const handleSchedule = async () => {
    try {
      const vals = await form.validateFields();
      const res = await fetch("/api/v1/events", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          studySubjectId: Number(subjectId),
          studyEventDefinitionId: vals.definitionId,
          location: vals.location,
          startDate: vals.startDate?.toISOString?.() ?? null,
          endDate: vals.endDate?.toISOString?.() ?? null,
          statusId: 1,
          subjectEventStatusId: 1,
        }),
      });
      if (!res.ok) { message.error("Failed to schedule event"); return; }
      message.success("Event scheduled");
      setScheduleOpen(false);
      form.resetFields();
      const r = await fetch(`/api/v1/events/by-subject?studySubjectId=${subjectId}`);
      if (r.ok) setEvents(await r.json());
    } catch { void 0; }
  };

  const handleComplete = async (eventId: number) => {
    const res = await fetch(`/api/v1/events/${eventId}/complete`, { method: "POST" });
    if (!res.ok) { message.error("Failed to complete event"); return; }
    message.success("Event completed");
    const r = await fetch(`/api/v1/events/by-subject?studySubjectId=${subjectId}`);
    if (r.ok) setEvents(await r.json());
  };

  const openSchedule = async () => {
    const r = await fetch(`/api/v1/events/definitions?studyId=0`);
    if (r.ok) setDefinitions(await r.json());
    setScheduleOpen(true);
  };

  if (loading) return <SkeletonPage />;

  const statusColors: Record<number, string> = {
    1: "blue", 2: "orange", 3: "purple", 4: "cyan",
    5: "geekblue", 6: "red", 7: "green",
  };

  const statusLabels: Record<number, string> = {
    1: "Initial", 2: "Scheduled", 3: "Data Entry Started",
    4: "Completed", 5: "Locked", 6: "Stopped", 7: "Signed",
  };

  const columns = [
    { title: "Event ID", dataIndex: "studyEventId", key: "id", width: 80 },
    {
      title: "Definition ID", dataIndex: "studyEventDefinitionId", key: "def",
      render: (v: number) => `Def #${v}`,
    },
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
      render: (_: any, record: StudyEvent) => (
        <Tag color={statusColors[record.statusId] ?? "default"}>
          {statusLabels[record.statusId] ?? `Status ${record.statusId}`}
        </Tag>
      ),
    },
    {
      title: "", key: "actions",
      render: (_: any, record: StudyEvent) => (
        record.statusId < 7 ? (
          <Button
            size="small"
            icon={<CheckCircleOutlined />}
            onClick={() => handleComplete(record.studyEventId)}
          >
            Complete
          </Button>
        ) : null
      ),
    },
  ];

  return (
    <div style={{ padding: "24px 32px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>Back</Button>
          <div>
            <Title level={3} style={{ margin: 0 }}>Events</Title>
            <Text type="secondary">Subject #{subjectId} &middot; {events.length} events</Text>
          </div>
        </Space>
        <Button type="primary" icon={<PlusOutlined />} onClick={openSchedule}>
          Schedule Event
        </Button>
      </div>

      <Card
        style={{ borderRadius: 14, border: "1px solid var(--color-border-light, #E5E0D8)" }}
        styles={{ body: { padding: 0 } }}
      >
        <Table
          dataSource={events}
          columns={columns}
          rowKey="studyEventId"
          pagination={false}
          locale={{ emptyText: "No events scheduled for this subject" }}
        />
      </Card>

      <Modal
        title="Schedule Event"
        open={scheduleOpen}
        onOk={handleSchedule}
        onCancel={() => { setScheduleOpen(false); form.resetFields(); }}
        okText="Schedule"
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="definitionId" label="Event Definition" rules={[{ required: true }]}>
            <Select
              showSearch
              placeholder="Select event type"
              onFocus={async () => {
                const r = await fetch(`/api/v1/events/definitions?studyId=0`);
                if (r.ok) setDefinitions(await r.json());
              }}
            >
              {definitions.map(d => (
                <Select.Option key={d.studyEventDefinitionId} value={d.studyEventDefinitionId}>
                  {d.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="location" label="Location">
            <Input placeholder="e.g. Clinic A" />
          </Form.Item>
          <Form.Item name="startDate" label="Start Date">
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="endDate" label="End Date">
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
