import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  Breadcrumb,
  Descriptions,
  Table,
  Tag,
  Spin,
  Empty,
  Typography,
  Card,
  Space,
  Alert,
  Button,
  Modal,
  Form,
  InputNumber,
  message,
  Popconfirm,
} from "antd";

import { useRuleSet, useRule } from "@/hooks/useRules";
import { useQueryClient } from "@/hooks/useQuery";
import PageHeader from "@/components/PageHeader";

const { Text, Paragraph } = Typography;

function RuleDetailRow({ ruleId }: { ruleId: number }) {
  const { data: detail, isLoading } = useRule(ruleId);
  if (isLoading) return <Spin size="small" />;
  if (!detail) return <Text type="secondary">Failed to load</Text>;
  return (
    <div style={{ padding: "12px 24px" }}>
      {detail.description && (
        <Paragraph>
          <Text strong>Description: </Text>
          {detail.description}
        </Paragraph>
      )}
      {detail.expressionValue && (
        <div>
          <Text strong>Expression: </Text>
          <pre
            style={{
              background: "var(--color-surface-alt, #EFEBE4)",
              padding: 12,
              borderRadius: 8,
              fontSize: 13,
              marginTop: 8,
              whiteSpace: "pre-wrap",
              wordBreak: "break-word",
            }}
          >
            {detail.expressionValue}
          </pre>
        </div>
      )}
    </div>
  );
}

