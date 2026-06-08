import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Card, Descriptions, Button, Space, Typography, Table,
  Modal, Form, Input, Select, InputNumber, DatePicker, message, Result,
} from "antd";

import { SkeletonPage } from "@/components/SkeletonCard";
import { useEventDefinitions, useScheduleEvent } from "@/hooks/useEvents";

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
  const [signLoading, setSignLoading] = useState(false);
  const [updateOpen, setUpdateOpen] = useState(false);
  const [eventCreateOpen, setEventCreateOpen] = useState(false);
  const [reassignOpen, setReassignOpen] = useState(false);
  const [reassigning, setReassigning] = useState(false);
  const [signForm] = Form.useForm();
  const [updateForm] = Form.useForm();
  const [eventCreateForm] = Form.useForm();
  const [reassignForm] = Form.useForm();

  const { data: eventDefs = [] } = useEventDefinitions(enrollment?.studyId);
  const scheduleEvent = useScheduleEvent();

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
      setSignLoading(true);
      const res = await fetch(`/api/v1/subjects/${id}/sign`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(vals),
      });
      if (res.ok) {
        message.success("受试者已签名");
        setSignOpen(false);
        signForm.resetFields();
      } else {
        message.error("签名失败，请稍后重试");
      }
    } catch {
      message.error("签名失败，请稍后重试");
    } finally {
      setSignLoading(false);
    }
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

  const handleCreateEvent = async () => {
    try {
      const vals = await eventCreateForm.validateFields();
      await scheduleEvent.mutateAsync({
        studySubjectId: Number(id),
        studyEventDefinitionId: vals.eventDefinitionId,
        location: vals.location ?? "",
        ordinal: 0,
        startDate: vals.startDate?.format("YYYY-MM-DD"),
        endDate: vals.endDate?.format("YYYY-MM-DD"),
      });
      message.success("访视创建成功");
      setEventCreateOpen(false);
      eventCreateForm.resetFields();
      fetchData();
    } catch {
      message.error("访视创建失败");
    }
  };

  const handleReassign = async () => {
    try {
      const vals = await reassignForm.validateFields();
      setReassigning(true);
      const res = await fetch(`/api/v1/subjects/${id}/reassign`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ studyId: vals.newStudyId }),
      });
      if (res.ok) {
        message.success("受试者已重新分配");
        setReassignOpen(false);
        reassignForm.resetFields();
        fetchData();
      } else {
        message.error("重新分配失败");
      }
    } catch {
      message.error("重新分配失败");
    } finally {
      setReassigning(false);
    }
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
            <Button onClick={() => setReassignOpen(true)}>
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
            onClick={() => setEventCreateOpen(true)}>
            安排访视
          </Button>
        }
      >
        <Table dataSource={events} columns={eventColumns} rowKey="studyEventId" pagination={false}
          locale={{ emptyText: "暂无安排访视" }} />
      </Card>

      <Modal title="签署受试者数据" open={signOpen}
        onOk={handleSign} onCancel={() => setSignOpen(false)}
        confirmLoading={signLoading}>
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

      <Modal title="安排访视" open={eventCreateOpen}
        onOk={handleCreateEvent} onCancel={() => setEventCreateOpen(false)}
        confirmLoading={scheduleEvent.isPending}>
        <Form form={eventCreateForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="eventDefinitionId" label="访视定义" rules={[{ required: true, message: "请选择访视定义" }]}>
            <Select
              placeholder="选择访视类型"
              options={eventDefs.map(d => ({
                value: d.studyEventDefinitionId,
                label: `${d.name}${d.ordinal ? ` (#${d.ordinal})` : ""}`,
              }))}
            />
          </Form.Item>
          <Form.Item name="location" label="地点">
            <Input placeholder="例如：门诊部 1 楼" />
          </Form.Item>
          <Form.Item name="startDate" label="开始日期">
            <DatePicker style={{ width: "100%" }} placeholder="选择开始日期" />
          </Form.Item>
          <Form.Item name="endDate" label="结束日期">
            <DatePicker style={{ width: "100%" }} placeholder="选择结束日期（可选）" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="重新分配受试者" open={reassignOpen}
        onOk={handleReassign} onCancel={() => setReassignOpen(false)}
        confirmLoading={reassigning}>
        <Form form={reassignForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="newStudyId" label="新研究 ID" rules={[{ required: true, message: "请输入新研究 ID" }]}>
            <InputNumber style={{ width: "100%" }} min={1} placeholder="输入目标研究 ID" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
