import { useParams, useNavigate } from "react-router-dom";
import { Card, Descriptions, Tag, Button, Space, Typography, Table, Divider, message, Alert, Modal } from "antd";
import { SafetyOutlined, ArrowLeftOutlined } from "@ant-design/icons";
import { useScheme, useActivateScheme, useCloseScheme } from "@/hooks/useRandomization";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title } = Typography;

const algorithmColors: Record<string, string> = {
  SIMPLE: "green", BLOCK: "blue", STRATIFIED_BLOCK: "purple",
};

const statusColors: Record<string, string> = {
  DRAFT: "default", ACTIVE: "success", PAUSED: "warning", CLOSED: "error",
};

export default function SchemeEditor() {
  const { id } = useParams<{ id: string }>();
  const schemeId = Number(id);
  const { data: scheme, isLoading } = useScheme(schemeId);
  const activateScheme = useActivateScheme();
  const closeScheme = useCloseScheme();
  const navigate = useNavigate();

  if (isLoading) return <SkeletonPage />;
  if (!scheme) {
    return <Alert message="Scheme not found" type="error" showIcon />;
  }

  const handleActivate = () => {
    Modal.confirm({
      title: "Activate Scheme",
      content: "Once activated, subjects can be randomized using this scheme. The configuration will be locked.",
      onOk: async () => {
        await activateScheme.mutateAsync(schemeId);
        message.success("Scheme activated");
      },
    });
  };

  const handleClose = () => {
    Modal.confirm({
      title: "Close Scheme",
      content: "Closing will stop all new randomizations. Existing assignments remain valid.",
      onOk: async () => {
        await closeScheme.mutateAsync(schemeId);
        message.success("Scheme closed");
      },
    });
  };

  const armColumns = [
    { title: "#", dataIndex: "orderNumber", key: "orderNumber", width: 60 },
    { title: "Name", dataIndex: "name", key: "name" },
    { title: "Display Name", dataIndex: "displayName", key: "displayName" },
    { title: "Ratio", dataIndex: "ratio", key: "ratio" },
  ];

  const stratumColumns = [
    { title: "#", dataIndex: "orderNumber", key: "orderNumber", width: 60 },
    { title: "Name", dataIndex: "name", key: "name" },
    { title: "Type", dataIndex: "stratumType", key: "stratumType",
      render: (t: string) => <Tag>{t}</Tag>,
    },
    { title: "Options", dataIndex: "options", key: "options",
      render: (opts: any[]) => opts?.map((o: any) => o.label).join(", ") ?? "-",
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate("/app/randomization")}>
          Back
        </Button>
      </Space>

      <Card>
        <Space style={{ justifyContent: "space-between", width: "100%" }}>
          <Space>
            <SafetyOutlined style={{ fontSize: 24 }} />
            <Title level={4} style={{ margin: 0 }}>{scheme.name}</Title>
          </Space>
          <Space>
            {scheme.status === "DRAFT" && (
              <Button type="primary" onClick={handleActivate} loading={activateScheme.isPending}>
                Activate
              </Button>
            )}
            {scheme.status === "ACTIVE" && (
              <Button danger onClick={handleClose} loading={closeScheme.isPending}>
                Close
              </Button>
            )}
          </Space>
        </Space>

        <Divider />

        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="Algorithm">
            <Tag color={algorithmColors[scheme.algorithm]}>{scheme.algorithm}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Status">
            <Tag color={statusColors[scheme.status ?? "DRAFT"]}>{scheme.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Min Block Size">{scheme.minBlockSize ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Max Block Size">{scheme.maxBlockSize ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Total Assigned">{scheme.totalAssigned ?? 0}</Descriptions.Item>
          <Descriptions.Item label="Seed">{scheme.seed ?? "-"}</Descriptions.Item>
        </Descriptions>

        <Divider orientation="left">Study Arms</Divider>
        <Table dataSource={scheme.arms} columns={armColumns} rowKey="id" pagination={false} size="small" />

        {(scheme.stratifications?.length ?? 0) > 0 && (
          <>
            <Divider orientation="left">Stratification Factors</Divider>
            <Table dataSource={scheme.stratifications} columns={stratumColumns} rowKey="id" pagination={false} size="small" />
          </>
        )}
      </Card>
    </div>
  );
}
