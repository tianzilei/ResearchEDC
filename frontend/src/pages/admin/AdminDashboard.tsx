import { Card, Row, Col, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { UserOutlined, SafetyOutlined, InfoCircleOutlined, FileTextOutlined, SettingOutlined, AuditOutlined } from "@ant-design/icons";

const { Title } = Typography;

const ADMIN_SECTIONS = [
  { title: "User Management", desc: "Create, edit, and manage user accounts and roles", icon: <UserOutlined />, path: "/app/admin/users", color: "#099A87" },
  { title: "Audit Log", desc: "View system-wide audit trail and user activity", icon: <AuditOutlined />, path: "/app/admin/audit-log", color: "#D4A854" },
  { title: "System Config", desc: "System health, version info, and component status", icon: <InfoCircleOutlined />, path: "/app/admin/system", color: "#4F46E5" },
  { title: "CRF Library", desc: "Manage case report form definitions and versions", icon: <FileTextOutlined />, path: "/app/crfs", color: "#0891B2" },
  { title: "Data Export", desc: "Create and manage data export jobs", icon: <SettingOutlined />, path: "/app/data-export", color: "#7C3AED" },
  { title: "Security", desc: "Role-based access and permission configuration", icon: <SafetyOutlined />, path: "/app/admin/users", color: "#DC2626" },
];

export default function AdminDashboard() {
  const navigate = useNavigate();

  return (
    <div style={{ padding: "24px 32px" }}>
      <Title level={3} style={{ marginBottom: 24 }}>Administration</Title>
      <Row gutter={[16, 16]}>
        {ADMIN_SECTIONS.map(section => (
          <Col xs={24} sm={12} lg={8} key={section.title}>
            <Card
              hoverable
              onClick={() => navigate(section.path)}
              style={{ borderRadius: 14, border: "1px solid var(--color-border-light, #E5E0D8)", height: "100%" }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
                <div style={{
                  width: 48, height: 48, borderRadius: 12,
                  background: `${section.color}15`,
                  display: "flex", alignItems: "center", justifyContent: "center",
                  fontSize: 22, color: section.color,
                }}>
                  {section.icon}
                </div>
                <div>
                  <div style={{ fontWeight: 600, marginBottom: 4 }}>{section.title}</div>
                  <div style={{ fontSize: 13, color: "#6B7280" }}>{section.desc}</div>
                </div>
              </div>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
