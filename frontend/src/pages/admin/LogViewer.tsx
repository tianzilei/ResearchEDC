import { useState } from "react";
import {
  Card, Typography, Table, Tag, Space, Button, Breadcrumb, Empty, Result,
} from "antd";
import { ReloadOutlined, FileTextOutlined } from "@ant-design/icons";
import { Link } from "react-router-dom";

const { Title, Text } = Typography;

export default function LogViewer() {
  const [logs, setLogs] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchLogs = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("/actuator/loggers");
      if (res.ok) {
        const data = await res.json();
        setLogs(Object.entries(data.loggers ?? {}).slice(0, 50).map(([name, config]: [string, any]) => ({
          name, level: config.configuredLevel ?? config.effectiveLevel ?? "INFO",
        })));
      } else {
        setError("Actuator endpoint not available");
      }
    } catch {
      setError("Actuator endpoint not available");
    }
    setLoading(false);
  };

  return (
    <div>
      <Breadcrumb items={[
        { title: <Link to="/app/admin">Admin</Link> },
        { title: "Log Viewer" },
      ]} style={{ marginBottom: 16 }} />

      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <Space style={{ width: "100%", justifyContent: "space-between" }} align="center">
          <Space>
            <FileTextOutlined style={{ fontSize: 22, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>Logger Configuration</Title>
              <Text type="secondary">View and configure log levels</Text>
            </div>
          </Space>
          <Button icon={<ReloadOutlined />} onClick={fetchLogs} loading={loading}>
            Load Loggers
          </Button>
        </Space>
      </Card>

      {error ? (
        <Result status="warning" title="Log Viewer Unavailable"
          subTitle={error}
          extra={<Button onClick={() => window.open("/legacy/ViewLogMessage", "_blank")}>Open Legacy Log Viewer</Button>}
        />
      ) : logs.length === 0 && !loading ? (
        <Card style={{ borderRadius: 14 }}>
          <Empty description="Click 'Load Loggers' to view logger configuration">
            <Button type="primary" onClick={fetchLogs}>Load Loggers</Button>
          </Empty>
        </Card>
      ) : (
        <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
          <Table
            dataSource={logs}
            columns={[
              { title: "Logger", dataIndex: "name", key: "name", ellipsis: true },
              {
                title: "Level", dataIndex: "level", key: "level",
                render: (level: string) => {
                  const color = level === "ERROR" ? "red" : level === "WARN" ? "orange" : level === "DEBUG" ? "blue" : "default";
                  return <Tag color={color}>{level}</Tag>;
                },
              },
            ]}
            rowKey="name"
            pagination={{ pageSize: 20 }}
            size="small"
          />
        </Card>
      )}
    </div>
  );
}
