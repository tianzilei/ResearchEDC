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
  Input,
  InputNumber,
  Switch,
  message,
  Popconfirm,
} from "antd";

import { useRuleSet, useRule } from "@/hooks/useRules";
import { useQueryClient } from "@/hooks/useQuery";
import PageHeader from "@/components/PageHeader";
import { apiClient } from "@/api/client";

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
              background: "var(--panel-muted)",
              padding: 12,
              borderRadius: 6,
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
  const [createOpen, setCreateOpen] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [createForm] = Form.useForm();
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
          <span className="status status-success">
            Active
          </span>
        ) : (
          <span className="status status-default">
            Inactive
          </span>
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
      await apiClient.post<void>(`/api/v1/rules/rule-sets/${parsedRuleSetIdNum}/rules`, { ruleId: vals.ruleId });
      message.success("Rule added");
      setAddOpen(false);
      addForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ["rule-set", parsedRuleSetId] });
    } catch { void 0; }
  };

  const handleCreateRule = async () => {
    try {
      const vals = await createForm.validateFields();
      setCreateLoading(true);
      // Create the rule via API
      const created: { ruleId: number } = await apiClient.post("/api/v1/rules/rules", {
        name: vals.name,
        description: vals.description ?? "",
        enabled: vals.enabled ?? true,
        expressionValue: vals.expressionValue ?? "",
        expressionContext: null,
      });
      // Auto-add the new rule to the current rule set
      await apiClient.post<void>(`/api/v1/rules/rule-sets/${parsedRuleSetIdNum}/rules`, {
        ruleId: created.ruleId,
      });
      message.success("Rule created and added to rule set");
      setCreateOpen(false);
      createForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ["rule-set", parsedRuleSetId] });
    } catch {
      message.error("Failed to create rule");
    } finally {
      setCreateLoading(false);
    }
  };

  const handleRemoveRule = async (ruleId: number) => {
    try {
      await apiClient.delete<void>(`/api/v1/rules/rule-sets/${parsedRuleSetIdNum}/rules/${ruleId}`);
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
            borderRadius: 6,
            border: "1px solid var(--border-light)",
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
                  <Text style={{ color: "var(--text-secondary)" }}>
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
            borderRadius: 6,
            border: "1px solid var(--border-light)",
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
              <Tag>{ruleSet.ruleNames?.length ?? 0}</Tag>
            </Descriptions.Item>
          </Descriptions>
        </Card>
      </div>

      <Card
        title="Rules in this Rule Set"
        extra={
          <Space>
            <Button type="primary" size="small" onClick={() => setCreateOpen(true)}>
              Create Rule
            </Button>
            <Button size="small" onClick={() => setAddOpen(true)}>
              Add Rule
            </Button>
          </Space>
        }
        style={{
          marginTop: 24,
          borderRadius: 6,
          border: "1px solid var(--border-light)",
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

      <Modal
        title="Create New Rule"
        open={createOpen}
        onOk={handleCreateRule}
        onCancel={() => { setCreateOpen(false); createForm.resetFields(); }}
        confirmLoading={createLoading}
        okText="Create"
      >
        <Form form={createForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="Rule Name" rules={[{ required: true }]}>
            <Input placeholder="Enter rule name" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input placeholder="Optional description" />
          </Form.Item>
          <Form.Item name="enabled" label="Enabled" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
          <Form.Item name="expressionValue" label="Expression">
            <Input.TextArea rows={3} placeholder="Expression value (e.g. age > 18)" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
