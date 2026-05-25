import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  Card, Typography, Table, Tag, Button, Space, Modal, Select,
  message, Spin, Empty, Breadcrumb, Result,
} from "antd";


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
        { title: <Link to="/app/admin">管理</Link> },
        { title: <Link to="/app/admin/users">用户</Link> },
        { title: study?.name ?? `研究 #${studyId}` },
      ]} style={{ marginBottom: 16 }} />

      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: "16px 24px" } }}>
        <Space style={{ width: "100%", justifyContent: "space-between" }} align="center">
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>研究用户 — {study?.name ?? `#${studyId}`}</Title>
              <Text type="secondary">已分配 {users.length} 个用户</Text>
            </div>
          </Space>
          <Button type="primary" onClick={() => setAssignOpen(true)}>
            分配用户
          </Button>
        </Space>
      </Card>

      {users.length === 0 ? (
        <Card><Empty description="暂无用户分配至此研究" /></Card>
      ) : (
        <Card styles={{ body: { padding: 0 } }}>
          <Table dataSource={users} rowKey={(r: any) => r.userName ?? r.userId}
            columns={[
              { title: "用户名", dataIndex: "userName", key: "userName" },
              { title: "姓名", key: "name", render: (_: any, r: any) => `${r.firstName ?? ""} ${r.lastName ?? ""}`.trim() || "-" },
              { title: "角色", dataIndex: "role", key: "role", render: (r: string) => <Tag>{r}</Tag> },
            ]}
            pagination={false}
          />
        </Card>
      )}

      <Modal title="分配用户至研究" open={assignOpen}
        onOk={handleAssign} onCancel={() => setAssignOpen(false)}
        confirmLoading={assigning}>
        <Space direction="vertical" style={{ width: "100%", marginTop: 16 }}>
          <div>
            <Text strong>用户</Text>
            <Select style={{ width: "100%", marginTop: 4 }}
              placeholder="选择用户" showSearch value={selectedUser || undefined}
              onChange={setSelectedUser}
              filterOption={(input, option) => (option?.label ?? "").toLowerCase().includes(input.toLowerCase())}
              options={userList.map(u => ({
                value: u.userName,
                label: `${u.userName} (${u.firstName} ${u.lastName})`,
              }))}
            />
          </div>
          <div>
            <Text strong>角色</Text>
            <Select style={{ width: "100%", marginTop: 4 }}
              placeholder="选择角色" value={selectedRole || undefined} onChange={setSelectedRole}
              options={[
                { value: "admin", label: "管理员" },
                { value: "coordinator", label: "协调员" },
                { value: "investigator", label: "研究者" },
                { value: "dataManager", label: "数据管理员" },
                { value: "dataEntry", label: "数据录入" },
                { value: "monitor", label: "监察员" },
              ]}
            />
          </div>
        </Space>
      </Modal>
    </div>
  );
}
