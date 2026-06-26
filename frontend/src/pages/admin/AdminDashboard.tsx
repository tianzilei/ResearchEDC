import { Card, Row, Col, Typography } from "antd";
import { useNavigate } from "react-router-dom";
const { Title } = Typography;

const ADMIN_SECTIONS = [
  { title: "用户管理", desc: "创建、编辑和管理用户账户和角色", path: "/app/admin/users" },
  { title: "审计日志", desc: "查看系统范围的审计追踪和用户活动", path: "/app/admin/audit-log" },
  { title: "系统配置", desc: "系统健康状态、版本信息和组件状态", path: "/app/admin/system" },
  { title: "CRF 库", desc: "管理病例报告表定义和版本", path: "/app/crfs" },
  { title: "数据导出", desc: "创建和管理数据导出任务", path: "/app/data-export" },
  { title: "安全设置", desc: "基于角色的权限配置", path: "/app/admin/users" },
];

export default function AdminDashboard() {
  const navigate = useNavigate();

  return (
    <div>
      <Title level={4} style={{ marginBottom: 20 }}>管理</Title>
      <Row gutter={[12, 12]}>
        {ADMIN_SECTIONS.map(section => (
          <Col xs={24} sm={12} lg={8} key={section.title}>
            <Card
              hoverable
              onClick={() => navigate(section.path)}
              style={{ height: "100%" }}
            >
              <div>
                <div style={{ fontWeight: 600, marginBottom: 4, color: "var(--text)" }}>{section.title}</div>
                <div style={{ fontSize: 13, color: "var(--text-secondary)" }}>{section.desc}</div>
              </div>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}