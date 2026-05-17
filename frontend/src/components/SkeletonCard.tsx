import { Card, Skeleton, Row, Col } from "antd";

interface SkeletonCardProps {
  count?: number;
}

export function SkeletonCard({ count = 4 }: SkeletonCardProps) {
  return (
    <Row gutter={[16, 16]}>
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

export function SkeletonTable() {
  return (
    <Card>
      <Skeleton active paragraph={{ rows: 8 }} />
    </Card>
  );
}

export function SkeletonPage() {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      <Skeleton active paragraph={{ rows: 1 }} style={{ width: 200 }} />
      <SkeletonCard count={4} />
      <SkeletonTable />
    </div>
  );
}
