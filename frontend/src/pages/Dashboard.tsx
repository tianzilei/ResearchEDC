import { useTranslation } from "react-i18next";
import { Card, Row, Col, Typography, Alert, Skeleton } from "antd";
import { useNavigate } from "react-router-dom";
import { SkeletonPage, SkeletonStatCard } from "@/components/SkeletonCard";
import {
  useBootstrap,
  useDashboardTasks,
  useDashboardStatus,
  useDashboardRecent,
  type ModuleInfo,
} from "@/hooks/useDashboard";

const { Title, Text } = Typography;

function getGreetingKey(): string {
  const hour = new Date().getHours();
  if (hour < 12) return "dashboard.greeting.morning";
  if (hour < 18) return "dashboard.greeting.afternoon";
  return "dashboard.greeting.evening";
}

function formatTimestamp(ts: string, locale: string, t: (key: string, options?: Record<string, unknown>) => string): string {
  const date = new Date(ts);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return t("time.justNow");
  if (diffMin < 60) return t("time.minutesAgo", { count: diffMin });
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return t("time.hoursAgo", { count: diffHour });
  const diffDay = Math.floor(diffHour / 24);
  if (diffDay < 7) return t("time.daysAgo", { count: diffDay });
  return date.toLocaleDateString(locale.startsWith("zh") ? "zh-CN" : "en-US");
}

function ModuleCard({ mod }: { mod: ModuleInfo }) {
  const navigate = useNavigate();
  return (
    <Col xs={24} sm={12} md={8} lg={6}>
      <Card
        hoverable
        onClick={() => navigate(mod.path)}
        styles={{ body: { padding: 14 } }}
        style={{ cursor: "pointer" }}
      >
        <div style={{ fontWeight: 600, fontSize: 14, color: "var(--text)" }}>
          {mod.name}
        </div>
        <div
          style={{
            color: "var(--text-secondary)",
            fontSize: 12,
            marginTop: 4,
            lineHeight: 1.5,
          }}
        >
          {mod.description}
        </div>

      </Card>
    </Col>
  );
}

