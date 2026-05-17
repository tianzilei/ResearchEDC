import { useCallback, useMemo, useState } from "react";
import { Form, Button, Space, Typography, Alert } from "antd";
import { SaveOutlined, LoadingOutlined, CheckCircleOutlined, ExclamationCircleOutlined } from "@ant-design/icons";
import { FormField, type FormItemConfig } from "@/components/form-engine/FormField";
import { useAutoSave } from "@/hooks/useAutoSave";
import { isFieldDisabled, type FormStatusConfig, type FormRecordStatus } from "@/components/form-engine/FormStatus";

const { Text } = Typography;

interface DataEntryFormProps {
  items: FormItemConfig[];
  initialValues?: Record<string, string>;
  statusConfig: FormStatusConfig;
  studySubjectId?: number;
  crfVersionId?: number;
  onSave?: (values: Record<string, string>, status: FormRecordStatus) => Promise<void>;
  enableAutoSave?: boolean;
}

export function DataEntryForm({
  items,
  initialValues,
  statusConfig,
  onSave,
  enableAutoSave = true,
}: DataEntryFormProps) {
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const [formValues, setFormValues] = useState(initialValues ?? {});
  const [manualSaveStatus, setManualSaveStatus] = useState<"idle" | "saved" | "error">("idle");

  const handleFieldChange = useCallback((itemName: string) => (value: unknown) => {
    setFormValues((prev) => ({ ...prev, [itemName]: value as string }));
  }, []);

  const doSave = useCallback(async (values: Record<string, string>) => {
    if (!onSave) return;
    setSaving(true);
    setManualSaveStatus("idle");
    try {
      await onSave(values, statusConfig.status === "INITIAL" ? "DRAFT" : statusConfig.status);
      setManualSaveStatus("saved");
    } catch {
      setManualSaveStatus("error");
    } finally {
      setSaving(false);
    }
  }, [onSave, statusConfig.status]);

  void useAutoSave({
    data: formValues,
    onSave: doSave,
    delay: 3000,
    enabled: enableAutoSave && !!onSave,
  });

  const handleManualSave = useCallback(async () => {
    const values = form.getFieldsValue();
    await doSave(values);
  }, [form, doSave]);

  const fieldsDisabled = useMemo(() => isFieldDisabled(statusConfig), [statusConfig]);

  const statusTag = useMemo(() => {
    if (saving) return { icon: <LoadingOutlined />, text: "Saving...", color: "rgba(0,0,0,0.45)" };
    if (manualSaveStatus === "saved") return { icon: <CheckCircleOutlined />, text: "Saved", color: "#52c41a" };
    if (manualSaveStatus === "error") return { icon: <ExclamationCircleOutlined />, text: "Save failed", color: "#ff4d4f" };
    return null;
  }, [saving, manualSaveStatus]);

  return (
    <div>
      {statusConfig.status === "LOCKED" && (
        <Alert message="This form is locked and cannot be edited." type="warning" showIcon style={{ marginBottom: 16 }} />
      )}
      {statusConfig.status === "FROZEN" && (
        <Alert message="This form is frozen for review. Changes are not permitted." type="info" showIcon style={{ marginBottom: 16 }} />
      )}
      {statusConfig.status === "SIGNED" && (
        <Alert message="This form has been electronically signed and is read-only." type="success" showIcon style={{ marginBottom: 16 }} />
      )}

      <Form
        form={form}
        layout="vertical"
        initialValues={initialValues}
        disabled={fieldsDisabled}
        onValuesChange={() => setManualSaveStatus("idle")}
      >
        {items
          .sort((a, b) => a.ordinal - b.ordinal)
          .map((item) => (
            <FormField
              key={item.itemId}
              item={item}
              value={formValues[item.name]}
              onChange={handleFieldChange(item.name)}
              disabled={fieldsDisabled}
            />
          ))}
      </Form>

      {onSave && (
        <Space style={{ marginTop: 16 }}>
          <Button
            type="primary"
            icon={<SaveOutlined />}
            onClick={handleManualSave}
            loading={saving}
            disabled={fieldsDisabled}
          >
            Save
          </Button>
          {statusTag && (
            <Text style={{ color: statusTag.color }}>
              {statusTag.icon} {statusTag.text}
            </Text>
          )}
        </Space>
      )}
    </div>
  );
}
