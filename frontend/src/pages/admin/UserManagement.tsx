import { useEffect, useState } from "react";
import { Card, Table, Button, Typography, Modal, Form, Input, Select, message, Tabs } from "antd";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface UserDTO {
  userId: number;
  userName: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  enabled: boolean;
  activeStudyId: number | null;
  dateCreated: string;
}

interface RoleDTO {
  studyUserRoleId: number;
  roleName: string;
  userName: string;
  studyId: number | null;
  statusId: number | null;
}

export default function UserManagement() {
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [roles, setRoles] = useState<RoleDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [roleOpen, setRoleOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<string | null>(null);
  const [form] = Form.useForm();
  const [roleForm] = Form.useForm();

  useEffect(() => {
    const fetchAll = async () => {
      try {
        const r = await fetch("/api/v1/identity/users?query=");
        if (r.ok) setUsers(await r.json());
      } catch { /* ignore */ }
      setLoading(false);
    };
    fetchAll();
  }, []);

  const handleCreate = async () => {
    try {
      const vals = await form.validateFields();
      const res = await fetch("/api/v1/identity/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userName: vals.userName,
          firstName: vals.firstName,
          lastName: vals.lastName,
          phone: vals.phone ?? null,
          institutionalAffiliation: vals.affiliation ?? null,
          statusId: 1,
        }),
      });
      if (!res.ok) { const err = await res.text(); message.error(`创建失败: ${err}`); return; }
      message.success("用户已创建");
      setCreateOpen(false);
      form.resetFields();
      const r = await fetch("/api/v1/identity/users?query=");
      if (r.ok) setUsers(await r.json());
    } catch { /* validation error */ }
  };

  const handleAssignRole = async () => {
    try {
      const vals = await roleForm.validateFields();
      const res = await fetch("/api/v1/identity/roles/assign", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userName: selectedUser,
          studyId: vals.studyId ? Number(vals.studyId) : null,
          roleName: vals.roleName,
          statusId: 1,
        }),
      });
      if (!res.ok) { message.error("分配角色失败"); return; }
      message.success("角色已分配");
      setRoleOpen(false);
      roleForm.resetFields();
      const r = await fetch(`/api/v1/identity/roles/by-user?userName=${selectedUser}`);
      if (r.ok) setRoles(await r.json());
    } catch { /* validation error */ }
  };

  const viewRoles = async (userName: string) => {
    setSelectedUser(userName);
    const r = await fetch(`/api/v1/identity/roles/by-user?userName=${userName}`);
    if (r.ok) setRoles(await r.json());
    setRoleOpen(true);
  };

  if (loading) return <SkeletonPage />;

  const columns = [
    {
      title: "用户名", dataIndex: "userName", key: "userName",
      render: (text: string) => <strong>{text}</strong>,
    },
    { title: "姓名", key: "name", render: (_: unknown, r: UserDTO) => `${r.firstName} ${r.lastName}` },
    { title: "电话", dataIndex: "phone", key: "phone", render: (v: string | null) => v ?? "-" },
    {
      title: "状态", dataIndex: "enabled", key: "enabled",
      render: (v: boolean) => v
        ? <span className="status status-success">正常</span>
        : <span className="status status-danger">已停用</span>,
    },
    {
      title: "", key: "actions",
      render: (_: unknown, record: UserDTO) => (
        <Button size="small" onClick={() => viewRoles(record.userName)}>
          角色
        </Button>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <div>
          <Title level={4} style={{ margin: 0 }}>用户管理</Title>
          <Text style={{ color: "var(--text-secondary)", fontSize: 13 }}>{users.length} 名用户</Text>
        </div>
        <Button type="primary" onClick={() => setCreateOpen(true)}>
          创建用户
        </Button>
      </div>

      <Card styles={{ body: { padding: 0 } }}>
        <Table dataSource={users} columns={columns} rowKey="userId" pagination={{ pageSize: 20 }}
          locale={{ emptyText: "暂无用户" }} />
      </Card>

      <Modal title="创建用户" open={createOpen} onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }} okText="创建" width={460}>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="userName" label="用户名" rules={[{ required: true }]}>
            <Input placeholder="例如 jsmith" />
          </Form.Item>
          <Form.Item name="firstName" label="名" rules={[{ required: true }]}>
            <Input placeholder="例如 John" />
          </Form.Item>
          <Form.Item name="lastName" label="姓" rules={[{ required: true }]}>
            <Input placeholder="例如 Smith" />
          </Form.Item>
          <Form.Item name="phone" label="电话">
            <Input placeholder="例如 +86-138-0000-0000" />
          </Form.Item>
          <Form.Item name="affiliation" label="机构">
            <Input placeholder="例如 大学附属医院" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={`角色: ${selectedUser}`} open={roleOpen} onCancel={() => { setRoleOpen(false); roleForm.resetFields(); }}
        footer={null} width={500}>
        {selectedUser && (
          <div style={{ marginTop: 8 }}>
            <Tabs items={[
              {
                key: "current", label: "当前角色",
                children: roles.length === 0
                  ? <Text style={{ color: "var(--text-secondary)" }}>未分配角色</Text>
                  : <Table dataSource={roles} columns={[
                    { title: "角色", dataIndex: "roleName", key: "role" },
                    { title: "项目 ID", dataIndex: "studyId", key: "study", render: (v: number | null) => v ?? "全部" },
                    { title: "状态", dataIndex: "statusId", key: "status", render: (v: number) => v === 1 ? <span className="status status-success">正常</span> : <span className="status status-default">未激活</span> },
                  ]} rowKey="studyUserRoleId" pagination={false} size="small" />,
              },
              {
                key: "assign", label: "分配角色",
                children: (
                  <Form form={roleForm} layout="vertical" style={{ marginTop: 16 }}>
                    <Form.Item name="roleName" label="角色" rules={[{ required: true }]}>
                      <Select placeholder="选择角色">
                        <Select.Option value="admin">管理员</Select.Option>
                        <Select.Option value="coordinator">研究协调员</Select.Option>
                        <Select.Option value="investigator">研究者</Select.Option>
                        <Select.Option value="data_manager">数据管理员</Select.Option>
                        <Select.Option value="monitor">监查员</Select.Option>
                        <Select.Option value="pi">主要研究者</Select.Option>
                      </Select>
                    </Form.Item>
                    <Form.Item name="studyId" label="项目 ID（留空为全局）">
                      <Input type="number" placeholder="例如 1" />
                    </Form.Item>
                    <Button type="primary" onClick={handleAssignRole}>分配</Button>
                  </Form>
                ),
              },
            ]} />
          </div>
        )}
      </Modal>
    </div>
  );
}
