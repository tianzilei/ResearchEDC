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
  const { data: crfs, isLoading } = useCrfs();
  const navigate = useNavigate();

  if (isLoading) return <SkeletonPage />;

  const columns = [
    { title: "Name", dataIndex: "name", key: "name",
      render: (name: string, r: CrfSummary) => (
        <a onClick={() => { navigate(`/app/crfs/${r.crfId}`); }}>{name}</a>
      ),
    },
    { title: "OID", dataIndex: "ocOid", key: "ocOid" },
    { title: "Status", dataIndex: "status", key: "status",
      render: (s: string) => <Tag>{s}</Tag>,
    },
    { title: "Versions", dataIndex: "versionCount", key: "versionCount" },
    { title: "Created", dataIndex: "dateCreated", key: "dateCreated",
      render: (d: string) => d ? new Date(d).toLocaleDateString() : "-",
    },
  ];

  return (
    <div>
      <Title level={4} style={{ marginTop: 0 }}><FileTextOutlined /> CRF Library</Title>
      <Card style={{ marginTop: 16 }}>
        <Table dataSource={crfs ?? []} columns={columns} rowKey="crfId" pagination={false}
          locale={{ emptyText: <Empty description="No CRFs found" /> }} />
      </Card>
    </div>
  );
}
