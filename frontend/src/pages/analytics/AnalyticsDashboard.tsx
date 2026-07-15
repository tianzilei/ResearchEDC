import { Card, Col, Row, Statistic, Typography } from "antd";
import { useTranslation } from "react-i18next";
import { SkeletonPage } from "@/components/SkeletonCard";
import { useAnalyticsDashboard } from "@/hooks/useAnalytics";
import { useCurrentStudy } from "@/hooks/useStudies";
import type { AnalyticsMetricDTO } from "@/api/analytics";

const { Text, Title } = Typography;

export default function AnalyticsDashboard() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const { data, isLoading } = useAnalyticsDashboard(currentStudy?.id);

  if (!currentStudy) {
    return <Text type="secondary">{t("analytics.selectStudy")}</Text>;
  }
  if (isLoading) {
    return <SkeletonPage />;
  }

  return (
    <div>
      <div style={{ marginBottom: 20 }}>
        <Title level={4} style={{ margin: 0 }}>{t("analytics.title")}</Title>
        <Text type="secondary">{currentStudy.name}</Text>
      </div>

      <MetricSection title={t("analytics.section.enrollment")} metrics={data?.enrollment ?? []} />
      <MetricSection title={t("analytics.section.participant")} metrics={data?.participantWork ?? []} />
      <MetricSection title={t("analytics.section.operations")} metrics={data?.operations ?? []} />
    </div>
  );
}

function MetricSection({ title, metrics }: { title: string; metrics: AnalyticsMetricDTO[] }) {
  const { t } = useTranslation();
  return (
    <section style={{ marginBottom: 20 }}>
      <Title level={5} style={{ marginTop: 0 }}>{title}</Title>
      <Row gutter={[12, 12]}>
        {metrics.map((metric) => (
          <Col key={metric.key} xs={12} sm={8} lg={6}>
            <Card>
              <Statistic
                title={t(`analytics.metric.${metric.key}`, { defaultValue: metric.label })}
                value={metric.value}
                suffix={<span style={{ fontSize: 12 }}>{metric.unit}</span>}
              />
            </Card>
          </Col>
        ))}
      </Row>
    </section>
  );
}
