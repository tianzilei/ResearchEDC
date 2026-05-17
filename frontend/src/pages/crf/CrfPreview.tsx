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
  const { versionId } = useParams<{ versionId: string }>();
  const navigate = useNavigate();
  const vId = Number(versionId);

  const { data: version, isLoading } = useAppQuery<CrfVersion>({
    queryKey: ["crf", "version", vId],
    queryFn: () => apiClient.get<CrfVersion>(`/api/v1/crfs/versions/${vId}`),
    enabled: vId > 0,
  });

  if (isLoading) return <SkeletonPage />;
  if (!version) return <Alert message="Version not found" type="error" showIcon />;

  const sectionColumns = [
    { title: "#", dataIndex: "ordinal", key: "ordinal", width: 60 },
    { title: "Label", dataIndex: "label", key: "label" },
    { title: "Title", dataIndex: "title", key: "title" },
    { title: "Section ID", dataIndex: "sectionId", key: "sectionId" },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => { navigate("/app/crfs"); }}>Back to CRFs</Button>
      </Space>

      <Title level={4}><FileTextOutlined /> {version.name}</Title>

      <Card>
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="OID">{version.ocOid}</Descriptions.Item>
          <Descriptions.Item label="Status"><Tag>{version.status}</Tag></Descriptions.Item>
          <Descriptions.Item label="Description" span={2}>{version.description ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Revision Notes" span={2}>{version.revisionNotes ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Sections">{version.sections?.length ?? 0}</Descriptions.Item>
        </Descriptions>

        <Divider orientation="left">Sections</Divider>
        <Table dataSource={version.sections ?? []} columns={sectionColumns} rowKey="sectionId"
          pagination={false} size="small"
          locale={{ emptyText: <Empty description="No sections" /> }} />
      </Card>
    </div>
  );
}
