import { useEffect, useState } from "react";
import { Card, Descriptions, Typography, Row, Col, Statistic } from "antd";
import { SkeletonPage } from "@/components/SkeletonCard";
import { apiClient } from "@/api/client";

const { Title, Text } = Typography;

interface HealthInfo {
  status: string;
  components?: Record<string, { status: string; details?: Record<string, unknown> }>;
}

interface DashboardStatus {
  database: string;
  backgroundTasks: string;
  lastBackup: string | null;
}

export default function SystemConfiguration() {
  const [health, setHealth] = useState<HealthInfo | null>(null);
  const [dbStatus, setDbStatus] = useState<string>("UNKNOWN");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      apiClient.get<DashboardStatus>("/api/v1/dashboard/status").catch(() => null),
      apiClient.get<HealthInfo>("/api/v1/dashboard/health").catch(() => null),
    ]).then(([status, healthInfo]) => {
      if (healthInfo) {
        setHealth(healthInfo);
      } else {
        // Fallback: if health endpoint unavailable, derive from dashboard status
        setHealth({
          status: status ? "UP" : "DOWN",
        });
      }
      setDbStatus(status?.database ?? "UNKNOWN");
      setLoading(false);
    });
  }, []);

  if (loading) return <SkeletonPage />;

  const isHealthy = health?.status === "UP";

  return (
    <div style={{ padding: "24px 32px", maxWidth: 900 }}>
      <Title level={3} style={{ marginBottom: 24 }}>
        系统配置
      </Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="系统状态"
              value={isHealthy ? "正常" : "异常"}
              valueStyle={{ color: isHealthy ? "var(--success)" : "var(--danger)" }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="版本"
              value="0.1"
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="数据库"
              value={dbStatus === "normal" ? "正常" : dbStatus === "error" ? "异常" : dbStatus}
              valueStyle={{ color: dbStatus === "normal" ? "var(--success)" : dbStatus === "error" ? "var(--danger)" : "var(--text-secondary)" }}
            />
          </Card>
        </Col>
      </Row>

      <Card title="应用信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2} size="small" bordered>
          <Descriptions.Item label="名称">ResearchEDC</Descriptions.Item>
          <Descriptions.Item label="版本">0.1</Descriptions.Item>
          <Descriptions.Item label="Java 版本">21</Descriptions.Item>
          <Descriptions.Item label="Spring Boot">3.2.5</Descriptions.Item>
          <Descriptions.Item label="数据库">PostgreSQL 17</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="组件状态">
        {health?.components ? (
          <Descriptions column={2} size="small" bordered>
            {Object.entries(health.components).map(([key, val]) => (
              <Descriptions.Item label={key} key={key}>
                <span className={val.status === "UP" ? "status status-success" : "status status-danger"}>
                  {val.status === "UP" ? "正常" : "异常"}
                </span>
              </Descriptions.Item>
            ))}
          </Descriptions>
        ) : (
          <Text type="secondary">使用 REST API 获取状态</Text>
        )}
      </Card>
    </div>
  );
}
