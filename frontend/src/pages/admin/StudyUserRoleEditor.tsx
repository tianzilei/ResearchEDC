import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  Card, Typography, Table, Tag, Button, Space, Modal, Select,
  message, Spin, Empty, Breadcrumb, Result,
} from "antd";

import { useAppQuery } from "@/hooks/useQuery";
import { identityApi, type RoleDTO, type UserDTO } from "@/api/identity";
import { studyApi } from "@/api/studies";

const { Title, Text } = Typography;

const ROLE_OPTIONS = [
  { value: "admin", label: "管理员" },
  { value: "coordinator", label: "协调员" },
  { value: "investigator", label: "研究者" },
  { value: "dataManager", label: "数据管理员" },
  { value: "dataEntry", label: "数据录入" },
  { value: "monitor", label: "监查员" },
];

export default function StudyUserRoleEditor() {
  const { studyId } = useParams<{ studyId: string }>();
  const parsedStudyId = studyId ? Number(studyId) : undefined;
  const [assignOpen, setAssignOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<string>("");
  const [selectedRole, setSelectedRole] = useState<string>("");
  const [assigning, setAssigning] = useState(false);

  const { data: study, isLoading: loadingStudy, error: studyError } = useAppQuery({
    queryKey: ["studies", "detail", parsedStudyId],
    queryFn: () =>
      parsedStudyId
        ? studyApi.getDetail(parsedStudyId)
        : Promise.resolve(null),
    enabled: !!parsedStudyId,
  });
  const { data: users = [], isLoading: loadingRoles, refetch: refetchRoles } = useAppQuery<RoleDTO[]>({
    queryKey: ["identity", "roles", "by-study", parsedStudyId],
    queryFn: () =>
      parsedStudyId
        ? identityApi.listRolesByStudy(parsedStudyId)
        : Promise.resolve([]),
    enabled: !!parsedStudyId,
  });
  const { data: userList = [], isLoading: loadingUsers } = useAppQuery<UserDTO[]>({
    queryKey: ["identity", "users"],
    queryFn: () => identityApi.listUsers(),
  });

  const handleAssign = async () => {
    if (!selectedUser || !selectedRole || !parsedStudyId) return;
    setAssigning(true);
    try {
      await identityApi.assignRole({
        userName: selectedUser,
        studyId: parsedStudyId,
        roleName: selectedRole,
        statusId: 1,
      });
      message.success("Role assigned");
      setAssignOpen(false);
      setSelectedUser("");
      setSelectedRole("");
      void refetchRoles();
    } catch {
      message.error("Failed to assign role");
    } finally {
      setAssigning(false);
    }
  };

  const loading = loadingStudy || loadingRoles || loadingUsers;
  if (loading) return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  if (studyError) return <Result status="error" title="Failed to load data" />;

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
              <Title level={4} style={{ margin: 0 }}>研究用户 - {study?.name ?? `#${studyId}`}</Title>
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
          <Table dataSource={users} rowKey={(r: RoleDTO) => String(r.studyUserRoleId)}
            columns={[
              { title: "用户名", dataIndex: "userName", key: "userName" },
              { title: "姓名", key: "name", render: (_: unknown, r: RoleDTO) => r.userName },
              { title: "角色", dataIndex: "roleName", key: "role", render: (r: string) => <Tag>{r}</Tag> },
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
                label: `${u.userName} (${u.firstName ?? ""} ${u.lastName ?? ""})`,
              }))}
            />
          </div>
          <div>
            <Text strong>角色</Text>
            <Select style={{ width: "100%", marginTop: 4 }}
              placeholder="选择角色" value={selectedRole || undefined} onChange={setSelectedRole}
              options={ROLE_OPTIONS}
            />
          </div>
        </Space>
      </Modal>
    </div>
  );
}
