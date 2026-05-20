import { useState } from "react";
import { Card, Table, Tag, Button, Typography, Modal, Form, Input, Select, message, Tabs } from "antd";
import { PlusOutlined, UserOutlined, SafetyOutlined } from "@ant-design/icons";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface UserDTO {
  userId: number;
  userName: string;
  firstName: string;
  lastName: string;
  email: string;
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

  useState(() => {
    const fetchAll = async () => {
      try {
        const r = await fetch("/api/v1/identity/users?query=");
        if (r.ok) setUsers(await r.json());
      } catch { void 0; }
      setLoading(false);
    };
    fetchAll();
  });

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
          email: vals.email,
          phone: vals.phone ?? null,
          institutionalAffiliation: vals.affiliation ?? null,
          statusId: 1,
        }),
      });
      if (!res.ok) { const err = await res.text(); message.error(`Failed: ${err}`); return; }
      message.success("User created");
      setCreateOpen(false);
      form.resetFields();
      const r = await fetch("/api/v1/identity/users?query=");
      if (r.ok) setUsers(await r.json());
    } catch { void 0; }
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
      if (!res.ok) { message.error("Failed to assign role"); return; }
      message.success("Role assigned");
      setRoleOpen(false);
      roleForm.resetFields();
      const r = await fetch(`/api/v1/identity/roles/by-user?userName=${selectedUser}`);
      if (r.ok) setRoles(await r.json());
    } catch { void 0; }
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
      title: "Username", dataIndex: "userName", key: "userName",
      render: (text: string) => <strong><UserOutlined style={{ marginRight: 8 }} />{text}</strong>,
    },
    { title: "Name", key: "name", render: (_: any, r: UserDTO) => `${r.firstName} ${r.lastName}` },
    { title: "Email", dataIndex: "email", key: "email" },
    { title: "Phone", dataIndex: "phone", key: "phone", render: (v: string) => v || "-" },
    {
      title: "Status", dataIndex: "enabled", key: "enabled",
      render: (v: boolean) => v ? <Tag color="green">Active</Tag> : <Tag color="red">Disabled</Tag>,
    },
    {
      title: "", key: "actions",
      render: (_: any, record: UserDTO) => (
        <Button size="small" icon={<SafetyOutlined />} onClick={() => viewRoles(record.userName)}>
          Roles
        </Button>
      ),
    },
  ];

  return (
    <div style={{ padding: "24px 32px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <div>
          <Title level={3} style={{ margin: 0 }}>User Management</Title>
          <Text type="secondary">{users.length} users</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
          Create User
        </Button>
      </div>

      <Card style={{ borderRadius: 14, border: "1px solid var(--color-border-light, #E5E0D8)" }} styles={{ body: { padding: 0 } }}>
        <Table dataSource={users} columns={columns} rowKey="userId" pagination={{ pageSize: 20 }}
          locale={{ emptyText: "No users found" }} />
      </Card>

      <Modal title="Create User" open={createOpen} onOk={handleCreate}
        onCancel={() => { setCreateOpen(false); form.resetFields(); }} okText="Create" width={480}>
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="userName" label="Username" rules={[{ required: true }]}>
            <Input placeholder="e.g. jsmith" />
          </Form.Item>
          <Form.Item name="firstName" label="First Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. John" />
          </Form.Item>
          <Form.Item name="lastName" label="Last Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Smith" />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: "email" }]}>
            <Input placeholder="e.g. john@example.com" />
          </Form.Item>
          <Form.Item name="phone" label="Phone">
            <Input placeholder="e.g. +1-555-0100" />
          </Form.Item>
          <Form.Item name="affiliation" label="Institution">
            <Input placeholder="e.g. University Hospital" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={`Roles: ${selectedUser}`} open={roleOpen} onCancel={() => { setRoleOpen(false); roleForm.resetFields(); }}
        footer={null} width={520}>
        {selectedUser && (
          <div style={{ marginTop: 8 }}>
            <Tabs items={[
              {
                key: "current", label: "Current Roles",
                children: roles.length === 0
                  ? <Text type="secondary">No roles assigned</Text>
                  : <Table dataSource={roles} columns={[
                    { title: "Role", dataIndex: "roleName", key: "role" },
                    { title: "Study ID", dataIndex: "studyId", key: "study", render: (v: number) => v ?? "All" },
                    { title: "Status", dataIndex: "statusId", key: "status", render: (v: number) => v === 1 ? <Tag color="green">Active</Tag> : <Tag>Inactive</Tag> },
                  ]} rowKey="studyUserRoleId" pagination={false} size="small" />,
              },
              {
                key: "assign", label: "Assign Role",
                children: (
                  <Form form={roleForm} layout="vertical" style={{ marginTop: 16 }}>
                    <Form.Item name="roleName" label="Role" rules={[{ required: true }]}>
                      <Select placeholder="Select role">
                        <Select.Option value="admin">Admin</Select.Option>
                        <Select.Option value="coordinator">Study Coordinator</Select.Option>
                        <Select.Option value="investigator">Investigator</Select.Option>
                        <Select.Option value="data_manager">Data Manager</Select.Option>
                        <Select.Option value="monitor">Monitor</Select.Option>
                        <Select.Option value="pi">Principal Investigator</Select.Option>
                      </Select>
                    </Form.Item>
                    <Form.Item name="studyId" label="Study ID (leave empty for global)">
                      <Input type="number" placeholder="e.g. 1" />
                    </Form.Item>
                    <Button type="primary" onClick={handleAssignRole}>Assign</Button>
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
