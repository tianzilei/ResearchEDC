import { useState } from "react";
import { Card, Table, Tag, Button, Space, Typography, Modal, Form, Input, Select, InputNumber, message, Alert } from "antd";
import { useTranslation } from "react-i18next";
import { useSchemes, useCreateScheme, useActivateScheme, useCloseScheme } from "@/hooks/useRandomization";
import { useCurrentStudy } from "@/hooks/useStudies";
import { useNavigate } from "react-router-dom";
import { SkeletonPage } from "@/components/SkeletonCard";
import type { SchemeDTO, ArmDTO } from "@/types/randomization";

const { Title } = Typography;

export default function RandomizationDashboard() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id ?? 0;
  const { data: schemes, isLoading } = useSchemes(studyId);
  const createScheme = useCreateScheme();
  const activateScheme = useActivateScheme();
  const closeScheme = useCloseScheme();
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const algorithmColors: Record<string, string> = {
    SIMPLE: "green",
    BLOCK: "blue",
    STRATIFIED_BLOCK: "purple",
  };

  const statusClasses: Record<string, string> = {
    DRAFT: "status-default",
    ACTIVE: "status-success",
    PAUSED: "status-warning",
    CLOSED: "status-danger",
  };

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      const arms: ArmDTO[] = values.arms?.map((a: any, i: number) => ({
        name: a.name,
        displayName: a.displayName ?? a.name,
        ratio: a.ratio ?? 1,
        orderNumber: i + 1,
      })) ?? [{ name: "Control", ratio: 1, orderNumber: 1 }, { name: "Treatment", ratio: 1, orderNumber: 2 }];

      const dto: SchemeDTO = {
        studyId,
        name: values.name,
        algorithm: values.algorithm,
        minBlockSize: values.minBlockSize,
        maxBlockSize: values.maxBlockSize,
        arms,
        stratifications: [],
      };

      await createScheme.mutateAsync(dto);
      message.success(t("randomization.created"));
      setModalOpen(false);
      form.resetFields();
    } catch (e: any) {
      message.error(e?.message ?? t("randomization.createFailed"));
    }
  };

  if (!currentStudy) {
    return (
      <Alert
        message={t("randomization.noStudy")}
        description={t("randomization.noStudyDescription")}
        type="info"
        showIcon
      />
    );
  }

  if (isLoading) return <SkeletonPage />;

  const columns = [
    { title: t("randomization.column.name"), dataIndex: "name", key: "name",
      render: (name: string, record: any) => (
        <a onClick={() => navigate(`/app/randomization/schemes/${record.id}`)}>{name}</a>
      ),
    },
    { title: t("randomization.column.algorithm"), dataIndex: "algorithm", key: "algorithm",
      render: (alg: string) => <Tag color={algorithmColors[alg]}>{alg}</Tag>,
    },
    { title: t("randomization.column.status"), dataIndex: "status", key: "status",
      render: (s: string) => <span className={`status ${statusClasses[s] ?? "status-default"}`}>{s}</span>,
    },
    { title: t("randomization.column.arms"), dataIndex: "totalArms", key: "totalArms" },
    { title: t("randomization.column.assigned"), dataIndex: "totalAssigned", key: "totalAssigned" },
    {
      title: t("randomization.column.actions"), key: "actions",
      render: (_: any, record: any) => (
        <Space>
          {record.status === "DRAFT" && (
            <Button size="small" type="primary" onClick={() => activateScheme.mutate(record.id)}>
              {t("randomization.action.activate")}
            </Button>
          )}
          {record.status === "ACTIVE" && (
            <Button size="small" danger onClick={() => closeScheme.mutate(record.id)}>
              {t("randomization.action.close")}
            </Button>
          )}
          <Button size="small" onClick={() => navigate(`/app/randomization/schemes/${record.id}`)}>
            {t("randomization.action.view")}
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <Title level={4} style={{ marginTop: 0 }}>
          {t("randomization.title")}
        </Title>
        <Button type="primary" onClick={() => setModalOpen(true)}>
          {t("randomization.newScheme")}
        </Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={schemes ?? []}
          columns={columns}
          rowKey="id"
          pagination={false}
          locale={{ emptyText: t("randomization.empty") }}
        />
      </Card>

      <Modal
        title={t("randomization.createScheme")}
        open={modalOpen}
        onOk={handleCreate}
        onCancel={() => setModalOpen(false)}
        confirmLoading={createScheme.isPending}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label={t("randomization.form.schemeName")} rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="algorithm" label={t("randomization.form.algorithm")} rules={[{ required: true }]}>
            <Select>
              <Select.Option value="SIMPLE">{t("randomization.form.simple")}</Select.Option>
              <Select.Option value="BLOCK">{t("randomization.form.block")}</Select.Option>
              <Select.Option value="STRATIFIED_BLOCK">{t("randomization.form.stratified")}</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(prev, cur) => prev.algorithm !== cur.algorithm}>
            {({ getFieldValue }) => {
              const alg = getFieldValue("algorithm");
              if (alg === "BLOCK" || alg === "STRATIFIED_BLOCK") {
                return (
                  <Space>
                    <Form.Item name="minBlockSize" label={t("randomization.form.minBlockSize")} rules={[{ required: true }]}>
                      <InputNumber min={2} />
                    </Form.Item>
                    <Form.Item name="maxBlockSize" label={t("randomization.form.maxBlockSize")} rules={[{ required: true }]}>
                      <InputNumber min={2} />
                    </Form.Item>
                  </Space>
                );
              }
              return null;
            }}
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
