import { useTranslation } from "react-i18next";
import { Card, Typography, Space, Button, Table, Tag, Empty, Descriptions, Divider, Alert } from "antd";
import { ArrowLeftOutlined, FileTextOutlined } from "@ant-design/icons";
import { useParams, useNavigate } from "react-router-dom";
import { useAppQuery } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title } = Typography;

interface Section {
  sectionId: number;
  crfVersionId: number;
  label: string;
  title: string;
  ordinal: number;
}

interface CrfVersion {
  crfVersionId: number;
  crfId: number;
  name: string;
  description?: string;
  revisionNotes?: string;
  ocOid: string;
  status: string;
  dateCreated: string;
  sections: Section[];
}

export default function CrfPreview() {
  const { t } = useTranslation();
  const { versionId } = useParams<{ versionId: string }>();
  const navigate = useNavigate();
  const vId = Number(versionId);

  const { data: version, isLoading } = useAppQuery<CrfVersion>({
    queryKey: ["crf", "version", vId],
    queryFn: () => apiClient.get<CrfVersion>(`/api/v1/crfs/versions/${vId}`),
    enabled: vId > 0,
  });

  if (isLoading) return <SkeletonPage />;
  if (!version) return <Alert message={t("crf.versionNotFound")} type="error" showIcon />;

  const sectionColumns = [
    { title: t("crf.column.order"), dataIndex: "ordinal", key: "ordinal", width: 60 },
    { title: t("crf.column.label"), dataIndex: "label", key: "label" },
    { title: t("crf.column.title"), dataIndex: "title", key: "title" },
    { title: t("crf.column.sectionId"), dataIndex: "sectionId", key: "sectionId" },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => { navigate("/app/crfs"); }}>{t("crf.back")}</Button>
      </Space>

      <Title level={4}><FileTextOutlined /> {version.name}</Title>

      <Card>
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label={t("crf.description.oid")}>{version.ocOid}</Descriptions.Item>
          <Descriptions.Item label={t("crf.description.status")}><Tag>{version.status}</Tag></Descriptions.Item>
          <Descriptions.Item label={t("crf.description.description")} span={2}>{version.description ?? "-"}</Descriptions.Item>
          <Descriptions.Item label={t("crf.description.revisionNotes")} span={2}>{version.revisionNotes ?? "-"}</Descriptions.Item>
          <Descriptions.Item label={t("crf.description.sections")}>{version.sections?.length ?? 0}</Descriptions.Item>
        </Descriptions>

        <Divider orientation="left">{t("crf.sections")}</Divider>
        <Table dataSource={version.sections ?? []} columns={sectionColumns} rowKey="sectionId"
          pagination={false} size="small"
          locale={{ emptyText: <Empty description={t("crf.noSections")} /> }} />
      </Card>
    </div>
  );
}
