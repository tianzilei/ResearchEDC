import { useState, useCallback } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
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
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { data: crfs = [], isLoading } = useEventCrfs(eventId);

  if (isLoading) {
    return <Spin size="small" style={{ margin: "12px 16px" }} />;
  }

  if (crfs.length === 0) {
    return (
      <Text type="secondary" style={{ display: "block", padding: "12px 16px" }}>
        {t("events.noCrfs")}
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
              {t("events.enterData")}
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
                  {STATUS_LABELS[crf.statusId] ?? t("events.statusWithId", { id: crf.statusId })}
                </span>
              </Space>
            }
            description={
              crf.dateInterviewed
                ? t("events.interviewed", { date: new Date(crf.dateInterviewed).toLocaleDateString() })
                : t("events.notStarted")
            }
          />
        </List.Item>
      )}
    />
  );
}

export default function EventList() {
  const { t } = useTranslation();
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
        location: vals.location ?? "",
        startDate: vals.startDate?.format("YYYY-MM-DDTHH:mm:ss") ?? null,
        endDate: vals.endDate?.format("YYYY-MM-DDTHH:mm:ss") ?? null,
      });
      message.success(t("events.scheduled"));
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
        message.success(t("events.completed"));
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
      title: t("events.column.id"),
      dataIndex: "studyEventId",
      key: "id",
      width: 80,
    },
    {
      title: t("events.column.definition"),
      dataIndex: "studyEventDefinitionId",
      key: "def",
      render: (v: number) => t("events.definitionWithId", { id: v }),
    },
    {
      title: t("events.column.location"),
      dataIndex: "location",
      key: "location",
      render: (v: string) => v || "-",
    },
    {
      title: t("events.column.start"),
      dataIndex: "dateStart",
      key: "start",
      render: (d: string) =>
        d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: t("events.column.end"),
      dataIndex: "dateEnd",
      key: "end",
      render: (d: string) =>
        d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: t("events.column.status"),
      key: "status",
      render: (_: unknown, record: StudyEventDTO) => (
        <span
          className={`status ${STATUS_CLASS_MAP[record.statusId] ?? "status-default"}`}
        >
          {STATUS_LABELS[record.statusId] ?? t("events.statusWithId", { id: record.statusId })}
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
              {t("events.complete")}
            </Button>
            <Link to={`/app/actions/study-event/remove/${record.studyEventId}`}>
              <Button size="small" danger type="text">
                {t("events.remove")}
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
          <Button onClick={() => navigate(-1)}>{t("entry.back")}</Button>
          <div>
            <Title level={3} style={{ margin: 0 }}>
              {t("events.title")}
            </Title>
            <Text type="secondary">
              {t("events.subjectSummary", { subjectId, count: events.length })}
            </Text>
          </div>
        </Space>
        <Space>
          <Button type="primary" onClick={openSchedule}>
            {t("events.schedule")}
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
          locale={{ emptyText: t("events.empty") }}
          expandable={{
            expandedRowRender: (record: StudyEventDTO) => (
              <EventCrfRow eventId={record.studyEventId} subjectId={subjectId ?? ""} />
            ),
            rowExpandable: () => true,
          }}
        />
      </Card>

      <Modal
        title={t("events.schedule")}
        open={scheduleOpen}
        onOk={handleSchedule}
        onCancel={() => {
          setScheduleOpen(false);
          form.resetFields();
        }}
        okText={t("events.schedule")}
        confirmLoading={scheduleMutation.isPending}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="definitionId"
            label={t("events.form.definition")}
            rules={[{ required: true, message: t("events.form.definitionRequired") }]}
          >
            <Select
              showSearch
              placeholder={t("events.form.definitionPlaceholder")}
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
          <Form.Item name="location" label={t("events.column.location")}>
            <Input placeholder={t("events.form.locationPlaceholder")} />
          </Form.Item>
          <Form.Item name="startDate" label={t("events.column.start")}>
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="endDate" label={t("events.column.end")}>
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
