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
import { PlusOutlined, TeamOutlined, UserOutlined } from "@ant-design/icons";
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
      message.success("Group created");
      setCreateOpen(false);
      form.resetFields();
    } catch { void 0; }
  };

  return (
    <div>
      <Space style={{ width: "100%", justifyContent: "space-between", marginBottom: 8 }}>
        <Text strong>Groups ({initialGroups.length})</Text>
        <Button type="primary" size="small" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
          Add Group
        </Button>
      </Space>
      {initialGroups.length === 0 ? (
        <Text type="secondary">No groups defined</Text>
      ) : (
        initialGroups.map((g) => (
          <Card key={g.groupId} size="small" style={{ marginBottom: 8, borderRadius: 8 }}>
            <Space>
              <UserOutlined />
              <div>
                <Text strong>{g.name}</Text>
                {g.description && <Text type="secondary" style={{ marginLeft: 8 }}>{g.description}</Text>}
              </div>
            </Space>
          </Card>
        ))
      )}
      <Modal
        title="Create Group"
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        confirmLoading={createGroup.isPending}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="Group Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Drug A" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={2} placeholder="Optional description" />
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
      message.success("Group class created");
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
          { title: <Link to="/app/studies">Studies</Link> },
          { title: <Link to={`/app/studies/${id}`}>Study #{id}</Link> },
          { title: "Subject Groups" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <TeamOutlined style={{ fontSize: 22, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>Subject Group Classes</Title>
              <Text type="secondary">{classes?.length ?? 0} group class{(classes?.length ?? 0) !== 1 ? "es" : ""}</Text>
            </div>
          </Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
            New Group Class
          </Button>
        </div>
      </Card>

      {!classes || classes.length === 0 ? (
        <Card style={{ borderRadius: 14 }}>
          <Empty description="No subject group classes defined" />
        </Card>
      ) : (
        <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
          <Collapse
            items={classes.map((gc) => ({
              key: String(gc.groupClassId),
              label: (
                <Space>
                  <TeamOutlined />
                  <Text strong>{gc.name}</Text>
                  <Tag>{gc.groupClassType || "Generic"}</Tag>
                  <Tag>{gc.subjectAssignment}</Tag>
                  <Text type="secondary">{gc.groups?.length ?? 0} groups</Text>
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
        title="Create Group Class"
        open={createOpen}
        onOk={handleCreateClass}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }}
        confirmLoading={createClass.isPending}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="Class Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Treatment Group" />
          </Form.Item>
          <Form.Item name="subjectAssignment" label="Subject Assignment">
            <Select defaultValue="optimal">
              <Select.Option value="optimal">Optimal</Select.Option>
              <Select.Option value="random">Random</Select.Option>
              <Select.Option value="manual">Manual</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
