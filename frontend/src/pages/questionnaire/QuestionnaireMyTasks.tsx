import { useTranslation } from "react-i18next";
import { Card, List, Tag, Typography, Space, Button, Empty, Alert, Progress, message } from "antd";
import {
  FileTextOutlined,
  RightOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  CalendarOutlined,
} from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { useAppQuery } from "@/hooks/useQuery";
import { apiClient } from "@/api/client";
import { SkeletonPage } from "@/components/SkeletonCard";
import { useAuth } from "@/providers/AuthProvider";

const { Title, Text } = Typography;

interface Assignment {
  id: string;
  study_id: string;
  subject_id: string;
  questionnaire_version_id: string;
  status: string;
  due_at: string | null;
  has_token: boolean;
  token_expires_at: string | null;
  created_at: string;
}

export default function QuestionnaireMyTasks() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { user } = useAuth();

  const statusConfig: Record<string, { color: string; icon: React.ReactNode; label: string }> = {
    pending: { color: "default", icon: <ClockCircleOutlined />, label: t("tasks.pending") },
    in_progress: { color: "processing", icon: <FileTextOutlined />, label: "In Progress" },
    submitted: { color: "success", icon: <CheckCircleOutlined />, label: t("tasks.completed") },
    reviewed: { color: "cyan", icon: <CheckCircleOutlined />, label: "Reviewed" },
    locked: { color: "purple", icon: <CheckCircleOutlined />, label: "Finalized" },
    expired: { color: "warning", icon: <CloseCircleOutlined />, label: "Expired" },
    withdrawn: { color: "error", icon: <CloseCircleOutlined />, label: "Withdrawn" },
  };
  const subjectId = user?.sub;

  const { data: assignments, isLoading } = useAppQuery<Assignment[]>({
    queryKey: ["my-questionnaire-tasks"],
    queryFn: () =>
      apiClient.get<Assignment[]>(
        `/api/v1/questionnaires/assignments/by-subject/${subjectId}`,
      ),
    enabled: !!subjectId,
  });

  if (isLoading) return <SkeletonPage />;
  if (!subjectId) {
    return <Alert message={t("tasks.loginRequired")} type="info" showIcon />;
  }

  const pendingTasks = (assignments ?? []).filter(
    (a) => a.status === "pending" || a.status === "in_progress",
  );
  const completedTasks = (assignments ?? []).filter(
    (a) => a.status === "submitted" || a.status === "reviewed" || a.status === "locked",
  );
  const expiredTasks = (assignments ?? []).filter(
    (a) => a.status === "expired" || a.status === "withdrawn",
  );

  const total = (assignments ?? []).length;
  const completed = completedTasks.length;
  const progressPct = total > 0 ? Math.round((completed / total) * 100) : 0;

  return (
    <div style={{ maxWidth: 800, margin: "0 auto", padding: "24px 16px" }}>
      <Title level={3}>
        <FileTextOutlined /> {t("questionnaire.myTasks")}
      </Title>

      <Card size="small" style={{ marginBottom: 24 }}>
        <Space style={{ width: "100%", justifyContent: "space-around" }}>
          <div style={{ textAlign: "center" }}>
            <Text style={{ fontSize: 24, fontWeight: 600, color: "#1890ff" }}>
              {pendingTasks.length}
            </Text>
            <div>
              <Text type="secondary">{t("tasks.pending")}</Text>
            </div>
          </div>
          <div style={{ textAlign: "center" }}>
            <Text style={{ fontSize: 24, fontWeight: 600, color: "#52c41a" }}>
              {completed}
            </Text>
            <div>
              <Text type="secondary">{t("tasks.completed")}</Text>
            </div>
          </div>
          <div style={{ textAlign: "center" }}>
            <Progress type="circle" percent={progressPct} size={60} />
          </div>
        </Space>
      </Card>

      {pendingTasks.length > 0 && (
        <>
          <Title level={5} style={{ marginTop: 0 }}>
            <ClockCircleOutlined /> {t("tasks.toComplete")}
          </Title>
          <List
            dataSource={pendingTasks}
            renderItem={(item) => (
              <List.Item
                actions={[
                  <Button
                    type="primary"
                    size="small"
                    icon={<RightOutlined />}
                    onClick={() => {
                      if (item.has_token) {
                        message.info(t("tasks.useLink"));
                      } else {
                        navigate(`/app/questionnaires/responses`);
                      }
                    }}
                  >
                    {t("tasks.fill")}
                  </Button>,
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Text>Questionnaire</Text>
                      <Tag color={statusConfig[item.status]?.color}>
                        {statusConfig[item.status]?.label ?? item.status}
                      </Tag>
                    </Space>
                  }
                  description={
                    <Space>
                      {item.due_at && (
                        <Text type="warning" style={{ fontSize: 12 }}>
                          <CalendarOutlined /> {t("tasks.dueLabel")}: {new Date(item.due_at).toLocaleDateString()}
                        </Text>
                      )}
                      {item.token_expires_at && (
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          {t("tasks.tokenExpires")}: {new Date(item.token_expires_at).toLocaleDateString()}
                        </Text>
                      )}
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        </>
      )}

      {completedTasks.length > 0 && (
        <>
          <Title level={5} style={{ marginTop: 24 }}>
            <CheckCircleOutlined /> {t("tasks.completed")}
          </Title>
          <List
            dataSource={completedTasks}
            renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  title={
                    <Space>
                      <Text>Questionnaire</Text>
                      <Tag color={statusConfig[item.status]?.color}>
                        {statusConfig[item.status]?.label ?? item.status}
                      </Tag>
                    </Space>
                  }
                  description={
                    <Text style={{ fontSize: 12 }} type="secondary">
                      {t("tasks.submitted")}
                    </Text>
                  }
                />
              </List.Item>
            )}
          />
        </>
      )}

      {expiredTasks.length > 0 && (
        <>
          <Title level={5} style={{ marginTop: 24, color: "#faad14" }}>
            <CloseCircleOutlined /> {t("tasks.expiredWithdrawn")}
          </Title>
          <List
            dataSource={expiredTasks}
            renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  title={
                    <Space>
                      <Text>Questionnaire</Text>
                      <Tag color={statusConfig[item.status]?.color}>
                        {statusConfig[item.status]?.label ?? item.status}
                      </Tag>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        </>
      )}

      {(assignments ?? []).length === 0 && (
        <Empty description={t("tasks.empty")} />
      )}
    </div>
  );
}
