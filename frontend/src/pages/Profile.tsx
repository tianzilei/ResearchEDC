import { useEffect, useState } from "react";
import { Card, Descriptions, Typography, Spin, Button, Space, Tag } from "antd";
import { UserOutlined, ExperimentOutlined, LogoutOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/providers/AuthProvider";
import { useCurrentStudy } from "@/hooks/useStudies";

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
  const { currentStudy, clearCurrentStudy } = useCurrentStudy();
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
    clearCurrentStudy();
    await logout();
    void navigate("/login");
  };

  if (loading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 80 }}><Spin size="large" /></div>;
  }

  return (
    <div style={{ maxWidth: 720, margin: "0 auto" }}>
      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "24px 32px" } }}>
        <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
          <div style={{ width: 64, height: 64, borderRadius: "50%", background: "var(--color-primary, #099A87)", display: "flex", alignItems: "center", justifyContent: "center" }}>
            <UserOutlined style={{ fontSize: 28, color: "#fff" }} />
          </div>
          <div>
            <Title level={4} style={{ margin: 0 }}>{profile?.firstName} {profile?.lastName}</Title>
            <Text type="secondary">{profile?.email}</Text>
            <div style={{ marginTop: 4 }}><Tag>{profile?.userType ?? "User"}</Tag></div>
          </div>
        </div>
      </Card>

      <Card style={{ marginBottom: 16, borderRadius: 14 }} title="Account Details">
        <Descriptions column={1} bordered size="small">
          <Descriptions.Item label="Username">{profile?.userName ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="First Name">{profile?.firstName ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Last Name">{profile?.lastName ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Email">{profile?.email ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Phone">{profile?.phone ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Institution">{profile?.institution ?? "-"}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card style={{ marginBottom: 16, borderRadius: 14 }} title="Current Study">
        {currentStudy ? (
          <Space>
            <ExperimentOutlined style={{ color: "var(--color-primary, #099A87)" }} />
            <Text strong>{currentStudy.name}</Text>
            <Button size="small" onClick={() => navigate("/app/studies")}>Switch Study</Button>
          </Space>
        ) : (
          <Space direction="vertical">
            <Text type="secondary">No study selected</Text>
            <Button type="primary" onClick={() => navigate("/app/studies")}>Select Study</Button>
          </Space>
        )}
      </Card>

      <Card style={{ borderRadius: 14 }}>
        <Button danger icon={<LogoutOutlined />} onClick={() => { void handleLogout(); }}>Sign Out</Button>
      </Card>
    </div>
  );
}
