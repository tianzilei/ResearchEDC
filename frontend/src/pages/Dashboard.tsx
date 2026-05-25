import { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Card, Col, Row, Typography, Alert, Space } from "antd";
import {
  TeamOutlined,
  MedicineBoxOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  UserOutlined,
  ExportOutlined,
  SafetyOutlined,
  FormOutlined,
  RightOutlined,
} from "@ant-design/icons";
import { useAuth } from "@/providers/AuthProvider";
import { useStudies } from "@/hooks/useStudies";
import { SkeletonCard } from "@/components/SkeletonCard";
import { useNavigate } from "react-router-dom";

const { Title, Text } = Typography;

const STAT_CARDS = [
  {
    key: "studies",
    title: "Active Studies",
    icon: <MedicineBoxOutlined />,
    color: "#099A87",
    bgColor: "rgba(9,154,135,0.10)",
  },
  {
    key: "sites",
    title: "Sites",
    icon: <TeamOutlined />,
    color: "#D4A854",
    bgColor: "rgba(212,168,84,0.10)",
  },
  {
    key: "subjects",
    title: "Subjects",
    icon: <UserOutlined />,
    color: "#066B5E",
    bgColor: "rgba(6,107,94,0.10)",
  },
  {
    key: "crfs",
    title: "CRFs Completed",
    icon: <FileTextOutlined />,
    color: "#5B7FAB",
    bgColor: "rgba(91,127,171,0.10)",
  },
  {
    key: "queries",
    title: "Queries Open",
    icon: <CheckCircleOutlined />,
    color: "#C44A4A",
    bgColor: "rgba(196,74,74,0.10)",
  },
];

const QUICK_ACTIONS = [
  {
    key: "export",
    label: "Data Export",
    icon: <ExportOutlined />,
    route: "/app/data-export",
    description: "Export study data in ODM XML, CSV, or Excel format",
    color: "#099A87",
  },
  {
    key: "randomization",
    label: "Randomization",
    icon: <SafetyOutlined />,
    route: "/app/randomization",
    description: "Manage randomization schemes and subject allocation",
    color: "#D4A854",
  },
  {
    key: "crfs",
    label: "CRF Library",
    icon: <FileTextOutlined />,
    route: "/app/crfs",
    description: "Browse and preview Case Report Forms",
    color: "#066B5E",
  },
  {
    key: "questionnaires",
    label: "Questionnaires",
    icon: <FormOutlined />,
    route: "/app/questionnaires/templates",
    description: "Create and manage questionnaire templates",
    color: "#5B7FAB",
  },
];

interface Activity {
  id: number;
  type: string;
  message: string;
  time: string;
}

const RECENT_ACTIVITIES: Activity[] = [
  { id: 1, type: "study", message: "New site added to STUDY-001", time: "2 hours ago" },
  { id: 2, type: "subject", message: "Subject SUBJ-042 enrolled in STUDY-001", time: "4 hours ago" },
  { id: 3, type: "crf", message: "CRF 'Adverse Events' completed for SUBJ-038", time: "6 hours ago" },
  { id: 4, type: "export", message: "Export 'Full Study Export' completed", time: "1 day ago" },
  { id: 5, type: "randomization", message: "Scheme 'Double-Blind RCT' activated", time: "2 days ago" },
  { id: 6, type: "query", message: "Query raised on CRF 'Lab Results' for SUBJ-022", time: "2 days ago" },
];

const ACTIVITY_ICONS: Record<string, React.ReactNode> = {
  study: <MedicineBoxOutlined style={{ fontSize: 12 }} />,
  subject: <UserOutlined style={{ fontSize: 12 }} />,
  crf: <FileTextOutlined style={{ fontSize: 12 }} />,
  export: <ExportOutlined style={{ fontSize: 12 }} />,
  randomization: <SafetyOutlined style={{ fontSize: 12 }} />,
  query: <CheckCircleOutlined style={{ fontSize: 12 }} />,
};

const ACTIVITY_COLORS: Record<string, string> = {
  study: "#099A87",
  subject: "#066B5E",
  crf: "#5B7FAB",
  export: "#D4A854",
  randomization: "#D4A854",
  query: "#C44A4A",
};