export default function Dashboard() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();

  const {
    data: bootstrap,
    isLoading: bootstrapLoading,
    isError: bootstrapError,
  } = useBootstrap();

  const { data: tasks, isLoading: tasksLoading } = useDashboardTasks();
  const { data: status, isLoading: statusLoading } = useDashboardStatus();
  const { data: recentItems, isLoading: recentLoading } = useDashboardRecent();

  if (bootstrapLoading) {
    return (
      <div>
        <Title level={4} style={{ marginTop: 0 }}>
          {t("dashboard.title")}
        </Title>
        <SkeletonPage />
      </div>
    );
  }

  if (bootstrapError || !bootstrap) {
    return (
      <div>
        <Title level={4} style={{ marginTop: 0 }}>
          {t("dashboard.title")}
        </Title>
        <Alert
          message={t("dashboard.error.title")}
          description={t("dashboard.error.description")}
          type="warning"
          action={<a onClick={() => navigate(0)}>{t("dashboard.error.retry")}</a>}
        />
      </div>
    );
  }

  const displayName =
    bootstrap.user.firstName ||
    bootstrap.user.lastName ||
    bootstrap.user.username ||
    t("common.user");

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
      {/* Welcome header with study/site context */}
      <div
        style={{
          display: "flex",
          alignItems: "flex-start",
          justifyContent: "space-between",
          flexWrap: "wrap",
          gap: 8,
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <div className="accent-bar" />
          <div>
            <Title
              level={4}
              style={{ margin: 0, fontWeight: 600 }}
            >
              {t(getGreetingKey())}, {displayName}
            </Title>
            <Text style={{ color: "var(--text-secondary)", fontSize: 13 }}>
              {t("dashboard.subtitle")}
            </Text>
          </div>
        </div>

        {bootstrap.defaultStudy && (
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: 12,
              padding: "6px 14px",
              background: "var(--bg-secondary)",
              borderRadius: 6,
              fontSize: 12,
              color: "var(--text-secondary)",
              flexWrap: "wrap",
            }}
          >
            <span>
              {t("dashboard.currentStudy")}:
              <strong style={{ color: "var(--text)" }}>
                {bootstrap.defaultStudy.name}
              </strong>
            </span>
            {bootstrap.defaultSiteName && (
              <>
                <span
                  style={{
                    display: "inline-block",
                    width: 1,
                    height: 14,
                    background: "var(--border-light)",
                  }}
                />
                <span>
                  {t("dashboard.currentSite")}:
                  <strong style={{ color: "var(--text)" }}>
                    {bootstrap.defaultSiteName}
                  </strong>
                </span>
              </>
            )}
            <span
              style={{
                display: "inline-block",
                width: 1,
                height: 14,
                background: "var(--border-light)",
              }}
            />
            <span>
              {t("dashboard.currentRole")}:
              <strong style={{ color: "var(--text)" }}>
                {bootstrap.defaultStudy.role}
              </strong>
            </span>
          </div>
        )}
      </div>

      {/* Module cards grid */}
      {bootstrap.modules.length > 0 && (
        <div>
          <h4
            style={{
              margin: "0 0 12px",
              fontWeight: 600,
              fontSize: 16,
              color: "var(--text)",
            }}
          >
            {t("dashboard.quickActions")}
          </h4>
          <Row gutter={[12, 12]}>
            {bootstrap.modules.map((mod) => (
              <ModuleCard key={mod.key} mod={mod} />
            ))}
          </Row>
        </div>
      )}

      {/* Pending tasks section */}
      <div>
        <h4
          style={{
            margin: "0 0 12px",
            fontWeight: 600,
            fontSize: 16,
            color: "var(--text)",
          }}
        >
          {t("dashboard.pendingTasks")}
        </h4>
        {tasksLoading ? (
          <Row gutter={[12, 12]}>
            {[1, 2, 3, 4].map((i) => (
              <Col key={i} xs={12} sm={6} lg={4}>
                <SkeletonStatCard />
              </Col>
            ))}
          </Row>
        ) : tasks ? (
          <Row gutter={[12, 12]}>
            <Col xs={12} sm={6} lg={4}>
              <Card styles={{ body: { padding: 16 } }}>
                <div
                  className="number-display"
                  style={{
                    fontSize: 24,
                    fontWeight: 600,
                    color: "var(--text)",
                  }}
                >
                  {tasks.pendingCrfs}
                </div>
                <div
                  style={{
                    color: "var(--text-secondary)",
                    fontSize: 13,
                    marginTop: 2,
                  }}
                >
                  {t("dashboard.pendingTasksCrf")}
                </div>
              </Card>
            </Col>
            <Col xs={12} sm={6} lg={4}>
              <Card styles={{ body: { padding: 16 } }}>
                <div
                  className="number-display"
                  style={{
                    fontSize: 24,
                    fontWeight: 600,
                    color: "var(--text)",
                  }}
                >
                  {tasks.pendingQueries}
                </div>
                <div
                  style={{
                    color: "var(--text-secondary)",
                    fontSize: 13,
                    marginTop: 2,
                  }}
                >
                  {t("dashboard.pendingTasksQuery")}
                </div>
              </Card>
            </Col>
            <Col xs={12} sm={6} lg={4}>
              <Card styles={{ body: { padding: 16 } }}>
                <div
                  className="number-display"
                  style={{
                    fontSize: 24,
                    fontWeight: 600,
                    color: "var(--text)",
                  }}
                >
                  {tasks.pendingReviews}
                </div>
                <div
                  style={{
                    color: "var(--text-secondary)",
                    fontSize: 13,
                    marginTop: 2,
                  }}
                >
                  {t("dashboard.pendingTasksReview")}
                </div>
              </Card>
            </Col>
            <Col xs={12} sm={6} lg={4}>
              <Card styles={{ body: { padding: 16 } }}>
                <div
                  className="number-display"
                  style={{
                    fontSize: 24,
                    fontWeight: 600,
                    color: "var(--text)",
                  }}
                >
                  {tasks.pendingAccountModifications}
                </div>
                <div
                  style={{
                    color: "var(--text-secondary)",
                    fontSize: 13,
                    marginTop: 2,
                  }}
                >
                  {t("dashboard.pendingTasksAccount")}
                </div>
              </Card>
            </Col>
          </Row>
        ) : null}
      </div>

      {/* Status and Recent row */}
      <Row gutter={[16, 16]}>
        {/* Recent Activity */}
        <Col xs={24} lg={16}>
          <Card
            title={
              <span style={{ fontSize: 16, fontWeight: 600, color: "var(--text)" }}>
                {t("dashboard.recentActivity")}
              </span>
            }
            styles={{
              header: {
                borderBottom: "1px solid var(--border-light)",
                padding: "12px 20px",
              },
              body: { padding: "4px 20px" },
            }}
          >
            {recentLoading ? (
              <Skeleton active paragraph={{ rows: 5 }} />
            ) : recentItems && recentItems.length > 0 ? (
              recentItems.map((item, idx) => (
                <div
                  key={idx}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    padding: "10px 0",
                    borderBottom:
                      idx < recentItems.length - 1
                        ? "1px solid var(--border-light)"
                        : "none",
                  }}
                >
                  <span
                    style={{
                      fontSize: 13,
                      color: "var(--text)",
                      lineHeight: 1.5,
                      flex: 1,
                    }}
                  >
                    {item.description}
                  </span>
                  <span
                    style={{
                      fontSize: 12,
                      color: "var(--text-muted)",
                      whiteSpace: "nowrap",
                      marginLeft: 12,
                      flexShrink: 0,
                    }}
                  >
                    {formatTimestamp(item.timestamp, i18n.language, t)}
                  </span>
                </div>
              ))
            ) : (
              <div
                style={{
                  padding: "20px 0",
                  textAlign: "center",
                  color: "var(--text-muted)",
                  fontSize: 13,
                }}
              >
                {t("dashboard.noRecentActivity")}
              </div>
            )}
          </Card>
        </Col>

        {/* System Status */}
        <Col xs={24} lg={8}>
          <Card
            title={
              <span style={{ fontSize: 16, fontWeight: 600, color: "var(--text)" }}>
                {t("dashboard.systemStatus")}
              </span>
            }
            styles={{
              header: {
                borderBottom: "1px solid var(--border-light)",
                padding: "12px 20px",
              },
              body: { padding: "16px 20px" },
            }}
          >
            {statusLoading ? (
              <Skeleton active paragraph={{ rows: 3 }} />
            ) : status ? (
              <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    padding: "8px 0",
                    borderBottom: "1px solid var(--border-light)",
                  }}
                >
                  <span style={{ fontSize: 13, color: "var(--text-secondary)" }}>
                    {t("dashboard.statusDatabase")}
                  </span>
                  <StatusBadge value={status.database} />
                </div>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    padding: "8px 0",
                    borderBottom: "1px solid var(--border-light)",
                  }}
                >
                  <span style={{ fontSize: 13, color: "var(--text-secondary)" }}>
                    {t("dashboard.statusBackgroundTasks")}
                  </span>
                  <StatusBadge value={status.backgroundTasks} />
                </div>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    padding: "8px 0",
                  }}
                >
                  <span style={{ fontSize: 13, color: "var(--text-secondary)" }}>
                    {t("dashboard.statusLastBackup")}
                  </span>
                  <span
                    style={{ fontSize: 12, color: "var(--text-muted)", fontWeight: 500 }}
                  >
                    {status.lastBackup
                      ? formatTimestamp(status.lastBackup, i18n.language, t)
                      : t("dashboard.statusUnknown")}
                  </span>
                </div>
              </div>
            ) : null}
          </Card>
        </Col>
      </Row>
    </div>
  );
}

function StatusBadge({ value }: { value: string }) {
  const color =
    value === "normal"
      ? "var(--success)"
      : value === "error"
        ? "var(--danger)"
        : "var(--warning)";
  return (
    <span style={{ fontSize: 12, color, fontWeight: 500 }}>
      {value === "normal" ? "正常" : value === "error" ? "异常" : value}
    </span>
  );
}
