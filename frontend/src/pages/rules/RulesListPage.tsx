import { useParams, Link } from "react-router-dom";
import {
  Breadcrumb,
  Card,
  Table,
  Typography,
  Tag,
  Spin,
  Empty,
  Descriptions,
} from "antd";

import { useRuleSets } from "@/hooks/useRules";
import type { RuleSetDTO } from "@/types/rules";
import type { ColumnsType } from "antd/es/table";

const { Title, Text } = Typography;

export default function RulesListPage() {
  const { studyId } = useParams<{ studyId: string }>();
  const parsedStudyId = studyId ? Number(studyId) : undefined;
  const { data: ruleSets, isLoading } = useRuleSets(parsedStudyId);

  if (isLoading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", padding: 80 }}>
        <Spin size="large" />
      </div>
    );
  }

  const columns: ColumnsType<RuleSetDTO> = [
    {
      title: "Rule Set",
      dataIndex: "name",
      key: "name",
      render: (name, record) => (
        <Link to={`/app/studies/${record.studyId}/rules/${record.ruleSetId}`}>
          {name as string}
        </Link>
      ),
    },
    {
      title: "CRF",
      dataIndex: "crfName",
      key: "crfName",
      render: (val) => val ?? "-",
    },
    {
      title: "Event Definition",
      dataIndex: "eventDefinitionName",
      key: "eventDefinitionName",
      render: (val) => val ?? "-",
    },
    {
      title: "Target",
      dataIndex: "target",
      key: "target",
      ellipsis: true,
      render: (val) =>
        val ? (
          <code style={{ fontSize: 12, background: "var(--panel-muted)", padding: "2px 6px", borderRadius: 4 }}>
            {val}
          </code>
        ) : (
          "-"
        ),
    },
    {
      title: "Rules",
      key: "rules",
      render: (_, record) => (
        <span>
          {record.ruleNames.length > 0
            ? record.ruleNames.map((name) => (
                <Tag key={name} color="blue" style={{ marginBottom: 2 }}>
                  {name}
                </Tag>
              ))
            : <Text type="secondary">None</Text>}
        </span>
      ),
    },
    {
      title: "Created",
      dataIndex: "dateCreated",
      key: "dateCreated",
      width: 120,
      render: (val) =>
        val ? new Date(val as string).toLocaleDateString() : "-",
    },
  ];

  const dataSource = (ruleSets ?? []).map((rs) => ({ ...rs, key: rs.ruleSetId }));

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/studies">Studies</Link> },
          { title: `Study #${parsedStudyId}` },
          { title: "Rules" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card
        style={{ marginBottom: 16, borderRadius: 6 }}
        styles={{ body: { padding: "16px 24px" } }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>
              Rule Sets
            </Title>
            <Text type="secondary">
              {ruleSets?.length ?? 0} rule set{(ruleSets?.length ?? 0) !== 1 ? "s" : ""} defined
            </Text>
          </div>
        </div>
      </Card>

      {!dataSource.length ? (
        <Card style={{ borderRadius: 6 }}>
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="No rule sets defined for this study"
          />
        </Card>
      ) : (
        <Card style={{ borderRadius: 6 }} styles={{ body: { padding: 0 } }}>
          <Table
            dataSource={dataSource}
            columns={columns}
            pagination={false}
            expandable={{
              expandedRowRender: (record) => (
                <div style={{ padding: "8px 0" }}>
                  <Descriptions size="small" column={2}>
                    <Descriptions.Item label="Description">
                      {record.description || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="CRF Version">
                      {record.crfVersionName || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="Study">
                      {record.studyName || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="Created">
                      {record.dateCreated
                        ? new Date(record.dateCreated).toLocaleDateString()
                        : "-"}
                    </Descriptions.Item>
                  </Descriptions>
                  {record.ruleNames.length > 0 && (
                    <div style={{ marginTop: 8 }}>
                      <Text strong style={{ fontSize: 13 }}>
                        Rules ({record.ruleNames.length}):
                      </Text>
                      <div style={{ marginTop: 4 }}>
                        {record.ruleNames.map((name, i) => (
                          <Tag key={i} color="blue" style={{ marginBottom: 4 }}>
                            {name}
                          </Tag>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              ),
              expandIcon: ({ expanded, onExpand, record }) => (
                <span
                  onClick={(e) => onExpand(record, e)}
                  style={{ cursor: "pointer", color: "var(--accent)", display: "inline-block", transform: expanded ? "rotate(90deg)" : "none", transition: "transform 0.2s" }}
                >
                  ▶
                </span>
              ),
            }}
          />
        </Card>
      )}
    </div>
  );
}
