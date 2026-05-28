import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, Form, InputNumber, Button, Typography, Table, Tag, message, Alert, Space, Modal, Select, Empty } from "antd";

import { useTranslation } from "react-i18next";
import { useScheme, useRandomize, useAssignments } from "@/hooks/useRandomization";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Title } = Typography;

export default function AllocationPage() {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const schemeId = Number(id);
  const { data: scheme, isLoading: schemeLoading } = useScheme(schemeId);
  const { data: assignments } = useAssignments(schemeId);
  const randomize = useRandomize();
  const navigate = useNavigate();
  const [subjectId, setSubjectId] = useState<number | undefined>();
  const [stratumValues, setStratumValues] = useState<Record<string, string>>({});

  if (schemeLoading) return <SkeletonPage />;
  if (!scheme) return <Alert message={t("scheme.notFound")} type="error" />;

  if (scheme.status !== "ACTIVE") {
    return (
      <div>
        <Space style={{ marginBottom: 16 }}>
          <Button onClick={() => navigate(`/app/randomization/schemes/${schemeId}`)}>{t("allocation.back")}</Button>
        </Space>
        <Alert
          message={t("allocation.notActive")}
          description={t("allocation.notActiveDescription")}
          type="warning"
        />
      </div>
    );
  }

  const handleRandomize = async () => {
    if (!subjectId) {
      message.error(t("allocation.enterSubjectId"));
      return;
    }
    try {
      const result = await randomize.mutateAsync({
        schemeId,
        studySubjectId: subjectId,
        stratumValues: Object.keys(stratumValues).length > 0 ? stratumValues : undefined,
      });
      Modal.success({
        title: t("allocation.complete"),
        content: (
          <div>
            <p>{t("allocation.assignedTo")} <strong>#{subjectId}</strong>:</p>
            <Tag style={{ padding: "4px 12px" }}>{result.armName}</Tag>
          </div>
        ),
      });
      setSubjectId(undefined);
      setStratumValues({});
    } catch (e: any) {
      message.error(e?.message ?? t("allocation.failed"));
    }
  };

  const statusClasses: Record<string, string> = {
    ACTIVE: "status-success", UNBLINDED: "status-warning", REVOKED: "status-danger",
  };

  const columns = [
    { title: t("allocation.column.subjectId"), dataIndex: "studySubjectId", key: "studySubjectId" },
    { title: t("allocation.column.arm"), dataIndex: "armName", key: "armName",
      render: (name: string) => <Tag>{name}</Tag>,
    },
    { title: t("allocation.column.stratum"), dataIndex: "stratumPath", key: "stratumPath",
      render: (p: string) => p || "-",
    },
    { title: t("allocation.column.status"), dataIndex: "status", key: "status",
      render: (s: string) => <span className={`status ${statusClasses[s] ?? "status-default"}`}>{s}</span>,
    },
    { title: t("allocation.column.assignedDate"), dataIndex: "assignedDate", key: "assignedDate",
      render: (d: string) => d ? new Date(d).toLocaleString() : "-",
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button onClick={() => navigate(`/app/randomization/schemes/${schemeId}`)}>{t("allocation.back")}</Button>
      </Space>

      <Title level={4}>{t("allocation.title")} - {scheme.name}</Title>

      <Card title={t("allocation.newRandomization")} style={{ marginBottom: 16 }}>
        <Form layout="inline">
          <Form.Item label={t("allocation.subjectId")} required>
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
                placeholder={t("allocation.selectValue")}
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
            >
              {t("allocation.randomize")}
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title={`${t("allocation.assignments")} (${assignments?.length ?? 0})`}>
        <Table
          dataSource={assignments ?? []}
          columns={columns}
          rowKey="id"
          pagination={false}
          size="small"
          locale={{ emptyText: <Empty description={t("allocation.empty")} /> }}
        />
      </Card>
    </div>
  );
}
