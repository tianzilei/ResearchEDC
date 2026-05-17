import { useState } from "react";
import { Card, Table, Tag, Button, Space, Typography, Modal, Form, Input, Select, InputNumber, message, Alert } from "antd";
import { PlusOutlined, SafetyOutlined } from "@ant-design/icons";
import { useSchemes, useCreateScheme, useActivateScheme, useCloseScheme } from "@/hooks/useRandomization";
import { useCurrentStudy } from "@/hooks/useStudies";
import { useNavigate } from "react-router-dom";
import { SkeletonPage } from "@/components/SkeletonCard";
import type { SchemeDTO, ArmDTO } from "@/types/randomization";

const { Title } = Typography;

export default function RandomizationDashboard() {
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

  const statusColors: Record<string, string> = {
    DRAFT: "default",
    ACTIVE: "success",
    PAUSED: "warning",
    CLOSED: "error",
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
      message.success("Scheme created");
      setModalOpen(false);
      form.resetFields();
    } catch (e: any) {
      message.error(e?.message ?? "Failed to create scheme");
    }
  };

  if (!currentStudy) {
    return (
      <Alert
        message="No study selected"
        description="Please select a study from the study switcher to view randomization schemes."
        type="info"
        showIcon
      />
    );
  }

  if (isLoading) return <SkeletonPage />;

  const columns = [
    { title: "Name", dataIndex: "name", key: "name",
      render: (name: string, record: any) => (
        <a onClick={() => navigate(`/app/randomization/schemes/${record.id}`)}>{name}</a>
      ),
    },
    { title: "Algorithm", dataIndex: "algorithm", key: "algorithm",
      render: (alg: string) => <Tag color={algorithmColors[alg]}>{alg}</Tag>,
    },
    { title: "Status", dataIndex: "status", key: "status",
      render: (s: string) => <Tag color={statusColors[s]}>{s}</Tag>,
    },
    { title: "Arms", dataIndex: "totalArms", key: "totalArms" },
    { title: "Assigned", dataIndex: "totalAssigned", key: "totalAssigned" },
    {
      title: "Actions", key: "actions",
      render: (_: any, record: any) => (
        <Space>
          {record.status === "DRAFT" && (
            <Button size="small" type="primary" onClick={() => activateScheme.mutate(record.id)}>
              Activate
            </Button>
          )}
          {record.status === "ACTIVE" && (
            <Button size="small" danger onClick={() => closeScheme.mutate(record.id)}>
              Close
            </Button>
          )}
          <Button size="small" onClick={() => navigate(`/app/randomization/schemes/${record.id}`)}>
            View
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }}>
        <Title level={4} style={{ marginTop: 0 }}>
          <SafetyOutlined /> Randomization
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          New Scheme
        </Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={schemes ?? []}
          columns={columns}
          rowKey="id"
          pagination={false}
          locale={{ emptyText: "No randomization schemes. Create one to get started." }}
        />
      </Card>

      <Modal
        title="Create Randomization Scheme"
        open={modalOpen}
        onOk={handleCreate}
        onCancel={() => setModalOpen(false)}
        confirmLoading={createScheme.isPending}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Scheme Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="algorithm" label="Algorithm" rules={[{ required: true }]}>
            <Select>
              <Select.Option value="SIMPLE">Simple (Coin Toss)</Select.Option>
              <Select.Option value="BLOCK">Block Randomization</Select.Option>
              <Select.Option value="STRATIFIED_BLOCK">Stratified Block</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(prev, cur) => prev.algorithm !== cur.algorithm}>
            {({ getFieldValue }) => {
              const alg = getFieldValue("algorithm");
              if (alg === "BLOCK" || alg === "STRATIFIED_BLOCK") {
                return (
                  <Space>
                    <Form.Item name="minBlockSize" label="Min Block Size" rules={[{ required: true }]}>
                      <InputNumber min={2} />
                    </Form.Item>
                    <Form.Item name="maxBlockSize" label="Max Block Size" rules={[{ required: true }]}>
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
