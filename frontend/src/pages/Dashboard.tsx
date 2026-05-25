import { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Card, Row, Col, Typography, Alert, Space } from "antd";
import { useAuth } from "@/providers/AuthProvider";
import { useStudies } from "@/hooks/useStudies";
import { SkeletonCard } from "@/components/SkeletonCard";
import { useNavigate } from "react-router-dom";

const { Title, Text } = Typography;

const QUICK_ACTIONS = [
  {
    key: "export",
    route: "/app/data-export",
  },
  {
    key: "randomization",
    route: "/app/randomization",
  },
  {
    key: "crfs",
    route: "/app/crfs",
  },
  {
    key: "questionnaires",
    route: "/app/questionnaires/templates",
  },
];

interface Activity {
  id: number;
  message: string;
  time: string;
}

const RECENT_ACTIVITIES: Activity[] = [
  { id: 1, message: "项目 STUDY-001 新增站点", time: "2 小时前" },
  { id: 2, message: "受试者 SUBJ-042 入组 STUDY-001", time: "4 小时前" },
  { id: 3, message: "CRF「不良事件」已完成（SUBJ-038）", time: "6 小时前" },
  { id: 4, message: "数据导出「全量导出」已完成", time: "1 天前" },
  { id: 5, message: "随机方案「双盲 RCT」已激活", time: "2 天前" },
  { id: 6, message: "CRF「实验室结果」数据质疑（SUBJ-022）", time: "2 天前" },
];

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return "早上好";
  if (hour < 18) return "下午好";
  return "晚上好";
}

