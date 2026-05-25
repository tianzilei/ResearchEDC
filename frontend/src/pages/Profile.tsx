import { useEffect, useState } from "react";
import { Card, Descriptions, Typography, Spin, Button, Tag } from "antd";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/providers/AuthProvider";

const { Title, Text } = Typography;

interface UserProfile {
  userId: number;
  userName: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  institution: string;
  userType: string;
  studyRoles: { studyId: number; studyName: string; role: string }[];
}

export default function Profile() {
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return;
    }
    fetch(`/api/v1/identity/users/by-username?username=${encodeURIComponent(user.username)}`)
      .then((r) => (r.ok ? r.json() : null))
      .then((data: UserProfile | null) => {
        if (data) setProfile(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, [user]);

  const handleLogout = async () => {
    await logout();
    void navigate("/login");
  };

  if (loading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 80 }}><Spin size="large" /></div>;
  }

  return (
    <div style={{ maxWidth: 720, margin: "0 auto" }}>
      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: "24px 32px" } }}>
        <div>
          <Title level={4} style={{ margin: 0 }}>{profile?.firstName} {profile?.lastName}</Title>
          <Text type="secondary">{profile?.email}</Text>
          <div style={{ marginTop: 4 }}><Tag>{profile?.userType ?? "用户"}</Tag></div>
        </div>
      </Card>

      <Card style={{ marginBottom: 16 }} title="账户详情">
        <Descriptions column={1} bordered size="small">
          <Descriptions.Item label="用户名">{profile?.userName ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="名">{profile?.firstName ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="姓">{profile?.lastName ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="邮箱">{profile?.email ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="电话">{profile?.phone ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="机构">{profile?.institution ?? "-"}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card>
        <Button danger onClick={() => { void handleLogout(); }}>退出登录</Button>
      </Card>
    </div>
  );
}