function DonutChart() {
  const { t } = useTranslation();
  const segments = [
    { label: t("dashboard.chart.active"), value: 65, color: "#099A87" },
    { label: t("dashboard.chart.paused"), value: 20, color: "#D4A854" },
    { label: t("dashboard.chart.closed"), value: 15, color: "#D9D4CA" },
  ];
  const total = segments.reduce((s, seg) => s + seg.value, 0);
  const radius = 60;
  const circumference = 2 * Math.PI * radius;
  let cumulativePercent = 0;

  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 16 }}>
      <svg width={160} height={160} viewBox="0 0 160 160">
        {segments.map((seg) => {
          const percent = seg.value / total;
          const dashLength = percent * circumference;
          const gap = circumference - dashLength;
          const dashArray = `${dashLength} ${gap}`;
          const rotation = (cumulativePercent * 360) - 90;
          cumulativePercent += percent;
          return (
            <circle
              key={seg.label}
              cx={80}
              cy={80}
              r={radius}
              fill="none"
              stroke={seg.color}
              strokeWidth={18}
              strokeDasharray={dashArray}
              strokeLinecap="round"
              transform={`rotate(${rotation} 80 80)`}
              style={{
                transition: "stroke-dasharray 0.6s ease",
              }}
            />
          );
        })}
        <text x={80} y={75} textAnchor="middle" fill="#1A1D23" fontSize={28} fontWeight={700} fontFamily="'Sora', sans-serif">
          {total}
        </text>
        <text x={80} y={95} textAnchor="middle" fill="#6B7280" fontSize={12} fontFamily="'DM Sans', sans-serif">
          {t("dashboard.chart.studies")}
        </text>
      </svg>
      <div style={{ display: "flex", gap: 20 }}>
        {segments.map((seg) => (
          <Space key={seg.label} size={6}>
            <div style={{ width: 8, height: 8, borderRadius: "50%", background: seg.color }} />
            <Text style={{ fontSize: 12, color: "#6B7280" }}>
              {seg.label} ({seg.value}%)
            </Text>
          </Space>
        ))}
      </div>
    </div>
  );
}

function getGreeting(t: (key: string) => string): string {
  const hour = new Date().getHours();
  if (hour < 12) return t("dashboard.greeting.morning");
  if (hour < 18) return t("dashboard.greeting.afternoon");
  return t("dashboard.greeting.evening");
}

