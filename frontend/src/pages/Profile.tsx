import { useEffect, useState, useCallback } from "react";
import {
  Card,
  Descriptions,
  Typography,
  Spin,
  Button,
  Tag,
  Form,
  Input,
  Modal,
  message,
  Divider,
} from "antd";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/providers/AuthProvider";
import { apiClient } from "@/api/client";

const { Title } = Typography;

interface StudyRole {
  studyUserRoleId: number;
  roleName: string;
  studyId: number;
}

interface UserProfile {
  userId: number;
  userName: string;
  firstName: string;
  lastName: string;
  phone: string;
  institutionalAffiliation: string;
  userType: string;
  enabled: boolean;
  activeStudyId: number | null;
}

interface ProfileFormValues {
  firstName: string;
  lastName: string;
  phone: string;
  institution: string;
}

interface PasswordFormValues {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export default function Profile() {
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [studyRoles, setStudyRoles] = useState<StudyRole[]>([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);
  const [editForm] = Form.useForm<ProfileFormValues>();
  const [passwordForm] = Form.useForm<PasswordFormValues>();

  const fetchProfile = useCallback(async () => {
    if (!user) return;
    try {
      const data = await apiClient.get<UserProfile>(
        `/api/v1/identity/users/by-username?username=${encodeURIComponent(user.username)}`,
      );
      setProfile(data);
    } catch {
    } finally {
      setLoading(false);
    }
  }, [user]);

  const fetchRoles = useCallback(async () => {
    if (!user) return;
    try {
      const roles = await apiClient.get<StudyRole[]>("/api/v1/identity/roles/by-user", {
        userName: user.username,
      });
      setStudyRoles(roles);
    } catch {
    }
  }, [user]);

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return;
    }
    void fetchProfile();
    void fetchRoles();
  }, [user, fetchProfile, fetchRoles]);

  const startEdit = () => {
    if (!profile) return;
    editForm.setFieldsValue({
      firstName: profile.firstName,
      lastName: profile.lastName,
      phone: profile.phone ?? "",
      institution: profile.institutionalAffiliation ?? "",
    });
    setEditing(true);
  };

  const cancelEdit = () => {
    setEditing(false);
    editForm.resetFields();
  };

  const handleSaveProfile = async () => {
    if (!profile) return;
    try {
      const values = await editForm.validateFields();
      setSaving(true);
      await apiClient.put(`/api/v1/identity/users/${profile.userId}/profile`, values);
      message.success("资料已更新");
      setEditing(false);
      await fetchProfile();
    } catch (err: unknown) {
      if (err instanceof Error && err.message) {
        message.error(err.message);
      }
    } finally {
      setSaving(false);
    }
  };

  const handleChangePassword = async () => {
    if (!profile) return;
    try {
      const values = await passwordForm.validateFields();
      if (values.newPassword !== values.confirmPassword) {
        message.error("两次输入的密码不一致");
        return;
      }
      setChangingPassword(true);
      await apiClient.put(`/api/v1/identity/users/${profile.userId}/password`, {
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
      });
      message.success("密码已修改");
      setPasswordModalOpen(false);
      passwordForm.resetFields();
    } catch (err: unknown) {
      if (err instanceof Error && err.message) {
        message.error(err.message);
      }
    } finally {
      setChangingPassword(false);
    }
  };

  const handleLogout = () => {
    logout();
    void navigate("/login");
  };

  if (loading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", padding: 80 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 720, margin: "0 auto" }}>
      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: "24px 32px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>
              {profile?.firstName} {profile?.lastName}
            </Title>
            <div style={{ marginTop: 4 }}>
              <Tag>{profile?.userType ?? "用户"}</Tag>
            </div>
          </div>
          <div style={{ display: "flex", gap: 8 }}>
            {!editing && (
              <Button onClick={startEdit}>编辑资料</Button>
            )}
            <Button onClick={() => setPasswordModalOpen(true)}>修改密码</Button>
          </div>
        </div>
      </Card>

      <Card style={{ marginBottom: 16 }} title="账户详情">
        {editing ? (
          <Form form={editForm} layout="vertical">
            <Form.Item
              name="firstName"
              label="名"
              rules={[{ required: true, message: "请输入名" }, { max: 60 }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
              name="lastName"
              label="姓"
              rules={[{ required: true, message: "请输入姓" }, { max: 60 }]}
            >
              <Input />
            </Form.Item>
            <Form.Item name="phone" label="电话" rules={[{ max: 255 }]}>
              <Input />
            </Form.Item>
            <Form.Item name="institution" label="机构" rules={[{ max: 255 }]}>
              <Input />
            </Form.Item>
            <div style={{ display: "flex", gap: 8 }}>
              <Button type="primary" loading={saving} onClick={handleSaveProfile}>
                保存
              </Button>
              <Button onClick={cancelEdit}>取消</Button>
            </div>
          </Form>
        ) : (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="用户名">{profile?.userName ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="名">{profile?.firstName ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="姓">{profile?.lastName ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="电话">{profile?.phone ?? "-"}</Descriptions.Item>
            <Descriptions.Item label="机构">
              {profile?.institutionalAffiliation ?? "-"}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Card>

      {studyRoles.length > 0 && (
        <Card title="研究角色" style={{ marginBottom: 16 }}>
          <Divider style={{ margin: "0 0 12px 0" }} />
          {studyRoles.map((role) => (
            <Tag key={role.studyUserRoleId} style={{ marginTop: 4, marginRight: 8 }}>
              研究 #{role.studyId}: {role.roleName}
            </Tag>
          ))}
        </Card>
      )}

      <Card>
        <Button danger onClick={handleLogout}>
          退出登录
        </Button>
      </Card>

      <Modal
        title="修改密码"
        open={passwordModalOpen}
        onOk={handleChangePassword}
        onCancel={() => {
          setPasswordModalOpen(false);
          passwordForm.resetFields();
        }}
        confirmLoading={changingPassword}
        okText="确认修改"
        cancelText="取消"
        destroyOnHidden
      >
        <Form form={passwordForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="oldPassword"
            label="当前密码"
            rules={[{ required: true, message: "请输入当前密码" }]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[
              { required: true, message: "请输入新密码" },
              { min: 6, message: "密码至少6个字符" },
            ]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label="确认新密码"
            dependencies={["newPassword"]}
            rules={[
              { required: true, message: "请确认新密码" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue("newPassword") === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error("两次输入的密码不一致"));
                },
              }),
            ]}
          >
            <Input.Password />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
