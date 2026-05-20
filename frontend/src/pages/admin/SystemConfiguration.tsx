import { useState } from "react";
import { Card, Descriptions, Tag, Typography, Row, Col, Statistic } from "antd";
import { CheckCircleOutlined, CloseCircleOutlined, InfoCircleOutlined, DatabaseOutlined } from "@ant-design/icons";
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
        <InfoCircleOutlined style={{ marginRight: 12 }} />System Configuration
      </Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="System Status"
              value={isHealthy ? "Healthy" : "Unhealthy"}
              valueStyle={{ color: isHealthy ? "#099A87" : "#FF4D4F" }}
              prefix={isHealthy ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Version"
              value={buildInfo?.version ?? "3.18-SNAPSHOT"}
              prefix={<InfoCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="Database"
              value={dbHealth}
              valueStyle={{ color: dbHealth === "UP" ? "#099A87" : "#FF4D4F" }}
              prefix={<DatabaseOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Card title="Application Info" style={{ borderRadius: 14, marginBottom: 16, border: "1px solid var(--color-border-light, #E5E0D8)" }}>
        <Descriptions column={2} size="small" bordered>
          <Descriptions.Item label="Name">{buildInfo?.name ?? "ResearchEDC"}</Descriptions.Item>
          <Descriptions.Item label="Version">{buildInfo?.version ?? "3.18-SNAPSHOT"}</Descriptions.Item>
          <Descriptions.Item label="Build Time">{buildInfo?.time ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Java Version">21</Descriptions.Item>
          <Descriptions.Item label="Spring Boot">3.2.5</Descriptions.Item>
          <Descriptions.Item label="Database">PostgreSQL 17</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="Component Status" style={{ borderRadius: 14, border: "1px solid var(--color-border-light, #E5E0D8)" }}>
        {health?.components ? (
          <Descriptions column={2} size="small" bordered>
            {Object.entries(health.components).map(([key, val]) => (
              <Descriptions.Item label={key} key={key}>
                <Tag color={val.status === "UP" ? "green" : "red"}>{val.status}</Tag>
              </Descriptions.Item>
            ))}
          </Descriptions>
        ) : (
          <Text type="secondary">Health endpoint not available</Text>
        )}
      </Card>
    </div>
  );
}
