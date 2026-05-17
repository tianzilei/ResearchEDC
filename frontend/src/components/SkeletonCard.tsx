import { Card, Skeleton, Row, Col } from "antd";

interface SkeletonCardProps {
  count?: number;
}

export function SkeletonCard({ count = 4 }: SkeletonCardProps) {
  return (
    <Row gutter={[16, 16]}>
      {Array.from({ length: count }).map((_, i) => (
        <Col key={i} xs={24} sm={12} lg={6}>
          <Card style={{ borderRadius: 14 }}>
            <Skeleton active paragraph={{ rows: 1 }} />
          </Card>
        </Col>
      ))}
    </Row>
  );
}

export function SkeletonStatCard() {
  return (
    <Card style={{ borderRadius: 14 }} styles={{ body: { padding: 20 } }}>
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        <Skeleton.Button active shape="circle" style={{ width: 36, height: 36 }} />
        <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
          <Skeleton.Button active style={{ width: 60, height: 28 }} />
          <Skeleton.Button active style={{ width: 100, height: 14 }} />
        </div>
      </div>
    </Card>
  );
}

export function SkeletonTimeline() {
  return (
    <Card style={{ borderRadius: 14 }}>
      <div style={{ display: "flex", flexDirection: "column", gap: 20, padding: "8px 0" }}>
        <Skeleton active avatar paragraph={{ rows: 1 }} />
        <Skeleton active avatar paragraph={{ rows: 1 }} />
        <Skeleton active avatar paragraph={{ rows: 1 }} />
        <Skeleton active avatar paragraph={{ rows: 1 }} />
      </div>
    </Card>
  );
}

export function SkeletonTable() {
  return (
    <Card style={{ borderRadius: 14 }}>
      <Skeleton active paragraph={{ rows: 8 }} />
    </Card>
  );
}

export function SkeletonPage() {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      <Skeleton active paragraph={{ rows: 1 }} style={{ width: 200 }} />
      <Row gutter={[16, 16]}>
        {Array.from({ length: 5 }).map((_, i) => (
          <Col key={i} xs={24} sm={12} lg={4} xl={4}>
            <SkeletonStatCard />
          </Col>
        ))}
      </Row>
      <Row gutter={[24, 24]}>
        <Col xs={24} lg={14}>
          <SkeletonTimeline />
        </Col>
        <Col xs={24} lg={10}>
          <Card style={{ borderRadius: 14, minHeight: 280 }}>
            <Skeleton active paragraph={{ rows: 6 }} />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
