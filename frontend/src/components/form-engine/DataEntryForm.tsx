import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Form, Button, Space, Typography, Alert } from "antd";
import { FormField, type FormItemConfig } from "@/components/form-engine/FormField";
import { RepeatingGroup } from "@/components/form-engine/RepeatingGroup";
import { useAutoSave } from "@/hooks/useAutoSave";
import { useUnsavedChanges } from "@/hooks/useUnsavedChanges";
import { isFieldDisabled, type FormStatusConfig, type FormRecordStatus } from "@/components/form-engine/FormStatus";
import { useTranslation } from "react-i18next";

const { Text } = Typography;

interface GroupInfo {
  groupId: number;
  groupLabel: string;
  items: FormItemConfig[];
}

interface DataEntryFormProps {
  items: FormItemConfig[];
  initialValues?: Record<string, string>;
  statusConfig: FormStatusConfig;
  studySubjectId?: number;
  crfVersionId?: number;
  onSave?: (values: Record<string, string>, status: FormRecordStatus) => Promise<void>;
  enableAutoSave?: boolean;
  saveErrorItemIds?: Set<number>;
  groupInstances?: Record<number, number[]>;
  onAddGroupInstance?: (groupId: number) => void;
  onRemoveGroupInstance?: (groupId: number, index: number) => void;
  hiddenItemIds?: Set<number>;
  itemDnCounts?: Map<number, number>;
  isAdminEdit?: boolean;
}

export function DataEntryForm({
  items,
  initialValues,
  statusConfig,
  onSave,
  enableAutoSave = true,
  saveErrorItemIds,
  groupInstances,
  onAddGroupInstance,
  onRemoveGroupInstance,
  hiddenItemIds,
  itemDnCounts,
  isAdminEdit,
}: DataEntryFormProps) {
  const { t } = useTranslation();
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const [formValues, setFormValues] = useState(initialValues ?? {});
  const [manualSaveStatus, setManualSaveStatus] = useState<"idle" | "saved" | "error">("idle");

  const groupInfoMap = useMemo(() => {
    const map = new Map<number, GroupInfo>();
    for (const item of items) {
      if (item.groupId == null || hiddenItemIds?.has(item.itemId)) continue;
      let g = map.get(item.groupId);
      if (!g) {
        g = { groupId: item.groupId, groupLabel: item.groupLabel ?? `Group ${item.groupId}`, items: [] };
        map.set(item.groupId, g);
      }
      g.items.push(item);
    }
    return map;
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [items]);

  const ungroupedItems = useMemo(() => items.filter(i => i.groupId == null && !hiddenItemIds?.has(i.itemId)), [items, hiddenItemIds]);

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

  const { isDirty } = useAutoSave({
    data: formValues,
    onSave: doSave,
    delay: 3000,
    enabled: enableAutoSave && !!onSave,
  });

  useUnsavedChanges(isDirty);

  const handleManualSave = useCallback(async () => {
    try {
      await form.validateFields();
    } catch {
      return;
    }
    const values = form.getFieldsValue();
    await doSave(values);
  }, [form, doSave]);

  // Ctrl+S / Cmd+S keyboard shortcut
  const handleManualSaveRef = useRef(handleManualSave);
  handleManualSaveRef.current = handleManualSave;
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === "s") {
        e.preventDefault();
        handleManualSaveRef.current();
      }
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, []);

  const fieldsDisabled = useMemo(() => isAdminEdit ? false : isFieldDisabled(statusConfig), [statusConfig, isAdminEdit]);

  const discrepancyCount = useMemo(() => {
    if (!statusConfig.isDoubleEntry || !statusConfig.originalValues) return 0;
    let count = 0;
    for (const item of items) {
      const current = formValues[item.name] ?? "";
      const original = statusConfig.originalValues[item.name];
      if (original !== undefined && original !== current) count++;
    }
    return count;
  }, [statusConfig.isDoubleEntry, statusConfig.originalValues, items, formValues]);

  const statusTag = useMemo(() => {
    if (saving) return { text: t("form.saving"), color: "var(--text-muted)" };
    if (manualSaveStatus === "saved") return { text: t("form.saved"), color: "var(--success)" };
    if (manualSaveStatus === "error") return { text: t("form.saveFailed"), color: "var(--danger)" };
    return null;
  }, [saving, manualSaveStatus, t]);

  return (
    <div>
      {isAdminEdit && (
        <Alert message={t("form.adminEditAlert")} type="warning" showIcon style={{ marginBottom: 16 }} />
      )}
      {statusConfig.status === "LOCKED" && (
        <Alert message={t("form.lockedAlert")} type="warning" style={{ marginBottom: 16 }} />
      )}
      {statusConfig.status === "FROZEN" && (
        <Alert message={t("form.frozenAlert")} type="info" style={{ marginBottom: 16 }} />
      )}
      {statusConfig.status === "SIGNED" && (
        <Alert message={t("form.signedAlert")} type="success" style={{ marginBottom: 16 }} />
      )}
      {statusConfig.isDoubleEntry && (
        <Alert
          message={t("dde.modeAlert")}
          description={
            discrepancyCount > 0
              ? t("dde.discrepancy", { count: discrepancyCount })
              : t("dde.noDiscrepancy")
          }
          type={discrepancyCount > 0 ? "warning" : "info"}
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      <Form
        form={form}
        layout="vertical"
        initialValues={initialValues}
        disabled={fieldsDisabled}
        onValuesChange={() => setManualSaveStatus("idle")}
      >
        {(() => {
          const groupedItems = Array.from(groupInfoMap.values()).map(group => {
            const items = group.items.filter(i => !hiddenItemIds?.has(i.itemId));
            if (items.length === 0) return null;
            return (
              <RepeatingGroup
                key={group.groupId}
                groupLabel={group.groupLabel}
                items={items}
                instanceIndices={groupInstances?.[group.groupId] ?? (group.items.length > 0 ? [0] : [])}
                formValues={formValues}
                onFieldChange={handleFieldChange}
                onAddInstance={() => onAddGroupInstance?.(group.groupId)}
                onRemoveInstance={(idx) => onRemoveGroupInstance?.(group.groupId, idx)}
                disabled={fieldsDisabled}
                saveErrorItemIds={saveErrorItemIds}
              />
            );
          });
          return groupedItems;
        })()}
        {ungroupedItems
          .sort((a, b) => a.ordinal - b.ordinal)
          .map((item) => (
            <FormField
              key={`item_${item.itemId}`}
              item={item}
              value={formValues[`item_${item.itemId}`]}
              onChange={handleFieldChange(`item_${item.itemId}`)}
              disabled={fieldsDisabled}
              hasError={saveErrorItemIds?.has(item.itemId) ?? false}
              dnCount={itemDnCounts?.get(item.itemId)}
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
