import { useTranslation } from "react-i18next";
import { Card, Table, Tag, Typography, Empty } from "antd";
import { FileTextOutlined } from "@ant-design/icons";
import { useAppQuery } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";
import { useNavigate } from "react-router-dom";

const { Title } = Typography;

interface CrfSummary {
  crfId: number;
  name: string;
  description?: string;
  ocOid: string;
  status: string;
  versionCount: number;
  dateCreated: string;
}

function useCrfs() {
  return useAppQuery<CrfSummary[]>({
    queryKey: ["crfs"],
    queryFn: () => apiClient.get<CrfSummary[]>("/api/v1/crfs"),
  });
}

export default function CrfList() {
  const { t } = useTranslation();
  const { data: crfs, isLoading } = useCrfs();
  const navigate = useNavigate();

  if (isLoading) return <SkeletonPage />;

  const columns = [
    { title: t("crf.column.name"), dataIndex: "name", key: "name",
      render: (name: string, r: CrfSummary) => (
        <a onClick={() => { navigate(`/app/crfs/${r.crfId}`); }}>{name}</a>
      ),
    },
    { title: t("crf.column.oid"), dataIndex: "ocOid", key: "ocOid" },
    { title: t("crf.column.status"), dataIndex: "status", key: "status",
      render: (s: string) => <Tag>{s}</Tag>,
    },
    { title: t("crf.column.versions"), dataIndex: "versionCount", key: "versionCount" },
    { title: t("crf.column.created"), dataIndex: "dateCreated", key: "dateCreated",
      render: (d: string) => d ? new Date(d).toLocaleDateString() : "-",
    },
  ];

  return (
    <div>
      <Title level={4} style={{ marginTop: 0 }}><FileTextOutlined /> {t("crf.library")}</Title>
      <Card style={{ marginTop: 16 }}>
        <Table dataSource={crfs ?? []} columns={columns} rowKey="crfId" pagination={false}
          locale={{ emptyText: <Empty description={t("crf.empty")} /> }} />
      </Card>
    </div>
  );
}
