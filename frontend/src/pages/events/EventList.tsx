import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, Table, Button, Space, Typography, Modal, Form, Input, Select, DatePicker, message } from "antd";
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

  const statusClassMap: Record<number, string> = {
    1: "status-info", 2: "status-warning", 3: "status-warning", 4: "status-success",
    5: "status-default", 6: "status-danger", 7: "status-success",
  };

  const statusLabels: Record<number, string> = {
    1: "初始", 2: "已安排", 3: "数据录入中",
    4: "已完成", 5: "已锁定", 6: "已停止", 7: "已签名",
  };

  const columns = [
    { title: "事件 ID", dataIndex: "studyEventId", key: "id", width: 80 },
    {
      title: "定义 ID", dataIndex: "studyEventDefinitionId", key: "def",
      render: (v: number) => `定义 #${v}`,
    },
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
      render: (_: any, record: StudyEvent) => (
        <span className={`status ${statusClassMap[record.statusId] ?? "status-default"}`}>
          {statusLabels[record.statusId] ?? `状态 ${record.statusId}`}
        </span>
      ),
    },
    {
      title: "", key: "actions",
      render: (_: any, record: StudyEvent) => (
        record.statusId < 7 ? (
          <Button
            size="small"
            onClick={() => handleComplete(record.studyEventId)}
          >
            完成
          </Button>
        ) : null
      ),
    },
  ];

  return (
    <div style={{ padding: "24px 32px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <Space>
          <Button onClick={() => navigate(-1)}>返回</Button>
          <div>
            <Title level={3} style={{ margin: 0 }}>访视事件</Title>
            <Text type="secondary">受试者 #{subjectId} &middot; {events.length} 个事件</Text>
          </div>
        </Space>
        <Button type="primary" onClick={openSchedule}>
          安排访视
        </Button>
      </div>

      <Card
        style={{ borderRadius: "var(--radius-lg)", border: "1px solid var(--border)" }}
        styles={{ body: { padding: 0 } }}
      >
        <Table
          dataSource={events}
          columns={columns}
          rowKey="studyEventId"
          pagination={false}
          locale={{ emptyText: "该受试者暂无安排访视" }}
        />
      </Card>

      <Modal
        title="安排访视"
        open={scheduleOpen}
        onOk={handleSchedule}
        onCancel={() => { setScheduleOpen(false); form.resetFields(); }}
        okText="安排"
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="definitionId" label="事件定义" rules={[{ required: true }]}>
            <Select
              showSearch
              placeholder="选择事件类型"
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
          <Form.Item name="location" label="地点">
            <Input placeholder="例如：A 诊所" />
          </Form.Item>
          <Form.Item name="startDate" label="开始日期">
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="endDate" label="结束日期">
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
