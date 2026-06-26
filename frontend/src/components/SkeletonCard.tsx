import { Card, Skeleton, Row, Col } from "antd";

interface SkeletonCardProps {
  count?: number;
}

export function SkeletonCard({ count = 4 }: SkeletonCardProps) {
  return (
    <Row gutter={[12, 12]}>
      {Array.from({ length: count }).map((_, i) => (
        <Col key={i} xs={24} sm={12} lg={6}>
          <Card>
            <Skeleton active paragraph={{ rows: 1 }} />
          </Card>
        </Col>
      ))}
    </Row>
  );
}

export function SkeletonStatCard() {
  return (
    <Card styles={{ body: { padding: 16 } }}>
      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
        <Skeleton.Button active style={{ width: 48, height: 24 }} />
        <Skeleton.Button active style={{ width: 80, height: 12 }} />
      </div>
    </Card>
  );
}

export function SkeletonTable() {
  return (
    <Card>
      <Skeleton active paragraph={{ rows: 6 }} />
    </Card>
  );
}

export function SkeletonPage() {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      <Skeleton active paragraph={{ rows: 1 }} style={{ width: 160 }} />
      <Row gutter={[12, 12]}>
        {Array.from({ length: 4 }).map((_, i) => (
          <Col key={i} xs={24} sm={12} lg={3}>
            <SkeletonStatCard />
          </Col>
        ))}
      </Row>
      <Card>
        <Skeleton active paragraph={{ rows: 6 }} />
      </Card>
    </div>
  );
}