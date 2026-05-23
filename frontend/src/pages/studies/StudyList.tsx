import { useState } from "react";
import { Card, Table, Tag, Button, Space, Typography } from "antd";
import { PlusOutlined, ExperimentOutlined, BranchesOutlined } from "@ant-design/icons";
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
      title: "Name", dataIndex: "name", key: "name",
      render: (text: string, record: StudySummary) => (
        <a onClick={() => handleSelectStudy(record)}>
          <ExperimentOutlined style={{ marginRight: 8 }} />{text}
        </a>
      ),
    },
    { title: "Identifier", dataIndex: "uniqueIdentifier", key: "uid", render: (v: string) => v ?? "-" },
    { title: "PI", dataIndex: "principalInvestigator", key: "pi", render: (v: string) => v ?? "-" },
    { title: "Phase", dataIndex: "phase", key: "phase", render: (v: string) => v ? <Tag>{v}</Tag> : "-" },
    { title: "Sponsor", dataIndex: "sponsor", key: "sponsor", render: (v: string) => v ?? "-" },
    {
      title: "Enrollment", dataIndex: "expectedTotalEnrollment", key: "enrollment",
      render: (v: number) => v ?? "-",
    },
    {
      title: "", key: "actions",
      render: (_: any, record: StudySummary) => (
        <Button type="link" size="small" onClick={() => handleSelectStudy(record)}>
          Manage Subjects
        </Button>
      ),
    },
  ];

  const dataSource = activeTab === "studies" ? parentStudies : allSites;

  return (
    <div style={{ padding: "24px 32px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <div>
          <Title level={3} style={{ margin: 0 }}>Studies</Title>
          <Text type="secondary" style={{ marginTop: 4, display: "block" }}>
            {parentStudies.length} studies &middot; {allSites.length} sites
          </Text>
        </div>
        <Space>
          <Button
            type={activeTab === "studies" ? "primary" : "default"}
            icon={<ExperimentOutlined />}
            onClick={() => setActiveTab("studies")}
          >
            Studies
          </Button>
          <Button
            type={activeTab === "sites" ? "primary" : "default"}
            icon={<BranchesOutlined />}
            onClick={() => setActiveTab("sites")}
          >
            Sites
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate("/app/studies/create")}>
            New Study
          </Button>
        </Space>
      </div>

      <Card
        style={{ borderRadius: 14, border: "1px solid var(--color-border-light, #E5E0D8)" }}
        styles={{ body: { padding: 0 } }}
      >
        <Table
          dataSource={dataSource}
          columns={columns}
          rowKey="studyId"
          pagination={{ pageSize: 20, showTotal: (t) => `${t} studies` }}
          locale={{ emptyText: activeTab === "studies" ? "No studies found" : "No sites found" }}
        />
      </Card>
    </div>
  );
}
