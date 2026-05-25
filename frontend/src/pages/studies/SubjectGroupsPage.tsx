import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  Breadcrumb,
  Card,
  Tag,
  Button,
  Space,
  Typography,
  Modal,
  Form,
  Input,
  Select,
  Spin,
  message,
  Empty,
  Collapse,
} from "antd";

import { useGroupClasses, useCreateGroupClass, useCreateGroup } from "@/hooks/useSubjectGroups";
import type { SubjectGroupDTO } from "@/types/subjectGroup";

const { Title, Text } = Typography;

function GroupList({ classId, groups: initialGroups }: { classId: number; groups: SubjectGroupDTO[] }) {
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();
  const createGroup = useCreateGroup();

  const handleCreate = async () => {
    try {
      const vals = await form.validateFields();
      await createGroup.mutateAsync({ classId, data: vals });
      message.success("分组已创建");
      setCreateOpen(false);
      form.resetFields();
    } catch { void 0; }
  };

  return (
    <div>
      <Space style={{ width: "100%", justifyContent: "space-between", marginBottom: 8 }}>
        <Text strong>分组 ({initialGroups.length})</Text>
        <Button type="primary" size="small" onClick={() => setCreateOpen(true)}>
          添加分组
        </Button>
      </Space>
      {initialGroups.length === 0 ? (
        <Text type="secondary">未定义分组</Text>
      ) : (
        initialGroups.map((g) => (
          <Card key={g.groupId} size="small" style={{ marginBottom: 8 }}>
            <div>
              <Text strong>{g.name}</Text>
              {g.description && <Text type="secondary" style={{ marginLeft: 8 }}>{g.description}</Text>}
            </div>
          </Card>
        ))
      )}
      <Modal
        title="创建分组"
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        confirmLoading={createGroup.isPending}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="分组名称" rules={[{ required: true }]}>
            <Input placeholder="例如：药物 A 组" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} placeholder="可选描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default function SubjectGroupsPage() {
  const { id } = useParams<{ id: string }>();
  const studyId = id ? Number(id) : undefined;
  const { data: classes, isLoading } = useGroupClasses(studyId);
  const createClass = useCreateGroupClass();

  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();

  const handleCreateClass = async () => {
    try {
      const vals = await form.validateFields();
      await createClass.mutateAsync({ ...vals, studyId });
      message.success("分组类别已创建");
      setCreateOpen(false);
      form.resetFields();
    } catch { void 0; }
  };

  if (isLoading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 80 }}><Spin size="large" /></div>;
  }

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/studies">研究</Link> },
          { title: <Link to={`/app/studies/${id}`}>研究 #{id}</Link> },
          { title: "受试者分组" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>分组类别</Title>
            <Text type="secondary">{classes?.length ?? 0} 个类别</Text>
          </div>
          <Button type="primary" onClick={() => setCreateOpen(true)}>
            新建类别
          </Button>
        </div>
      </Card>

      {!classes || classes.length === 0 ? (
        <Card>
          <Empty description="未定义受试者分组类别" />
        </Card>
      ) : (
        <Card styles={{ body: { padding: 0 } }}>
          <Collapse
            items={classes.map((gc) => ({
              key: String(gc.groupClassId),
              label: (
                <Space>
                  <Text strong>{gc.name}</Text>
                  <Tag>{gc.groupClassType || "通用"}</Tag>
                  <Tag>{gc.subjectAssignment}</Tag>
                  <Text type="secondary">{gc.groups?.length ?? 0} 个分组</Text>
                </Space>
              ),
              children: (
                <GroupList classId={gc.groupClassId} groups={gc.groups ?? []} />
              ),
            }))}
          />
        </Card>
      )}

      <Modal
        title="创建分组类别"
        open={createOpen}
        onOk={handleCreateClass}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        confirmLoading={createClass.isPending}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="类别名称" rules={[{ required: true }]}>
            <Input placeholder="例如：治疗组" />
          </Form.Item>
          <Form.Item name="subjectAssignment" label="受试者分配方式">
            <Select defaultValue="optimal">
              <Select.Option value="optimal">最优分配</Select.Option>
              <Select.Option value="random">随机</Select.Option>
              <Select.Option value="manual">手动</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
