import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
  Breadcrumb,
  Card,
  Descriptions,
  Tag,
  Typography,
  Button,
  Space,
  Tabs,
  Table,
  Divider,
  Result,
} from "antd";
import {
  EditOutlined,
  ExperimentOutlined,
  BranchesOutlined,
  TeamOutlined,
  FileTextOutlined,
  SettingOutlined,
} from "@ant-design/icons";
import { SkeletonPage } from "@/components/SkeletonCard";
import type { StudyDetail as StudyDetailType } from "@/types/study";

const { Title, Text } = Typography;

export default function StudyDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [study, setStudy] = useState<StudyDetailType | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    fetch(`/api/v1/studies/${id}`)
      .then((r) => (r.ok ? r.json() : null))
      .then((data) => {
        setStudy(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, [id]);

  const statusColor = (status: string) => {
    switch (status?.toLowerCase()) {
      case "available": return "green";
      case "pending": return "orange";
      case "frozen": return "blue";
      case "locked": return "red";
      case "removed": return "default";
      default: return "default";
    }
  };

  if (loading) return <SkeletonPage />;
  if (!study) {
    return (
      <Result
        status="404"
        title="Study Not Found"
        subTitle={`Study #${id} could not be found.`}
        extra={<Button onClick={() => navigate("/app/studies")}>Back to Studies</Button>}
      />
    );
  }

  const overviewItems = [
    { label: "Name", children: study.name },
    { label: "Unique Identifier", children: study.uniqueIdentifier ?? "-" },
    { label: "Official Title", children: study.officialTitle ?? "-" },
    { label: "Phase", children: study.phase ? <Tag>{study.phase}</Tag> : "-" },
    { label: "Status", children: <Tag color={statusColor(study.status)}>{study.status}</Tag> },
    { label: "Principal Investigator", children: study.principalInvestigator ?? "-" },
    { label: "Sponsor", children: study.sponsor ?? "-" },
    { label: "Collaborators", children: study.collaborators ?? "-" },
    { label: "Summary", children: study.summary ?? "-", span: 2 },
  ];

  const designItems = [
    { label: "Purpose", children: study.purpose ?? "-" },
    { label: "Allocation", children: study.allocation ?? "-" },
    { label: "Masking", children: study.masking ?? "-" },
    { label: "Gender", children: study.gender ?? "-" },
    { label: "Conditions", children: study.conditions ?? "-" },
    { label: "Keywords", children: study.keywords ?? "-" },
    { label: "Eligibility", children: study.eligibility ?? "-", span: 2 },
  ];

  const facilityItems = [
    { label: "Facility Name", children: study.facilityName ?? "-" },
    { label: "City", children: study.facilityCity ?? "-" },
    { label: "State", children: study.facilityState ?? "-" },
    { label: "Country", children: study.facilityCountry ?? "-" },
    { label: "Planned Start", children: study.datePlannedStart ? new Date(study.datePlannedStart).toLocaleDateString() : "-" },
    { label: "Planned End", children: study.datePlannedEnd ? new Date(study.datePlannedEnd).toLocaleDateString() : "-" },
    { label: "Expected Enrollment", children: study.expectedTotalEnrollment ?? "-" },
    { label: "Protocol Type", children: study.protocolType ?? "-" },
  ];

  const siteColumns = [
    { title: "Name", dataIndex: "name", key: "name",
      render: (name: string, record: any) => (
        <Link to={`/app/studies/${record.studyId}`}>{name}</Link>
      ),
    },
    { title: "Identifier", dataIndex: "uniqueIdentifier", key: "uid", render: (v: string) => v ?? "-" },
    { title: "PI", dataIndex: "principalInvestigator", key: "pi", render: (v: string) => v ?? "-" },
    {
      title: "Status", dataIndex: "status", key: "status",
      render: (s: string) => <Tag color={statusColor(s)}>{s}</Tag>,
    },
  ];

  const tabItems = [
    {
      key: "overview",
      label: "Overview",
      children: (
        <div style={{ padding: 16 }}>
          <Descriptions title="Protocol Information" column={2} items={overviewItems} bordered size="small" />
          <Divider />
          <Descriptions title="Study Design" column={2} items={designItems} bordered size="small" />
          <Divider />
          <Descriptions title="Facility & Enrollment" column={2} items={facilityItems} bordered size="small" />
        </div>
      ),
    },
    {
      key: "sites",
      label: <span><BranchesOutlined /> Sites ({study.sites?.length ?? 0})</span>,
      children: (
        <div style={{ padding: 16 }}>
          <Space style={{ marginBottom: 16, justifyContent: "space-between", width: "100%" }}>
            <Text strong>Sites for {study.name}</Text>
            <Button type="primary" size="small" icon={<BranchesOutlined />}
              onClick={() => navigate(`/app/studies/${id}/sites/create`)}>
              Add Site
            </Button>
          </Space>
          <Table
            dataSource={study.sites ?? []}
            columns={siteColumns}
            rowKey="studyId"
            pagination={false}
            locale={{ emptyText: "No sites defined" }}
          />
        </div>
      ),
    },
    {
      key: "actions",
      label: "Quick Actions",
      children: (
        <div style={{ padding: 16 }}>
          <Space direction="vertical" style={{ width: "100%" }}>
            <Card hoverable onClick={() => navigate(`/app/studies/${id}/edit`)}
              styles={{ body: { padding: "16px 20px" } }}>
              <Space><EditOutlined style={{ fontSize: 18 }} /><div><Text strong>Edit Study</Text><br /><Text type="secondary">Update study details and configuration</Text></div></Space>
            </Card>
            <Card hoverable onClick={() => navigate(`/app/subjects`)}
              styles={{ body: { padding: "16px 20px" } }}>
              <Space><TeamOutlined style={{ fontSize: 18 }} /><div><Text strong>Manage Subjects</Text><br /><Text type="secondary">View and enroll subjects in this study</Text></div></Space>
            </Card>
            <Card hoverable onClick={() => navigate(`/app/studies/${id}/event-definitions`)}
              styles={{ body: { padding: "16px 20px" } }}>
              <Space><FileTextOutlined style={{ fontSize: 18 }} /><div><Text strong>Event Definitions</Text><br /><Text type="secondary">Define study events and CRF assignments</Text></div></Space>
            </Card>
            <Card hoverable onClick={() => navigate(`/app/studies/${id}/rules`)}
              styles={{ body: { padding: "16px 20px" } }}>
              <Space><SettingOutlined style={{ fontSize: 18 }} /><div><Text strong>Rules</Text><br /><Text type="secondary">View and manage rule sets</Text></div></Space>
            </Card>
          </Space>
        </div>
      ),
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/studies">Studies</Link> },
          { title: study.name },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card
        style={{ marginBottom: 16, borderRadius: 14 }}
        styles={{ body: { padding: "16px 24px" } }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <ExperimentOutlined style={{ fontSize: 24, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>{study.name}</Title>
              <Space split={<Text type="secondary">|</Text>} style={{ marginTop: 2 }}>
                <Text type="secondary">{study.uniqueIdentifier ?? "No identifier"}</Text>
                <Tag color={statusColor(study.status)}>{study.status}</Tag>
                {study.site && <Tag>Site</Tag>}
              </Space>
            </div>
          </Space>
          <Space>
            <Button icon={<EditOutlined />} onClick={() => navigate(`/app/studies/${id}/edit`)}>
              Edit
            </Button>
            <Button onClick={() => navigate("/app/studies")}>Back</Button>
          </Space>
        </div>
      </Card>

      <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 0 } }}>
        <Tabs items={tabItems} style={{ minHeight: 300 }} />
      </Card>
    </div>
  );
}
