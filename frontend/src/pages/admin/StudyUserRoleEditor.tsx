import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  Card, Typography, Table, Tag, Button, Space, Modal, Select,
  message, Spin, Empty, Breadcrumb, Result,
} from "antd";
import { UserOutlined, PlusOutlined } from "@ant-design/icons";

const { Title, Text } = Typography;

interface UserRole {
  userName: string;
  firstName: string;
  lastName: string;
  role: string;
}

interface StudyInfo {
  studyId: number;
  name: string;
}

export default function StudyUserRoleEditor() {
  const { studyId } = useParams<{ studyId: string }>();
  const [users, setUsers] = useState<UserRole[]>([]);
  const [study, setStudy] = useState<StudyInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [assignOpen, setAssignOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<string>("");
  const [selectedRole, setSelectedRole] = useState<string>("");
  const [userList, setUserList] = useState<{ userName: string; firstName: string; lastName: string }[]>([]);
  const [assigning, setAssigning] = useState(false);

  useEffect(() => {
    if (!studyId) return;
    setLoading(true);
    Promise.all([
      fetch(`/api/v1/studies/${studyId}`).then(r => r.ok ? r.json() : null),
      fetch(`/api/v1/identity/roles/by-study?studyId=${studyId}`).then(r => r.ok ? r.json() : []),
      fetch("/api/v1/identity/users?query=").then(r => r.ok ? r.json() : []),
    ]).then(([studyData, rolesData, usersData]) => {
      setStudy(studyData);
      setUsers(Array.isArray(rolesData) ? rolesData : []);
      setUserList(Array.isArray(usersData) ? usersData : []);
      setLoading(false);
    }).catch(() => { setError("Failed to load data"); setLoading(false); });
  }, [studyId]);

  const handleAssign = async () => {
    if (!selectedUser || !selectedRole || !studyId) return;
    setAssigning(true);
    try {
      const res = await fetch("/api/v1/identity/roles/assign", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userName: selectedUser, studyId: Number(studyId), role: selectedRole }),
      });
      if (res.ok) {
        message.success("Role assigned");
        setAssignOpen(false);
        setSelectedUser("");
        setSelectedRole("");
        const r = await fetch(`/api/v1/identity/roles/by-study?studyId=${studyId}`);
        if (r.ok) setUsers(await r.json());
      } else {
        message.error("Failed to assign role");
      }
    } catch { message.error("Failed to assign role"); }
    setAssigning(false);
  };

  if (loading) return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  if (error) return <Result status="error" title={error} />;

  return (
    <div>
      <Breadcrumb items={[
        { title: <Link to="/app/admin">Admin</Link> },
        { title: <Link to="/app/admin/users">Users</Link> },
        { title: study?.name ?? `Study #${studyId}` },
      ]} style={{ marginBottom: 16 }} />

      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <Space style={{ width: "100%", justifyContent: "space-between" }} align="center">
          <Space>
            <UserOutlined style={{ fontSize: 22, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>Study Users — {study?.name ?? `#${studyId}`}</Title>
              <Text type="secondary">{users.length} user{users.length !== 1 ? "s" : ""} assigned</Text>
            </div>
          </Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setAssignOpen(true)}>
            Assign User
          </Button>
        </Space>
      </Card>

      {users.length === 0 ? (
        <Card style={{ borderRadius: 14 }}><Empty description="No users assigned to this study" /></Card>
      ) : (
        <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
          <Table dataSource={users} rowKey={(r: any) => r.userName ?? r.userId}
            columns={[
              { title: "Username", dataIndex: "userName", key: "userName" },
              { title: "Name", key: "name", render: (_: any, r: any) => `${r.firstName ?? ""} ${r.lastName ?? ""}`.trim() || "-" },
              { title: "Role", dataIndex: "role", key: "role", render: (r: string) => <Tag>{r}</Tag> },
            ]}
            pagination={false}
          />
        </Card>
      )}

      <Modal title="Assign User to Study" open={assignOpen}
        onOk={handleAssign} onCancel={() => setAssignOpen(false)}
        confirmLoading={assigning}>
        <Space direction="vertical" style={{ width: "100%", marginTop: 16 }}>
          <div>
            <Text strong>User</Text>
            <Select style={{ width: "100%", marginTop: 4 }}
              placeholder="Select user" showSearch value={selectedUser || undefined}
              onChange={setSelectedUser}
              filterOption={(input, option) => (option?.label ?? "").toLowerCase().includes(input.toLowerCase())}
              options={userList.map(u => ({
                value: u.userName,
                label: `${u.userName} (${u.firstName} ${u.lastName})`,
              }))}
            />
          </div>
          <div>
            <Text strong>Role</Text>
            <Select style={{ width: "100%", marginTop: 4 }}
              placeholder="Select role" value={selectedRole || undefined} onChange={setSelectedRole}
              options={[
                { value: "admin", label: "Admin" },
                { value: "coordinator", label: "Coordinator" },
                { value: "investigator", label: "Investigator" },
                { value: "dataManager", label: "Data Manager" },
                { value: "dataEntry", label: "Data Entry" },
                { value: "monitor", label: "Monitor" },
              ]}
            />
          </div>
        </Space>
      </Modal>
    </div>
  );
}
