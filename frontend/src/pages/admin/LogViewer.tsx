import { useState } from "react";
import {
  Card, Typography, Table, Space, Button, Breadcrumb, Empty, Result,
} from "antd";
import { Link } from "react-router-dom";

const { Title, Text } = Typography;

export default function LogViewer() {
  const [logs, setLogs] = useState<{ name: string; level: string }[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchLogs = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("/actuator/loggers");
      if (res.ok) {
        const data = await res.json();
        setLogs(Object.entries<Record<string, string | null>>(data.loggers ?? {}).slice(0, 50).map(([name, config]) => ({
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
        { title: <Link to="/app/admin">管理</Link> },
        { title: "日志查看器" },
      ]} style={{ marginBottom: 16 }} />

      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: "16px 24px" } }}>
        <Space style={{ width: "100%", justifyContent: "space-between" }} align="center">
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>日志配置</Title>
              <Text type="secondary">查看和配置日志级别</Text>
            </div>
          </Space>
          <Button onClick={fetchLogs} loading={loading}>
            加载日志器
          </Button>
        </Space>
      </Card>

      {error ? (
        <Result status="warning" title="日志查看器不可用"
          subTitle={error}
        />
      ) : logs.length === 0 && !loading ? (
        <Card>
          <Empty description="点击「加载日志器」查看日志配置">
            <Button type="primary" onClick={fetchLogs}>加载日志器</Button>
          </Empty>
        </Card>
      ) : (
        <Card styles={{ body: { padding: 0 } }}>
          <Table
            dataSource={logs}
            columns={[
              { title: "日志器", dataIndex: "name", key: "name", ellipsis: true },
              {
                title: "级别", dataIndex: "level", key: "level",
                render: (level: string) => <span className="status status-default">{level}</span>,
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
