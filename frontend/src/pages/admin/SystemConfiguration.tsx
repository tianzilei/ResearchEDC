import { useState } from "react";
import { Card, Descriptions, Typography, Row, Col, Statistic } from "antd";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface HealthInfo {
  status: string;
  components?: Record<string, { status: string }>;
}

interface BuildInfo {
  version?: string;
  name?: string;
  time?: string;
}

export default function SystemConfiguration() {
  const [health, setHealth] = useState<HealthInfo | null>(null);
  const [buildInfo, setBuildInfo] = useState<BuildInfo | null>(null);
  const [loading, setLoading] = useState(true);

  useState(() => {
    Promise.all([
      fetch("/actuator/health").then(r => r.ok ? r.json() : null),
      fetch("/actuator/info").then(r => r.ok ? r.json() : null),
    ]).then(([h, b]) => {
      setHealth(h);
      setBuildInfo(b?.build ?? null);
      setLoading(false);
    }).catch(() => setLoading(false));
  });

  if (loading) return <SkeletonPage />;

  const dbHealth = health?.components?.db?.status ?? "UNKNOWN";
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
              prefix={undefined}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="版本"
              value={buildInfo?.version ?? "0.1"}
              prefix={undefined}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="数据库"
              value={dbHealth}
              valueStyle={{ color: dbHealth === "UP" ? "var(--success)" : "var(--danger)" }}
              prefix={undefined}
            />
          </Card>
        </Col>
      </Row>

      <Card title="应用信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2} size="small" bordered>
          <Descriptions.Item label="名称">{buildInfo?.name ?? "ResearchEDC"}</Descriptions.Item>
          <Descriptions.Item label="版本">{buildInfo?.version ?? "0.1"}</Descriptions.Item>
          <Descriptions.Item label="构建时间">{buildInfo?.time ?? "-"}</Descriptions.Item>
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
                <span className={val.status === "UP" ? "status status-success" : "status status-danger"}>{val.status}</span>
              </Descriptions.Item>
            ))}
          </Descriptions>
        ) : (
          <Text type="secondary">健康检查端点不可用</Text>
        )}
      </Card>
    </div>
  );
}