export default function Dashboard() {
  const { t } = useTranslation();
  const { isAuthenticated, isInitialized, user } = useAuth();
  const { data: studies, isLoading, isError } = useStudies();
  const navigate = useNavigate();

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
        <Title level={4} style={{ marginTop: 0 }}>{t("dashboard.title")}</Title>
        <SkeletonCard count={4} />
      </div>
    );
  }

  if (isError) {
    return (
      <div>
        <Title level={4} style={{ marginTop: 0 }}>{t("dashboard.title")}</Title>
        <Alert
          message={t("dashboard.error.title")}
          description={t("dashboard.error.description")}
          type="warning"
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

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
      {/* Welcome header */}
      <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
        <div className="accent-bar" />
        <div>
          <Title
            level={4}
            style={{
              margin: 0,
              fontWeight: 600,
            }}
          >
            {getGreeting()}，{user?.firstName ?? user?.username ?? "用户"}
          </Title>
          <Text style={{ color: "var(--text-secondary)", fontSize: 13 }}>
            {t("dashboard.subtitle")}
          </Text>
        </div>
      </div>

      {/* Stat cards */}
      <Row gutter={[12, 12]}>
        <Col xs={12} sm={6} lg={4}>
          <Card styles={{ body: { padding: 16 } }}>
            <div className="number-display" style={{ fontSize: 24, fontWeight: 600, color: "var(--text)" }}>
              {activeStudies}
            </div>
            <div style={{ color: "var(--text-secondary)", fontSize: 13, marginTop: 2 }}>
              {t("dashboard.stats.activeStudies")}
            </div>
          </Card>
        </Col>
        <Col xs={12} sm={6} lg={4}>
          <Card styles={{ body: { padding: 16 } }}>
            <div className="number-display" style={{ fontSize: 24, fontWeight: 600, color: "var(--text)" }}>
              {totalSites}
            </div>
            <div style={{ color: "var(--text-secondary)", fontSize: 13, marginTop: 2 }}>
              {t("dashboard.stats.sites")}
            </div>
          </Card>
        </Col>
        <Col xs={12} sm={6} lg={4}>
          <Card styles={{ body: { padding: 16 } }}>
            <div className="number-display" style={{ fontSize: 24, fontWeight: 600, color: "var(--text)" }}>
              0
            </div>
            <div style={{ color: "var(--text-secondary)", fontSize: 13, marginTop: 2 }}>
              {t("dashboard.stats.subjects")}
            </div>
          </Card>
        </Col>
        <Col xs={12} sm={6} lg={4}>
          <Card styles={{ body: { padding: 16 } }}>
            <div className="number-display" style={{ fontSize: 24, fontWeight: 600, color: "var(--text)" }}>
              0
            </div>
            <div style={{ color: "var(--text-secondary)", fontSize: 13, marginTop: 2 }}>
              {t("dashboard.stats.crfsCompleted")}
            </div>
          </Card>
        </Col>
        <Col xs={12} sm={6} lg={4}>
          <Card styles={{ body: { padding: 16 } }}>
            <div className="number-display" style={{ fontSize: 24, fontWeight: 600, color: "var(--text)" }}>
              0
            </div>
            <div style={{ color: "var(--text-secondary)", fontSize: 13, marginTop: 2 }}>
              {t("dashboard.stats.queriesOpen")}
            </div>
          </Card>
        </Col>
      </Row>

      {/* Recent Activity */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card
            title={
              <Space size={8}>
                <span style={{ fontSize: 15, fontWeight: 600, color: "var(--text)" }}>
                  {t("dashboard.recentActivity")}
                </span>
                <span style={{ fontSize: 12, color: "var(--text-muted)" }}>
                  {t("dashboard.past7Days")}
                </span>
              </Space>
            }
            styles={{
              header: {
                borderBottom: "1px solid var(--border-light)",
                padding: "12px 20px",
              },
              body: { padding: "4px 20px" },
            }}
          >
            {RECENT_ACTIVITIES.map((activity) => (
              <div
                key={activity.id}
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  padding: "10px 0",
                  borderBottom: "1px solid var(--border-light)",
                }}
              >
                <span style={{ fontSize: 13, color: "var(--text)", lineHeight: 1.4 }}>
                  {activity.message}
                </span>
                <span style={{ fontSize: 11, color: "var(--text-muted)", whiteSpace: "nowrap", marginLeft: 12, flexShrink: 0 }}>
                  {activity.time}
                </span>
              </div>
            ))}
          </Card>
        </Col>

        {/* Study Overview as text */}
        <Col xs={24} lg={10}>
          <Card
            title={
              <span style={{ fontSize: 15, fontWeight: 600, color: "var(--text)" }}>
                {t("dashboard.studyOverview")}
              </span>
            }
            styles={{
              header: {
                borderBottom: "1px solid var(--border-light)",
                padding: "12px 20px",
              },
              body: { padding: "20px" },
            }}
          >
            <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
              <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 0", borderBottom: "1px solid var(--border-light)" }}>
                <span style={{ fontSize: 13, color: "var(--text-secondary)" }}>运行中</span>
                <span className="number-display" style={{ fontWeight: 600, color: "var(--success)" }}>65%</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 0", borderBottom: "1px solid var(--border-light)" }}>
                <span style={{ fontSize: 13, color: "var(--text-secondary)" }}>暂停</span>
                <span className="number-display" style={{ fontWeight: 600, color: "var(--warning)" }}>20%</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 0" }}>
                <span style={{ fontSize: 13, color: "var(--text-secondary)" }}>已关闭</span>
                <span className="number-display" style={{ fontWeight: 600, color: "var(--text-muted)" }}>15%</span>
              </div>
              <div style={{ marginTop: 8, paddingTop: 12, borderTop: "1px solid var(--border)", textAlign: "center" }}>
                <span className="number-display" style={{ fontSize: 22, fontWeight: 600, color: "var(--text)" }}>
                  {studies?.length ?? 0}
                </span>
                <div style={{ fontSize: 12, color: "var(--text-secondary)", marginTop: 2 }}>项目总数</div>
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* Quick Actions */}
      <div>
        <h4 style={{ margin: "0 0 12px", fontWeight: 600, fontSize: 15, color: "var(--text)" }}>
          {t("dashboard.quickActions")}
        </h4>
        <Row gutter={[12, 12]}>
          {QUICK_ACTIONS.map((action) => (
            <Col key={action.key} xs={24} sm={12} lg={6}>
              <Card
                hoverable
                onClick={() => navigate(action.route)}
                styles={{ body: { padding: 16 } }}
                style={{ cursor: "pointer" }}
              >
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14, color: "var(--text)" }}>
                    {actionLabels[action.key]}
                  </div>
                  <div style={{ color: "var(--text-secondary)", fontSize: 12, marginTop: 4, lineHeight: 1.4 }}>
                    {actionDescriptions[action.key]}
                  </div>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </div>
    </div>
  );
}