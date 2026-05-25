import { useState, useCallback } from "react";
import { useTranslation } from "react-i18next";
import {
  Card,
  Table,
  Tag,
  Button,
  Typography,
  Modal,
  Form,
  Input,
  Space,
  message,
  Empty,
  Tooltip,
} from "antd";
import { useParams, useNavigate } from "react-router-dom";
import {
  useCrfList,
  useCrfVersions,
  useUpdateCrfVersionStatus,
  useDeleteCrfVersion,
  useCreateCrfVersion,
} from "@/hooks/useCrf";
import { SkeletonPage } from "@/components/SkeletonCard";
import type { CrfVersionEntity } from "@/types/crf";

const { Title, Text } = Typography;

const STATUS_AVAILABLE = 1;
const STATUS_LOCKED = 2;

const statusTag = (statusId: number) => {
  if (statusId === STATUS_LOCKED) {
    return <Tag color="orange">Locked</Tag>;
  }
  return <Tag color="success">Available</Tag>;
};

export default function CrfVersionManager() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { crfId: routeCrfId } = useParams<{ crfId: string }>();
  const crfId = Number(routeCrfId);

  const { data: crfs, isLoading: crfsLoading } = useCrfList();
  const { data: versions, isLoading: versionsLoading } = useCrfVersions(
    crfId > 0 ? crfId : undefined,
  );
  const updateStatus = useUpdateCrfVersionStatus();
  const deleteVersion = useDeleteCrfVersion();
  const createVersion = useCreateCrfVersion();

  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);
  const [form] = Form.useForm();

  const crfName =
    crfs?.find((c) => c.crfId === crfId)?.name ?? t("crf.versionManager.title");

  const handleToggleStatus = useCallback(
    (record: CrfVersionEntity) => {
      const newStatus =
        record.statusId === STATUS_LOCKED ? STATUS_AVAILABLE : STATUS_LOCKED;
      updateStatus.mutate(
        { crfVersionId: record.crfVersionId, statusId: newStatus },
        {
          onSuccess: () => {
            message.success(
              newStatus === STATUS_LOCKED
                ? t("crf.versionManager.locked")
                : t("crf.versionManager.unlocked"),
            );
          },
          onError: (err) => {
            message.error(err.message || t("crf.versionManager.statusError"));
          },
        },
      );
    },
    [updateStatus, t],
  );

  const handleDelete = useCallback(
    (versionId: number) => {
      deleteVersion.mutate(versionId, {
        onSuccess: () => {
          message.success(t("crf.versionManager.deleted"));
          setDeleteConfirmId(null);
        },
        onError: (err) => {
          message.error(err.message || t("crf.versionManager.deleteError"));
        },
      });
    },
    [deleteVersion, t],
  );

  const handleCreate = useCallback(async () => {
    try {
      const values = await form.validateFields();
      createVersion.mutate(
        { crfId, name: values.name, description: values.description ?? "" },
        {
          onSuccess: () => {
            message.success(t("crf.versionManager.created"));
            setCreateModalOpen(false);
            form.resetFields();
          },
          onError: (err) => {
            message.error(err.message || t("crf.versionManager.createError"));
          },
        },
      );
    } catch {
      void 0;
    }
  }, [crfId, createVersion, form, t]);

  const columns = [
    {
      title: t("crf.versionManager.column.name"),
      dataIndex: "name",
      key: "name",
      render: (name: string) => <Text strong>{name}</Text>,
    },
    {
      title: t("crf.versionManager.column.description"),
      dataIndex: "description",
      key: "description",
      ellipsis: true,
      render: (desc: string) => desc || <Text type="secondary">-</Text>,
    },
    {
      title: t("crf.versionManager.column.status"),
      dataIndex: "statusId",
      key: "statusId",
      width: 130,
      render: (statusId: number) => statusTag(statusId),
    },
    {
      title: t("crf.versionManager.column.created"),
      dataIndex: "dateCreated",
      key: "dateCreated",
      width: 140,
      render: (d: string) =>
        d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: t("crf.versionManager.column.actions"),
      key: "actions",
      width: 200,
      render: (_: unknown, record: CrfVersionEntity) => (
        <Space size="small">
          <Tooltip title={t("crf.versionManager.preview")}>
            <Button
              size="small"
              onClick={() => {
                navigate(`/app/crfs/${String(record.crfVersionId)}`);
              }}
            >
              {t("crf.versionManager.preview")}
            </Button>
          </Tooltip>
          <Tooltip
            title={
              record.statusId === STATUS_LOCKED
                ? t("crf.versionManager.unlock")
                : t("crf.versionManager.lock")
            }
          >
            <Button
              size="small"
              onClick={() => {
                handleToggleStatus(record);
              }}
              loading={updateStatus.isPending}
            />
          </Tooltip>
          <Tooltip title={t("crf.versionManager.delete")}>
            <Button
              size="small"
              danger
              onClick={() => {
                setDeleteConfirmId(record.crfVersionId);
              }}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  if (crfsLoading) return <SkeletonPage />;

  if (!crfId || crfId <= 0) {
    return (
      <div>
        <Title level={4} style={{ marginTop: 0 }}>
          {t("crf.versionManager.selectCrf")}
        </Title>
        <Card>
          <Empty description={t("crf.versionManager.selectCrfHint")} />
        </Card>
      </div>
    );
  }

  return (
    <div className="animate-in">
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 24,
        }}
      >
        <div>
          <Button
            onClick={() => {
              navigate("/app/crfs");
            }}
            style={{ marginBottom: 12 }}
          >
            {t("crf.back")}
          </Button>
          <Title level={4} style={{ margin: 0 }}>
            {crfName}
          </Title>
          <Text type="secondary">
            {t("crf.versionManager.versionCount", {
              count: versions?.length ?? 0,
            })}
          </Text>
        </div>
        <Button
          type="primary"
          onClick={() => {
            setCreateModalOpen(true);
          }}
        >
          {t("crf.versionManager.createVersion")}
        </Button>
      </div>

      <Card>
        <Table
          dataSource={versions ?? []}
          columns={columns}
          rowKey="crfVersionId"
          loading={versionsLoading}
          pagination={false}
          locale={{
            emptyText: <Empty description={t("crf.versionManager.noVersions")} />,
          }}
        />
      </Card>

      <Modal
        title={t("crf.versionManager.createVersion")}
        open={createModalOpen}
        onOk={() => {
          void handleCreate();
        }}
        onCancel={() => {
          setCreateModalOpen(false);
          form.resetFields();
        }}
        confirmLoading={createVersion.isPending}
        okText={t("crf.versionManager.create")}
        width={480}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="name"
            label={t("crf.versionManager.column.name")}
            rules={[{ required: true, message: t("crf.versionManager.nameRequired") }]}
          >
            <Input placeholder={t("crf.versionManager.namePlaceholder")} />
          </Form.Item>
          <Form.Item
            name="description"
            label={t("crf.versionManager.column.description")}
          >
            <Input.TextArea
              rows={3}
              placeholder={t("crf.versionManager.descriptionPlaceholder")}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={t("crf.versionManager.confirmDelete")}
        open={deleteConfirmId !== null}
        onOk={() => {
          if (deleteConfirmId !== null) handleDelete(deleteConfirmId);
        }}
        onCancel={() => {
          setDeleteConfirmId(null);
        }}
        confirmLoading={deleteVersion.isPending}
        okText={t("crf.versionManager.delete")}
        okButtonProps={{ danger: true }}
        width={420}
      >
        <Text>{t("crf.versionManager.deleteWarning")}</Text>
      </Modal>
    </div>
  );
}
