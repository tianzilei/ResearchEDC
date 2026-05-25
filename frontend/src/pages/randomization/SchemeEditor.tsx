import { useParams, useNavigate } from "react-router-dom";
import { Card, Descriptions, Tag, Button, Space, Typography, Table, Divider, message, Alert, Modal } from "antd";
import { useTranslation } from "react-i18next";
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
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const schemeId = Number(id);
  const { data: scheme, isLoading } = useScheme(schemeId);
  const activateScheme = useActivateScheme();
  const closeScheme = useCloseScheme();
  const navigate = useNavigate();

  if (isLoading) return <SkeletonPage />;
  if (!scheme) {
    return <Alert message={t("scheme.notFound")} type="error" showIcon />;
  }

  const handleActivate = () => {
    Modal.confirm({
      title: t("scheme.activateConfirm"),
      content: t("scheme.activateMessage"),
      onOk: async () => {
        await activateScheme.mutateAsync(schemeId);
        message.success(t("scheme.activated"));
      },
    });
  };

  const handleClose = () => {
    Modal.confirm({
      title: t("scheme.closeConfirm"),
      content: t("scheme.closeMessage"),
      onOk: async () => {
        await closeScheme.mutateAsync(schemeId);
        message.success(t("scheme.closed"));
      },
    });
  };

  const armColumns = [
    { title: t("scheme.column.order"), dataIndex: "orderNumber", key: "orderNumber", width: 60 },
    { title: t("scheme.column.name"), dataIndex: "name", key: "name" },
    { title: t("scheme.column.displayName"), dataIndex: "displayName", key: "displayName" },
    { title: t("scheme.column.ratio"), dataIndex: "ratio", key: "ratio" },
  ];

  const stratumColumns = [
    { title: t("scheme.column.order"), dataIndex: "orderNumber", key: "orderNumber", width: 60 },
    { title: t("scheme.column.name"), dataIndex: "name", key: "name" },
    { title: t("scheme.column.type"), dataIndex: "stratumType", key: "stratumType",
      render: (type: string) => <Tag>{type}</Tag>,
    },
    { title: t("scheme.column.options"), dataIndex: "options", key: "options",
      render: (opts: any[]) => opts?.map((o: any) => o.label).join(", ") ?? "-",
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button onClick={() => navigate("/app/randomization")}>
          {t("scheme.back")}
        </Button>
      </Space>

      <Card>
        <Space style={{ justifyContent: "space-between", width: "100%" }}>
          <Space>
            <Title level={4} style={{ margin: 0 }}>{scheme.name}</Title>
          </Space>
          <Space>
            {scheme.status === "DRAFT" && (
              <Button type="primary" onClick={handleActivate} loading={activateScheme.isPending}>
                {t("scheme.activate")}
              </Button>
            )}
            {scheme.status === "ACTIVE" && (
              <Button danger onClick={handleClose} loading={closeScheme.isPending}>
                {t("scheme.close")}
              </Button>
            )}
          </Space>
        </Space>

        <Divider />

        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label={t("scheme.algorithm")}>
            <Tag color={algorithmColors[scheme.algorithm]}>{scheme.algorithm}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label={t("scheme.status")}>
            <Tag color={statusColors[scheme.status ?? "DRAFT"]}>{scheme.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label={t("scheme.minBlockSize")}>{scheme.minBlockSize ?? "-"}</Descriptions.Item>
          <Descriptions.Item label={t("scheme.maxBlockSize")}>{scheme.maxBlockSize ?? "-"}</Descriptions.Item>
          <Descriptions.Item label={t("scheme.totalAssigned")}>{scheme.totalAssigned ?? 0}</Descriptions.Item>
          <Descriptions.Item label={t("scheme.seed")}>{scheme.seed ?? "-"}</Descriptions.Item>
        </Descriptions>

        <Divider orientation="left">{t("scheme.studyArms")}</Divider>
        <Table dataSource={scheme.arms} columns={armColumns} rowKey="id" pagination={false} size="small" />

        {(scheme.stratifications?.length ?? 0) > 0 && (
          <>
            <Divider orientation="left">{t("scheme.stratificationFactors")}</Divider>
            <Table dataSource={scheme.stratifications} columns={stratumColumns} rowKey="id" pagination={false} size="small" />
          </>
        )}
      </Card>
    </div>
  );
}
