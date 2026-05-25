import { useState } from "react";
import { Card, Table, Button, Space, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { useCurrentStudy } from "@/hooks/useStudies";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title, Text } = Typography;

interface StudySummary {
  studyId: number;
  name: string;
  uniqueIdentifier: string | null;
  phase: string | null;
  principalInvestigator: string | null;
  sponsor: string | null;
  dateCreated: string;
  expectedTotalEnrollment: number | null;
  site: boolean;
  parentStudyId: number | null;
}

export default function StudyList() {
  const navigate = useNavigate();
  const { setCurrentStudy } = useCurrentStudy();
  const [studies, setStudies] = useState<StudySummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<"studies" | "sites">("studies");

  useState(() => {
    Promise.all([
      fetch("/api/v1/studies").then(r => r.ok ? r.json() : []),
    ]).then(([data]) => {
      setStudies(data);
      setLoading(false);
    }).catch(() => setLoading(false));
  });

  const handleSelectStudy = (study: StudySummary) => {
    setCurrentStudy({
      id: study.studyId,
      name: study.name,
      uniqueIdentifier: study.uniqueIdentifier ?? "",
    } as any);
    navigate("/app/subjects");
  };

  if (loading) return <SkeletonPage />;

  const parentStudies = studies.filter(s => !s.site);
  const allSites = studies.filter(s => s.site);

  const columns = [
    {
      title: "名称", dataIndex: "name", key: "name",
      render: (text: string, record: StudySummary) => (
        <a onClick={() => handleSelectStudy(record)}>
          {text}
        </a>
      ),
    },
    { title: "标识符", dataIndex: "uniqueIdentifier", key: "uid", render: (v: string | null) => v ?? "-" },
    { title: "主要研究者", dataIndex: "principalInvestigator", key: "pi", render: (v: string | null) => v ?? "-" },
    { title: "阶段", dataIndex: "phase", key: "phase", render: (v: string | null) => v ?? "-" },
    { title: "赞助方", dataIndex: "sponsor", key: "sponsor", render: (v: string | null) => v ?? "-" },
    { title: "计划入组", dataIndex: "expectedTotalEnrollment", key: "enrollment", render: (v: number | null) => v ?? "-" },
    {
      title: "", key: "actions",
      render: (_: any, record: StudySummary) => (
        <Button size="small" onClick={() => handleSelectStudy(record)}>
          管理受试者
        </Button>
      ),
    },
  ];

  const dataSource = activeTab === "studies" ? parentStudies : allSites;

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <div>
          <Title level={4} style={{ margin: 0 }}>项目</Title>
          <Text style={{ color: "var(--text-secondary)", marginTop: 2, display: "block", fontSize: 13 }}>
            {parentStudies.length} 个项目 · {allSites.length} 个站点
          </Text>
        </div>
        <Space>
          <Button
            type={activeTab === "studies" ? "primary" : "default"}
            onClick={() => setActiveTab("studies")}
          >
            项目
          </Button>
          <Button
            type={activeTab === "sites" ? "primary" : "default"}
            onClick={() => setActiveTab("sites")}
          >
            站点
          </Button>
          <Button type="primary" onClick={() => navigate("/app/studies/create")}>
            新建项目
          </Button>
        </Space>
      </div>

      <Card styles={{ body: { padding: 0 } }}>
        <Table
          dataSource={dataSource}
          columns={columns}
          rowKey="studyId"
          pagination={{ pageSize: 20, showTotal: (t) => `共 ${t} 项` }}
          locale={{ emptyText: activeTab === "studies" ? "暂无项目" : "暂无站点" }}
        />
      </Card>
    </div>
  );
}