export default function RuleSetDetail() {
  const { studyId, ruleSetId } = useParams<{
    studyId: string;
    ruleSetId: string;
  }>();
  const parsedRuleSetId = ruleSetId ? Number(ruleSetId) : undefined;
  const { data: ruleSet, isLoading } = useRuleSet(parsedRuleSetId);

  const [addOpen, setAddOpen] = useState(false);
  const [addForm] = Form.useForm();
  const queryClient = useQueryClient();
  const parsedRuleSetIdNum = ruleSetId ? Number(ruleSetId) : 0;

  if (isLoading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", padding: 80 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!ruleSet) {
    return (
      <div style={{ padding: 48 }}>
        <Empty description="Rule set not found" />
      </div>
    );
  }

  const ruleColumns = [
    {
      title: "#",
      dataIndex: "index",
      key: "index",
      width: 60,
      render: (_: unknown, __: unknown, i: number) => i + 1,
    },
    {
      title: "Rule Name",
      dataIndex: "name",
      key: "name",
      render: (name: string) => (
        <Space>
          <Text strong>{name}</Text>
        </Space>
      ),
    },
    {
      title: "Status",
      dataIndex: "enabled",
      key: "enabled",
      width: 100,
      render: (enabled: boolean | null) =>
        enabled ? (
          <Tag color="success">
            Active
          </Tag>
        ) : (
          <Tag color="default">
            Inactive
          </Tag>
        ),
    },
    {
      title: "", key: "actions", width: 60,
      render: (_: unknown, record: { ruleId: number }) => (
        <Popconfirm
          title="Remove this rule from the rule set?"
          onConfirm={() => handleRemoveRule(record.ruleId)}
          okText="Remove"
          cancelText="Cancel"
        >
          <Button type="text" size="small" danger />
        </Popconfirm>
      ),
    },
  ];

  const ruleIds = ruleSet.ruleIds ?? [];
  const ruleNames = ruleSet.ruleNames ?? [];
  const ruleData = ruleIds.map((rid, i) => ({
    key: rid,
    name: ruleNames[i] ?? `Rule #${rid}`,
    enabled: null,
    ruleId: rid,
  }));

  const handleAddRule = async () => {
    try {
      const vals = await addForm.validateFields();
      const res = await fetch(`/api/legacy/rule-sets/${parsedRuleSetIdNum}/rules`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ruleId: vals.ruleId }),
      });
      if (!res.ok) throw new Error("Add failed");
      message.success("Rule added");
      setAddOpen(false);
      addForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ["rule-set", parsedRuleSetId] });
    } catch { void 0; }
  };

  const handleRemoveRule = async (ruleId: number) => {
    try {
      const res = await fetch(`/api/legacy/rule-sets/${parsedRuleSetIdNum}/rules/${ruleId}`, {
        method: "DELETE",
      });
      if (!res.ok) throw new Error("Delete failed");
      message.success("Rule removed");
      queryClient.invalidateQueries({ queryKey: ["rule-set", parsedRuleSetId] });
    } catch {
      message.error("Failed to remove rule");
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Breadcrumb
        items={[
          { title: <Link to="/app/dashboard">Dashboard</Link> },
          {
            title: (
              <Link to={`/app/studies/${ruleSet.studyId ?? studyId}`}>
                Study
              </Link>
            ),
          },
          {
            title: (
              <Link
                to={`/app/studies/${ruleSet.studyId ?? studyId}/rules`}
              >
                Rules
              </Link>
            ),
          },
          { title: ruleSet.name ?? `Rule Set #${ruleSet.ruleSetId}` },
        ]}
        style={{ marginBottom: 16 }}
      />

      <PageHeader
        title={ruleSet.name ?? `Rule Set #${ruleSet.ruleSetId}`}
        subtitle="Rule set configuration and associated rules"
      />

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr 1fr",
          gap: 24,
          marginTop: 24,
        }}
      >
        <Card
          title="Details"
          style={{
            borderRadius: 14,
            border: "1px solid var(--color-border-light, #E5E0D8)",
          }}
        >
          <Descriptions column={1} size="small">
            <Descriptions.Item label="Rule Set ID">
              <Text code>{ruleSet.ruleSetId}</Text>
            </Descriptions.Item>
            {ruleSet.crfName && (
              <Descriptions.Item label="CRF">
                {ruleSet.crfName}
                {ruleSet.crfVersionName && (
                  <Text style={{ color: "var(--color-text-secondary)" }}>
                    {" "}
                    ({ruleSet.crfVersionName})
                  </Text>
                )}
              </Descriptions.Item>
            )}
            {ruleSet.eventDefinitionName && (
              <Descriptions.Item label="Event Definition">
                {ruleSet.eventDefinitionName}
              </Descriptions.Item>
            )}
            {ruleSet.description && (
              <Descriptions.Item label="Expression">
                {ruleSet.description}
              </Descriptions.Item>
            )}
          </Descriptions>
        </Card>

        <Card
          title="Info"
          style={{
            borderRadius: 14,
            border: "1px solid var(--color-border-light, #E5E0D8)",
          }}
        >
          <Descriptions column={1} size="small">
            <Descriptions.Item label="Owner ID">
              #{ruleSet.ownerId}
            </Descriptions.Item>
            {ruleSet.dateCreated && (
              <Descriptions.Item label="Created">
                {new Date(ruleSet.dateCreated).toLocaleDateString("en-US", {
                  year: "numeric",
                  month: "long",
                  day: "numeric",
                })}
              </Descriptions.Item>
            )}
            <Descriptions.Item label="Rules Count">
              <Tag color="blue">{ruleSet.ruleNames?.length ?? 0}</Tag>
            </Descriptions.Item>
          </Descriptions>
        </Card>
      </div>

      <Card
        title="Rules in this Rule Set"
        extra={
          <Button type="primary" size="small" onClick={() => setAddOpen(true)}>
            Add Rule
          </Button>
        }
        style={{
          marginTop: 24,
          borderRadius: 14,
          border: "1px solid var(--color-border-light, #E5E0D8)",
        }}
      >
        {ruleData.length > 0 ? (
          <Table
            columns={ruleColumns}
            dataSource={ruleData}
            pagination={false}
            expandable={{
              expandedRowRender: (record) => <RuleDetailRow ruleId={record.ruleId} />,
              rowExpandable: () => true,
            }}
          />
        ) : (
          <Alert
            type="info"
            message="No rules configured"
            description="This rule set does not have any rules associated with it."
            showIcon
          />
        )}
      </Card>

      <Modal
        title="Add Rule to Rule Set"
        open={addOpen}
        onOk={handleAddRule}
        onCancel={() => { setAddOpen(false); addForm.resetFields(); }}
        okText="Add"
      >
        <Form form={addForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="ruleId" label="Rule ID" rules={[{ required: true }]}>
            <InputNumber style={{ width: "100%" }} placeholder="Enter rule ID" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}