import { useCallback, useMemo, useState } from "react";
import { Form, Button, Space, Typography, Alert } from "antd";
import { FormField, type FormItemConfig } from "@/components/form-engine/FormField";
import { useAutoSave } from "@/hooks/useAutoSave";
import { isFieldDisabled, type FormStatusConfig, type FormRecordStatus } from "@/components/form-engine/FormStatus";
import { useTranslation } from "react-i18next";

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
  const { t } = useTranslation();
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
    if (saving) return { text: t("form.saving"), color: "var(--text-muted)" };
    if (manualSaveStatus === "saved") return { text: t("form.saved"), color: "var(--success)" };
    if (manualSaveStatus === "error") return { text: t("form.saveFailed"), color: "var(--danger)" };
    return null;
  }, [saving, manualSaveStatus, t]);

  return (
    <div>
      {statusConfig.status === "LOCKED" && (
        <Alert message={t("form.lockedAlert")} type="warning" style={{ marginBottom: 16 }} />
      )}
      {statusConfig.status === "FROZEN" && (
        <Alert message={t("form.frozenAlert")} type="info" style={{ marginBottom: 16 }} />
      )}
      {statusConfig.status === "SIGNED" && (
        <Alert message={t("form.signedAlert")} type="success" style={{ marginBottom: 16 }} />
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
            onClick={handleManualSave}
            loading={saving}
            disabled={fieldsDisabled}
          >
            {t("form.save")}
          </Button>
          {statusTag && (
            <Text style={{ color: statusTag.color }}>
              {statusTag.text}
            </Text>
          )}
        </Space>
      )}
    </div>
  );
}
