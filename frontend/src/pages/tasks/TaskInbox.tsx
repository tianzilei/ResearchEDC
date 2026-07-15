import { useMemo, useState } from "react";
import { Alert, Button, Card, DatePicker, Empty, Form, Input, Modal, Select, Space, Table, Tag, Typography, message } from "antd";
import { CheckOutlined, CloseOutlined, PlusOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import { formatApiError } from "@/api/errors";
import { useAuth } from "@/providers/AuthProvider";
import { useCurrentStudy } from "@/hooks/useStudies";
import { SkeletonPage } from "@/components/SkeletonCard";
import {
  useCancelTask,
  useCompleteTask,
  useCreateTask,
  useTasks,
  type CreateTaskRequest,
  type TaskInstanceDTO,
  type TaskStatus,
} from "@/hooks/useTasks";

const { Title, Text } = Typography;

const STATUS_COLORS: Record<TaskStatus, string> = {
  PENDING: "default",
  DUE: "processing",
  OVERDUE: "error",
  COMPLETED: "success",
  CANCELLED: "warning",
  EXPIRED: "purple",
};

interface TaskFormValues {
  title: string;
  description?: string;
  assignedTo?: number;
  dueDate?: { toDate: () => Date };
}

export default function TaskInbox() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id;
  const [status, setStatus] = useState<TaskStatus | undefined>();
  const [assignedToMe, setAssignedToMe] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm<TaskFormValues>();
  const { data: tasks = [], isLoading } = useTasks({ studyId, status, assignedToMe });
  const createTask = useCreateTask(studyId);
  const completeTask = useCompleteTask();
  const cancelTask = useCancelTask();

  const statusOptions = useMemo(() => ([
    { value: "PENDING", label: t("tasks.status.pending") },
    { value: "DUE", label: t("tasks.status.due") },
    { value: "OVERDUE", label: t("tasks.status.overdue") },
    { value: "COMPLETED", label: t("tasks.status.completed") },
    { value: "CANCELLED", label: t("tasks.status.cancelled") },
    { value: "EXPIRED", label: t("tasks.status.expired") },
  ]), [t]);

  if (!currentStudy) {
    return <Alert message={t("tasks.selectStudy")} type="info" />;
  }

  if (isLoading) {
    return <SkeletonPage />;
  }

  const submitTask = async () => {
    try {
      const values = await form.validateFields();
      const request: CreateTaskRequest = {
        studyId: currentStudy.id,
        title: values.title,
        description: values.description,
        assignedTo: values.assignedTo ? Number(values.assignedTo) : user?.userId,
        targetType: "STUDY",
        targetId: currentStudy.id,
        dueDate: values.dueDate?.toDate().toISOString(),
      };
      await createTask.mutateAsync(request);
      message.success(t("tasks.created"));
      setModalOpen(false);
      form.resetFields();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("tasks.error.createFailed")));
    }
  };

  const columns = [
    {
      title: t("tasks.column.title"),
      dataIndex: "title",
      key: "title",
      render: (_: string, record: TaskInstanceDTO) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.title}</Text>
          {record.description && <Text type="secondary">{record.description}</Text>}
        </Space>
      ),
    },
    {
      title: t("tasks.column.status"),
      dataIndex: "status",
      key: "status",
      width: 130,
      render: (value: TaskStatus) => (
        <Tag color={STATUS_COLORS[value]}>{t(`tasks.status.${value.toLowerCase()}`)}</Tag>
      ),
    },
    {
      title: t("tasks.column.due"),
      dataIndex: "dueDate",
      key: "dueDate",
      width: 190,
      render: (value: string | null) => value ? new Date(value).toLocaleString() : "-",
    },
    {
      title: t("tasks.column.assignedTo"),
      dataIndex: "assignedTo",
      key: "assignedTo",
      width: 130,
      render: (value: number | null) => value ?? "-",
    },
    {
      title: t("tasks.column.actions"),
      key: "actions",
      width: 210,
      render: (_: unknown, record: TaskInstanceDTO) => {
        const active = record.status === "PENDING" || record.status === "DUE" || record.status === "OVERDUE";
        return (
          <Space>
            <Button
              size="small"
              icon={<CheckOutlined />}
              disabled={!active}
              onClick={() => completeTask.mutate(record.id)}
            >
              {t("tasks.action.complete")}
            </Button>
            <Button
              size="small"
              danger
              icon={<CloseOutlined />}
              disabled={!active}
              onClick={() => cancelTask.mutate(record.id)}
            >
              {t("tasks.action.cancel")}
            </Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }} align="start">
        <div>
          <Title level={4} style={{ marginTop: 0 }}>{t("tasks.title")}</Title>
          <Text type="secondary">{currentStudy.name}</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          {t("tasks.newTask")}
        </Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Space style={{ marginBottom: 16 }} wrap>
          <Select
            placeholder={t("tasks.filter.status")}
            allowClear
            style={{ width: 180 }}
            value={status}
            onChange={(value) => setStatus(value)}
            options={statusOptions}
          />
          <Select
            style={{ width: 180 }}
            value={assignedToMe ? "mine" : "study"}
            onChange={(value) => setAssignedToMe(value === "mine")}
            options={[
              { value: "study", label: t("tasks.filter.study") },
              { value: "mine", label: t("tasks.filter.mine") },
            ]}
          />
        </Space>
        <Table
          dataSource={tasks}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: <Empty description={t("tasks.empty")} /> }}
        />
      </Card>

      <Modal
        title={t("tasks.modal.title")}
        open={modalOpen}
        onCancel={() => {
          setModalOpen(false);
          form.resetFields();
        }}
        onOk={submitTask}
        confirmLoading={createTask.isPending}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="title"
            label={t("tasks.modal.titleLabel")}
            rules={[{ required: true, message: t("tasks.modal.titleRequired") }]}
          >
            <Input maxLength={160} />
          </Form.Item>
          <Form.Item name="description" label={t("tasks.modal.description")}>
            <Input.TextArea rows={3} maxLength={2000} />
          </Form.Item>
          <Form.Item name="assignedTo" label={t("tasks.modal.assignedTo")}>
            <Input type="number" placeholder={String(user?.userId ?? "")} />
          </Form.Item>
          <Form.Item name="dueDate" label={t("tasks.modal.dueDate")}>
            <DatePicker showTime style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
