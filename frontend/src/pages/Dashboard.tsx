import { useEffect } from "react";
import { Card, Col, Row, Statistic, Typography, Alert } from "antd";
import {
  TeamOutlined,
  MedicineBoxOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  UserOutlined,
} from "@ant-design/icons";
import { useAuth } from "@/providers/AuthProvider";
import { useStudies } from "@/hooks/useStudies";
import { SkeletonCard } from "@/components/SkeletonCard";
import { useNavigate } from "react-router-dom";

const { Title } = Typography;

const CARD_ACCENTS = ["#099A87", "#D4A854", "#099A87", "#D4A854", "#099A87"];

export default function Dashboard() {
  const { isAuthenticated, isInitialized, login } = useAuth();
  const { data: studies, isLoading, isError } = useStudies();
  const navigate = useNavigate();

  useEffect(() => {
    if (isInitialized && !isAuthenticated) {
      login();
    }
  }, [isInitialized, isAuthenticated, login]);

  if (!isInitialized || isLoading) {
    return (
      <div>
        <Title level={4} style={{ marginTop: 0 }}>
          Dashboard
        </Title>
        <SkeletonCard count={4} />
      </div>
    );
  }

  if (isError) {
    return (
      <div>
        <Title level={4} style={{ marginTop: 0 }}>
          Dashboard
        </Title>
        <Alert
          message="Unable to load dashboard data"
          description="The server could not be reached. Please check your connection and try again."
          type="warning"
          showIcon
          action={
            <a onClick={() => { navigate(0); }}>Retry</a>
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

  const stats = [
    {
      title: "Active Studies",
      value: activeStudies,
      icon: <MedicineBoxOutlined />,
    },
    {
      title: "Sites",
      value: totalSites,
      icon: <TeamOutlined />,
    },
    {
      title: "Subjects",
      value: 0,
      icon: <UserOutlined />,
    },
    {
      title: "CRFs Completed",
      value: 0,
      icon: <FileTextOutlined />,
    },
    {
      title: "Queries Open",
      value: 0,
      icon: <CheckCircleOutlined />,
    },
  ];

  return (
    <div className="animate-in" style={{ animationDuration: "0.45s" }}>
      <Title
        level={3}
        style={{
          marginTop: 0,
          marginBottom: 24,
          paddingLeft: 14,
          borderLeft: "3px solid #099A87",
          fontFamily: "'Sora', sans-serif",
          fontWeight: 600,
        }}
      >
        Dashboard
      </Title>
      <Row gutter={[16, 16]}>
        {stats.map((stat, index) => (
          <Col key={stat.title} xs={24} sm={12} lg={6}>
            <Card
              className="card-hover"
              style={{
                animation: "fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) both",
                animationDelay: `${index * 0.08}s`,
                borderLeft: `3px solid ${CARD_ACCENTS[index] ?? "#099A87"}`,
                borderRadius: 12,
              }}
            >
              <Statistic
                title={stat.title}
                value={stat.value}
                prefix={
                  <span style={{ color: CARD_ACCENTS[index] ?? "#099A87" }}>
                    {stat.icon}
                  </span>
                }
                valueStyle={{ fontFamily: "'Sora', sans-serif", fontWeight: 600 }}
              />
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
