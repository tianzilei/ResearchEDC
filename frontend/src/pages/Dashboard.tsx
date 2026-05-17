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
            <a onClick={() => navigate(0)}>Retry</a>
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
    <div>
      <Title level={4} style={{ marginTop: 0 }}>
        Dashboard
      </Title>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Active Studies"
              value={activeStudies}
              prefix={<MedicineBoxOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Sites"
              value={totalSites}
              prefix={<TeamOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Subjects"
              value={0}
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="CRFs Completed"
              value={0}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Queries Open"
              value={0}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