export default function Dashboard() {
  const { t } = useTranslation();
  const { isAuthenticated, isInitialized, user } = useAuth();
  const { data: studies, isLoading, isError } = useStudies();
  const navigate = useNavigate();

  const statTitles: Record<string, string> = {
    studies: t("dashboard.stats.activeStudies"),
    sites: t("dashboard.stats.sites"),
    subjects: t("dashboard.stats.subjects"),
    crfs: t("dashboard.stats.crfsCompleted"),
    queries: t("dashboard.stats.queriesOpen"),
  };

  const actionLabels: Record<string, string> = {
    export: t("dashboard.actions.dataExport"),
    randomization: t("dashboard.actions.randomization"),
    crfs: t("dashboard.actions.crfLibrary"),
    questionnaires: t("dashboard.actions.questionnaires"),
  };

  const actionDescriptions: Record<string, string> = {
    export: t("dashboard.actions.dataExportDesc"),
    randomization: t("dashboard.actions.randomizationDesc"),
    crfs: t("dashboard.actions.crfLibraryDesc"),
    questionnaires: t("dashboard.actions.questionnairesDesc"),
  };

  useEffect(() => {
    if (isInitialized && !isAuthenticated) {
      navigate("/");
    }
  }, [isInitialized, isAuthenticated, navigate]);

  if (!isInitialized || isLoading) {
    return (
      <div>
        <Title level={4} style={{ marginTop: 0, fontFamily: "'Sora', sans-serif" }}>
          {t("dashboard.title")}
        </Title>
        <SkeletonCard count={5} />
      </div>
    );
  }

  if (isError) {
    return (
      <div>
        <Title level={4} style={{ marginTop: 0, fontFamily: "'Sora', sans-serif" }}>
          {t("dashboard.title")}
        </Title>
        <Alert
          message={t("dashboard.error.title")}
          description={t("dashboard.error.description")}
          type="warning"
          showIcon
          action={
            <a onClick={() => { navigate(0); }}>{t("dashboard.error.retry")}</a>
          }
        />
      </div>
    );
  }

  const activeStudies = studies?.filter(
    (s) => s.study.status === "available",
  ).length ?? 0;

  const totalSites = studies?.reduce(
    (acc, s) => acc + (s.sites?.length ?? 0),
    0,
  ) ?? 0;

  const statValues: Record<string, number> = {
    studies: activeStudies,
    sites: totalSites,
    subjects: 0,
    crfs: 0,
    queries: 0,
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 28 }}>
      {/* Welcome header */}
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        <div style={{ width: 3, height: 28, borderRadius: 2, background: "#D4A854" }} />
        <div>
          <Title
            level={3}
            style={{
              margin: 0,
              fontFamily: "'Sora', sans-serif",
              fontWeight: 600,
              fontSize: 22,
            }}
          >
            {getGreeting(t)}, {user?.firstName ?? user?.username ?? t("dashboard.greeting.user")}
          </Title>
          <Text style={{ color: "#6B7280", fontSize: 13, letterSpacing: "0.01em" }}>
            {t("dashboard.subtitle")}
          </Text>
        </div>
      </div>

      {/* Stat cards */}
      <Row gutter={[16, 16]}>
        {STAT_CARDS.map((card, index) => (
          <Col key={card.key} xs={24} sm={12} lg={4} xl={4}>
            <Card
              className="card-hover"
              style={{
                borderRadius: 14,
                borderLeft: `3px solid ${card.color}`,
                animation: "fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) both",
                animationDelay: `${index * 0.08}s`,
              }}
              styles={{ body: { padding: 20 } }}
            >
              <Space direction="vertical" size={12} style={{ width: "100%" }}>
                <div
                  style={{
                    width: 36,
                    height: 36,
                    borderRadius: "50%",
                    background: card.bgColor,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    color: card.color,
                    fontSize: 16,
                  }}
                >
                  {card.icon}
                </div>
                <div>
                  <div
                    style={{
                      fontFamily: "'Sora', sans-serif",
                      fontWeight: 600,
                      fontSize: 28,
                      lineHeight: 1.1,
                      letterSpacing: "-0.02em",
                      color: "#1A1D23",
                    }}
                  >
                    {statValues[card.key]}
                  </div>
                  <div
                    style={{
                      color: "#6B7280",
                      fontSize: 13,
                      marginTop: 4,
                      letterSpacing: "0.01em",
                    }}
                  >
                    {statTitles[card.key]}
                  </div>
                </div>
              </Space>
            </Card>
          </Col>
        ))}
      </Row>

      {/* Middle section: Activity + Studies Overview */}
      <Row gutter={[24, 24]}>
        {/* Recent Activity */}
        <Col xs={24} lg={14}>
          <Card
            title={
              <Space>
                <span style={{ fontFamily: "'Sora', sans-serif", fontSize: 15, fontWeight: 600, color: "#1A1D23" }}>
                  {t("dashboard.recentActivity")}
                </span>
                <span style={{ fontSize: 12, color: "#9CA3AF", fontWeight: 400 }}>{t("dashboard.past7Days")}</span>
              </Space>
            }
            styles={{
              header: {
                borderBottom: "1px solid #EDE8E0",
                padding: "16px 24px",
              },
              body: { padding: "8px 24px" },
            }}
            style={{ borderRadius: 14 }}
          >
            <div style={{ position: "relative", paddingLeft: 28 }}>
              {/* Timeline vertical line */}
              <div
                style={{
                  position: "absolute",
                  left: 10,
                  top: 16,
                  bottom: 16,
                  width: 1.5,
                  background: "#EDE8E0",
                }}
              />
              {RECENT_ACTIVITIES.map((activity) => (
                <div
                  key={activity.id}
                  style={{
                    position: "relative",
                    padding: "14px 0 14px 16px",
                    animation: "fadeInUp 0.4s cubic-bezier(0.22, 1, 0.36, 1) both",
                    animationDelay: `${0.1 + activity.id * 0.06}s`,
                  }}
                >
                  {/* Timeline dot */}
                  <div
                    style={{
                      position: "absolute",
                      left: -24,
                      top: 18,
                      width: 10,
                      height: 10,
                      borderRadius: "50%",
                      background: ACTIVITY_COLORS[activity.type] ?? "#D9D4CA",
                      border: "2px solid #F8F5F0",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      zIndex: 1,
                    }}
                  />
                  <div
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "flex-start",
                      gap: 12,
                    }}
                  >
                    <Space size={8} style={{ minWidth: 0 }}>
                      <span
                        style={{
                          display: "inline-flex",
                          alignItems: "center",
                          justifyContent: "center",
                          width: 22,
                          height: 22,
                          borderRadius: 6,
                          background: `${ACTIVITY_COLORS[activity.type] ?? "#EDE8E0"}15`,
                          color: ACTIVITY_COLORS[activity.type] ?? "#6B7280",
                          flexShrink: 0,
                        }}
                      >
                        {ACTIVITY_ICONS[activity.type]}
                      </span>
                      <span style={{ fontSize: 13, color: "#1A1D23", lineHeight: 1.4 }}>
                        {activity.message}
                      </span>
                    </Space>
                    <Text style={{ fontSize: 11, color: "#9CA3AF", whiteSpace: "nowrap", flexShrink: 0 }}>
                      {activity.time}
                    </Text>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </Col>

        {/* Study Overview */}
        <Col xs={24} lg={10}>
          <Card
            title={
              <span style={{ fontFamily: "'Sora', sans-serif", fontSize: 15, fontWeight: 600, color: "#1A1D23" }}>
                {t("dashboard.studyOverview")}
              </span>
            }
            styles={{
              header: {
                borderBottom: "1px solid #EDE8E0",
                padding: "16px 24px",
              },
              body: { padding: "32px 24px" },
            }}
            style={{ borderRadius: 14 }}
          >
            <DonutChart />
          </Card>
        </Col>
      </Row>

      {/* Quick Actions */}
      <div>
        <Title
          level={5}
          style={{
            fontFamily: "'Sora', sans-serif",
            fontWeight: 600,
            marginBottom: 16,
            color: "#1A1D23",
          }}
        >
          {t("dashboard.quickActions")}
        </Title>
        <Row gutter={[16, 16]}>
          {QUICK_ACTIONS.map((action, index) => (
            <Col key={action.key} xs={24} sm={12} lg={6}>
              <Card
                hoverable
                className="card-hover"
                onClick={() => navigate(action.route)}
                style={{
                  borderRadius: 14,
                  cursor: "pointer",
                  animation: "fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) both",
                  animationDelay: `${0.3 + index * 0.08}s`,
                }}
                styles={{ body: { padding: 20 } }}
              >
                <Space direction="vertical" size={10} style={{ width: "100%" }}>
                  <div
                    style={{
                      width: 40,
                      height: 40,
                      borderRadius: 12,
                      background: `${action.color}12`,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      color: action.color,
                      fontSize: 20,
                    }}
                  >
                    {action.icon}
                  </div>
                  <div>
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                      }}
                    >
                      <span
                        style={{
                          fontFamily: "'Sora', sans-serif",
                          fontWeight: 600,
                          fontSize: 14,
                          color: "#1A1D23",
                        }}
                      >
                        {actionLabels[action.key]}
                      </span>
                      <RightOutlined style={{ fontSize: 11, color: "#D4A854" }} />
                    </div>
                    <div
                      style={{
                        color: "#6B7280",
                        fontSize: 12,
                        marginTop: 4,
                        lineHeight: 1.4,
                      }}
                    >
                      {actionDescriptions[action.key]}
                    </div>
                  </div>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>
      </div>
    </div>
  );
}
