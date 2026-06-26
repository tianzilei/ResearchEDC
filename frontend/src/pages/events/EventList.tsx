import { useState, useCallback } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
  Card, Table, Button, Space, Typography, Modal, Form, Input, Select, DatePicker, message, List, Spin,
} from "antd";
import { SkeletonPage } from "@/components/SkeletonCard";
import {
  useSubjectEvents,
  useEventDefinitions,
  useScheduleEvent,
  useCompleteEvent,
  useEventCrfs,
} from "@/hooks/useEvents";
import { useCurrentStudy } from "@/hooks/useStudies";
import type { StudyEventDTO, EventCrfDTO } from "@/types/event";

const { Title, Text } = Typography;

const STATUS_CLASS_MAP: Record<number, string> = {
  1: "status-info",
  2: "status-warning",
  3: "status-warning",
  4: "status-success",
  5: "status-default",
  6: "status-danger",
  7: "status-success",
};

const STATUS_LABELS: Record<number, string> = {
  1: "初始",
  2: "已安排",
  3: "数据录入中",
  4: "已完成",
  5: "已锁定",
  6: "已停止",
  7: "已签名",
};

function EventCrfRow({
  eventId,
  subjectId,
}: {
  eventId: number;
  subjectId: string;
}) {
  const navigate = useNavigate();
  const { data: crfs = [], isLoading } = useEventCrfs(eventId);

  if (isLoading) {
    return <Spin size="small" style={{ margin: "12px 16px" }} />;
  }

  if (crfs.length === 0) {
    return (
      <Text type="secondary" style={{ display: "block", padding: "12px 16px" }}>
        No CRFs assigned to this event
      </Text>
    );
  }

  return (
    <List
      size="small"
      dataSource={crfs}
      renderItem={(crf: EventCrfDTO) => (
        <List.Item
          style={{ paddingInline: 16 }}
          actions={[
            <Button
              key="enter"
              type="link"
              size="small"
              onClick={() =>
                navigate(
                  `/app/subjects/${subjectId}/events/${eventId}/crfs/${crf.eventCrfId}/entry`,
                )
              }
            >
              Enter Data
            </Button>,
          ]}
        >
          <List.Item.Meta
            title={
              <Space>
                <span>CRF #{crf.eventCrfId}</span>
                <span
                  className={`status ${STATUS_CLASS_MAP[crf.statusId] ?? "status-default"}`}
                >
                  {STATUS_LABELS[crf.statusId] ?? `Status ${crf.statusId}`}
                </span>
              </Space>
            }
            description={
              crf.dateInterviewed
                ? `Interviewed: ${new Date(crf.dateInterviewed).toLocaleDateString()}`
                : "Not yet started"
            }
          />
        </List.Item>
      )}
    />
  );
}

export default function EventList() {
  const { subjectId } = useParams<{ subjectId: string }>();
  const navigate = useNavigate();
  const studySubjectId = subjectId ? Number(subjectId) : undefined;

  const { currentStudy } = useCurrentStudy();
  const { data: events = [], isLoading: loadingEvents } =
    useSubjectEvents(studySubjectId);
  const { data: definitions = [] } = useEventDefinitions(currentStudy?.id);
  const scheduleMutation = useScheduleEvent();
  const completeMutation = useCompleteEvent();

  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [form] = Form.useForm();

  const handleSchedule = useCallback(async () => {
    if (!studySubjectId) return;
    try {
      const vals = await form.validateFields();
      await scheduleMutation.mutateAsync({
        studySubjectId,
        studyEventDefinitionId: vals.definitionId,
        ordinal: 0,
        location: vals.location ?? "",
        startDate: vals.startDate?.toISOString?.() ?? null,
        endDate: vals.endDate?.toISOString?.() ?? null,
      });
      message.success("Event scheduled");
      setScheduleOpen(false);
      form.resetFields();
    } catch {
      /* handled by TanStack Query */
    }
  }, [studySubjectId, scheduleMutation, form]);

  const handleComplete = useCallback(
    async (eventId: number) => {
      try {
        await completeMutation.mutateAsync(eventId);
        message.success("Event completed");
      } catch {
        /* handled by TanStack Query */
      }
    },
    [completeMutation],
  );

  const openSchedule = useCallback(() => {
    setScheduleOpen(true);
  }, []);

  if (loadingEvents) return <SkeletonPage />;

  const columns = [
    {
      title: "Event ID",
      dataIndex: "studyEventId",
      key: "id",
      width: 80,
    },
    {
      title: "Definition",
      dataIndex: "studyEventDefinitionId",
      key: "def",
      render: (v: number) => `Def #${v}`,
    },
    {
      title: "Location",
      dataIndex: "location",
      key: "location",
      render: (v: string) => v || "-",
    },
    {
      title: "Start",
      dataIndex: "dateStart",
      key: "start",
      render: (d: string) =>
        d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "End",
      dataIndex: "dateEnd",
      key: "end",
      render: (d: string) =>
        d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "Status",
      key: "status",
      render: (_: unknown, record: StudyEventDTO) => (
        <span
          className={`status ${STATUS_CLASS_MAP[record.statusId] ?? "status-default"}`}
        >
          {STATUS_LABELS[record.statusId] ?? `Status ${record.statusId}`}
        </span>
      ),
    },
    {
      title: "",
      key: "actions",
      render: (_: unknown, record: StudyEventDTO) =>
        record.statusId < 7 ? (
          <Space>
            <Button size="small" onClick={() => handleComplete(record.studyEventId)}>
              Complete
            </Button>
            <Link to={`/app/actions/study-event/remove/${record.studyEventId}`}>
              <Button size="small" danger type="text">
                Remove
              </Button>
            </Link>
          </Space>
        ) : null,
    },
  ];

  return (
    <div style={{ padding: "24px 32px" }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 24,
        }}
      >
        <Space>
          <Button onClick={() => navigate(-1)}>返回</Button>
          <div>
            <Title level={3} style={{ margin: 0 }}>
              访视事件
            </Title>
            <Text type="secondary">
              Subject #{subjectId} &middot; {events.length} event
              {events.length !== 1 ? "s" : ""}
            </Text>
          </div>
        </Space>
        <Space>
          <Button type="primary" onClick={openSchedule}>
            Schedule Event
          </Button>
        </Space>
      </div>

      <Card
        style={{
          borderRadius: "var(--radius-lg)",
          border: "1px solid var(--border)",
        }}
        styles={{ body: { padding: 0 } }}
      >
        <Table
          dataSource={events}
          columns={columns}
          rowKey="studyEventId"
          pagination={false}
          locale={{ emptyText: "No scheduled events for this subject" }}
          expandable={{
            expandedRowRender: (record: StudyEventDTO) => (
              <EventCrfRow eventId={record.studyEventId} subjectId={subjectId ?? ""} />
            ),
            rowExpandable: () => true,
          }}
        />
      </Card>

      <Modal
        title="Schedule Event"
        open={scheduleOpen}
        onOk={handleSchedule}
        onCancel={() => {
          setScheduleOpen(false);
          form.resetFields();
        }}
        okText="Schedule"
        confirmLoading={scheduleMutation.isPending}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="definitionId"
            label="Event Definition"
            rules={[{ required: true }]}
          >
            <Select
              showSearch
              placeholder="Select event type"
              optionFilterProp="label"
            >
              {definitions.map((d) => (
                <Select.Option
                  key={d.studyEventDefinitionId}
                  value={d.studyEventDefinitionId}
                  label={d.name}
                >
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
