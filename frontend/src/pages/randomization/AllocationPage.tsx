import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, Form, InputNumber, Button, Typography, Table, Tag, message, Alert, Space, Modal, Select, Empty } from "antd";
import { ArrowLeftOutlined, SwapOutlined } from "@ant-design/icons";
import { useScheme, useRandomize, useAssignments } from "@/hooks/useRandomization";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title } = Typography;

export default function AllocationPage() {
  const { id } = useParams<{ id: string }>();
  const schemeId = Number(id);
  const { data: scheme, isLoading: schemeLoading } = useScheme(schemeId);
  const { data: assignments } = useAssignments(schemeId);
  const randomize = useRandomize();
  const navigate = useNavigate();
  const [subjectId, setSubjectId] = useState<number | undefined>();
  const [stratumValues, setStratumValues] = useState<Record<string, string>>({});

  if (schemeLoading) return <SkeletonPage />;
  if (!scheme) return <Alert message="Scheme not found" type="error" showIcon />;

  if (scheme.status !== "ACTIVE") {
    return (
      <div>
        <Space style={{ marginBottom: 16 }}>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(`/app/randomization/schemes/${schemeId}`)}>Back</Button>
        </Space>
        <Alert
          message="Scheme not active"
          description="This scheme must be activated before subjects can be randomized."
          type="warning"
          showIcon
        />
      </div>
    );
  }

  const handleRandomize = async () => {
    if (!subjectId) {
      message.error("Please enter a study subject ID");
      return;
    }
    try {
      const result = await randomize.mutateAsync({
        schemeId,
        studySubjectId: subjectId,
        stratumValues: Object.keys(stratumValues).length > 0 ? stratumValues : undefined,
      });
      Modal.success({
        title: "Randomization Complete",
        content: (
          <div>
            <p>Subject <strong>#{subjectId}</strong> assigned to:</p>
            <Tag color="blue" style={{ fontSize: 16, padding: "4px 12px" }}>{result.armName}</Tag>
          </div>
        ),
      });
      setSubjectId(undefined);
      setStratumValues({});
    } catch (e: any) {
      message.error(e?.message ?? "Randomization failed");
    }
  };

  const statusColors: Record<string, string> = {
    ACTIVE: "success", UNBLINDED: "warning", REVOKED: "error",
  };

  const columns = [
    { title: "Subject ID", dataIndex: "studySubjectId", key: "studySubjectId" },
    { title: "Arm", dataIndex: "armName", key: "armName",
      render: (name: string) => <Tag color="blue">{name}</Tag>,
    },
    { title: "Stratum", dataIndex: "stratumPath", key: "stratumPath",
      render: (p: string) => p || "-",
    },
    { title: "Status", dataIndex: "status", key: "status",
      render: (s: string) => <Tag color={statusColors[s]}>{s}</Tag>,
    },
    { title: "Assigned Date", dataIndex: "assignedDate", key: "assignedDate",
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(`/app/randomization/schemes/${schemeId}`)}>Back</Button>
      </Space>

      <Title level={4}><SwapOutlined /> Allocation - {scheme.name}</Title>

      <Card title="New Randomization" style={{ marginBottom: 16 }}>
        <Form layout="inline">
          <Form.Item label="Study Subject ID" required>
            <InputNumber
              min={1}
              value={subjectId}
              onChange={(v) => setSubjectId(v ?? undefined)}
              style={{ width: 200 }}
            />
          </Form.Item>
          {scheme.stratifications?.map((stratum) => (
            <Form.Item key={stratum.id} label={stratum.name}>
              <Select
                style={{ width: 160 }}
                placeholder="Select value"
                onChange={(v) => setStratumValues(prev => ({ ...prev, [stratum.name]: v }))}
                value={stratumValues[stratum.name]}
              >
                {stratum.options?.map((opt) => (
                  <Select.Option key={opt.value} value={opt.value}>{opt.label}</Select.Option>
                ))}
              </Select>
            </Form.Item>
          ))}
          <Form.Item>
            <Button
              type="primary"
              onClick={handleRandomize}
              loading={randomize.isPending}
              icon={<SwapOutlined />}
            >
              Randomize
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title={`Assignments (${assignments?.length ?? 0})`}>
        <Table
          dataSource={assignments ?? []}
          columns={columns}
          rowKey="id"
          pagination={false}
          size="small"
          locale={{ emptyText: <Empty description="No assignments yet" /> }}
        />
      </Card>
    </div>
  );
}
