import { useMemo } from "react";
import { Card, List, Button, Tag, Empty, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { useStudies, useCurrentStudy } from "@/hooks/useStudies";
import type { Study } from "@/types/study";

const { Text } = Typography;

export default function ChangeStudy() {
  const navigate = useNavigate();
  const { data: summaries, isLoading } = useStudies();
  const { currentStudy, setCurrentStudy } = useCurrentStudy();

  const flatStudies = useMemo(
    () => summaries?.flatMap((g) => [g.study, ...g.sites]) ?? [],
    [summaries],
  );

  const handleSelect = (study: Study) => {
    setCurrentStudy(study);
    navigate("/app/dashboard");
  };

  return (
    <div style={{ maxWidth: 640, margin: "0 auto", padding: "24px" }}>
      <Card
        title="Change Study"
        style={{ border: "1px solid var(--border-light)" }}
      >
        {currentStudy && (
          <div style={{ marginBottom: 16, padding: 12, background: "var(--panel-muted)", borderRadius: 6 }}>
            <Text type="secondary">Current study: </Text>
            <Tag color="blue">{currentStudy.name}</Tag>
          </div>
        )}

        {isLoading ? (
          <div style={{ textAlign: "center", padding: 40 }}>
            <Text type="secondary">Loading studies...</Text>
          </div>
        ) : flatStudies.length === 0 ? (
          <Empty description="No studies available" />
        ) : (
          <List
            dataSource={flatStudies}
            renderItem={(study) => (
              <List.Item
                actions={[
                  <Button
                    key="select"
                    type={currentStudy?.id === study.id ? "default" : "primary"}
                    disabled={currentStudy?.id === study.id}
                    onClick={() => handleSelect(study)}
                  >
                    {currentStudy?.id === study.id ? "Current" : "Select"}
                  </Button>,
                ]}
              >
                <List.Item.Meta
                  title={
                    <Text strong={currentStudy?.id === study.id}>
                      {study.name}
                    </Text>
                  }
                  description={
                    <Text type="secondary">
                      {study.identifier && <Tag style={{ marginRight: 8 }}>{study.identifier}</Tag>}
                      #{study.id}
                    </Text>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Card>
    </div>
  );
}
