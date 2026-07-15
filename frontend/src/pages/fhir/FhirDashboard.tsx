import { useState } from "react";
import { Button, Card, Form, Input, Modal, Select, Space, Table, Tag, Typography, message } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useTranslation } from "react-i18next";
import { formatApiError } from "@/api/errors";
import { useCurrentStudy } from "@/hooks/useStudies";
import { useCreateFhirConnector, useFhirConnectors, useFhirRecords, useReconcileFhirRecord, useSubmitFhirResource, type FhirImportRecordDTO, type FhirImportStatus } from "@/hooks/useFhir";
import { SkeletonPage } from "@/components/SkeletonCard";

const { Text, Title } = Typography;

const STATUS_COLORS: Record<FhirImportStatus, string> = {
  RECEIVED: "default",
  MAPPED: "processing",
  RECONCILED: "success",
  REJECTED: "warning",
  FAILED: "error",
};

export default function FhirDashboard() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const [status, setStatus] = useState<FhirImportStatus | undefined>();
  const [connectorOpen, setConnectorOpen] = useState(false);
  const [resourceOpen, setResourceOpen] = useState(false);
  const [reconciling, setReconciling] = useState<FhirImportRecordDTO | null>(null);
  const [connectorForm] = Form.useForm();
  const [resourceForm] = Form.useForm();
  const [reconcileForm] = Form.useForm();
  const { data: connectors = [], isLoading: connectorsLoading, refetch: refetchConnectors } = useFhirConnectors(currentStudy?.id);
  const { data: records = [], isLoading: recordsLoading, refetch: refetchRecords } = useFhirRecords(currentStudy?.id, status);
  const createConnector = useCreateFhirConnector();
  const submitResource = useSubmitFhirResource();
  const reconcileRecord = useReconcileFhirRecord();

  const submitConnector = async () => {
    if (!currentStudy?.id) return;
    try {
      const values = await connectorForm.validateFields();
      await createConnector.mutateAsync({ ...values, studyId: currentStudy.id });
      message.success(t("fhir.connectorCreated"));
      setConnectorOpen(false);
      connectorForm.resetFields();
      await refetchConnectors();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("fhir.error.connector")));
    }
  };

  const submitPatient = async () => {
    try {
      const values = await resourceForm.validateFields();
      await submitResource.mutateAsync(values);
      message.success(t("fhir.resourceSubmitted"));
      setResourceOpen(false);
      resourceForm.resetFields();
      await refetchRecords();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("fhir.error.resource")));
    }
  };

  const submitReconcile = async () => {
    if (!reconciling) return;
    try {
      const values = await reconcileForm.validateFields();
      await reconcileRecord.mutateAsync({ recordId: reconciling.id, request: values });
      message.success(t("fhir.reconciled"));
      setReconciling(null);
      reconcileForm.resetFields();
      await refetchRecords();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("fhir.error.reconcile")));
    }
  };

  if (!currentStudy) return <Text type="secondary">{t("fhir.selectStudy")}</Text>;
  if (connectorsLoading || recordsLoading) return <SkeletonPage />;

  const columns: ColumnsType<FhirImportRecordDTO> = [
    { title: t("fhir.column.externalId"), dataIndex: "externalId", key: "externalId" },
    { title: t("fhir.column.subject"), dataIndex: "mappedSubjectIdentifier", key: "mappedSubjectIdentifier" },
    { title: t("fhir.column.gender"), dataIndex: "mappedGender", key: "mappedGender" },
    {
      title: t("fhir.column.status"),
      dataIndex: "status",
      key: "status",
      render: (value: FhirImportStatus) => <Tag color={STATUS_COLORS[value]}>{t(`fhir.status.${value.toLowerCase()}`)}</Tag>,
    },
    { title: t("fhir.column.created"), dataIndex: "createdDate", key: "createdDate", render: (v: string) => new Date(v).toLocaleString() },
    {
      title: t("fhir.column.actions"),
      key: "actions",
      render: (_, record) => <Button size="small" onClick={() => { setReconciling(record); reconcileForm.setFieldsValue({ status: record.status, reviewNotes: record.reviewNotes ?? "" }); }}>{t("fhir.action.reconcile")}</Button>,
    },
  ];

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <div>
          <Title level={4} style={{ margin: 0 }}>{t("fhir.title")}</Title>
          <Text type="secondary">{currentStudy.name} · {connectors.length} {t("fhir.connectors")}</Text>
        </div>
        <Space>
          <Select allowClear placeholder={t("fhir.filter.status")} value={status} style={{ width: 170 }} onChange={(value) => setStatus(value)} options={(["MAPPED", "RECONCILED", "REJECTED", "FAILED"] as FhirImportStatus[]).map((value) => ({ value, label: t(`fhir.status.${value.toLowerCase()}`) }))} />
          <Button onClick={() => setConnectorOpen(true)}>{t("fhir.newConnector")}</Button>
          <Button type="primary" onClick={() => setResourceOpen(true)} disabled={connectors.length === 0}>{t("fhir.submitPatient")}</Button>
        </Space>
      </div>

      <Card styles={{ body: { padding: 0 } }}>
        <Table columns={columns} dataSource={records} rowKey="id" pagination={{ pageSize: 20 }} />
      </Card>

      <Modal title={t("fhir.modal.connector")} open={connectorOpen} onOk={submitConnector} onCancel={() => setConnectorOpen(false)} confirmLoading={createConnector.isPending}>
        <Form form={connectorForm} layout="vertical">
          <Form.Item name="name" label={t("fhir.form.name")} rules={[{ required: true }]}><Input maxLength={120} /></Form.Item>
          <Form.Item name="baseUrl" label={t("fhir.form.baseUrl")}><Input maxLength={500} /></Form.Item>
        </Form>
      </Modal>

      <Modal title={t("fhir.modal.patient")} open={resourceOpen} onOk={submitPatient} onCancel={() => setResourceOpen(false)} confirmLoading={submitResource.isPending} width={720}>
        <Form form={resourceForm} layout="vertical">
          <Form.Item name="connectorId" label={t("fhir.form.connector")} rules={[{ required: true }]}>
            <Select options={connectors.map((connector) => ({ value: connector.id, label: connector.name }))} />
          </Form.Item>
          <Form.Item name="payloadJson" label={t("fhir.form.payload")} rules={[{ required: true }]}>
            <Input.TextArea rows={10} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={t("fhir.modal.reconcile")} open={!!reconciling} onOk={submitReconcile} onCancel={() => setReconciling(null)} confirmLoading={reconcileRecord.isPending}>
        <Form form={reconcileForm} layout="vertical">
          <Form.Item name="status" label={t("fhir.form.status")} rules={[{ required: true }]}>
            <Select options={(["RECONCILED", "REJECTED", "FAILED", "MAPPED"] as FhirImportStatus[]).map((value) => ({ value, label: t(`fhir.status.${value.toLowerCase()}`) }))} />
          </Form.Item>
          <Form.Item name="reviewNotes" label={t("fhir.form.notes")}><Input.TextArea rows={4} maxLength={2000} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
