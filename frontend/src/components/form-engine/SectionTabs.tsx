import { Card, Tabs, Button, Spin } from "antd";
import { DataEntryForm } from "@/components/form-engine/DataEntryForm";
import type { FormItemConfig } from "@/components/form-engine/FormField";
import type { FormStatusConfig } from "@/components/form-engine/FormStatus";
import DiscrepancyNotes from "@/components/DiscrepancyNotes";
import { useTranslation } from "react-i18next";

interface SectionTabsProps {
  sections: Array<{ sectionId?: number; title?: string; label?: string }>;
  activeSectionIdx: number;
  onTabChange: (key: string) => void;
  loadingSectionItems: boolean;
  formItems: FormItemConfig[];
  initialFormValues: Record<string, string>;
  statusConfig: FormStatusConfig;
  onSave: (values: Record<string, string>) => Promise<void>;
  enableAutoSave?: boolean;
  parsedEventCrfId: number | undefined;
  saveErrorItemIds?: Set<number>;
}

export function SectionTabs({
  sections,
  activeSectionIdx,
  onTabChange,
  loadingSectionItems,
  formItems,
  initialFormValues,
  statusConfig,
  onSave,
  enableAutoSave = true,
  parsedEventCrfId,
  saveErrorItemIds,
}: SectionTabsProps) {
  const { t } = useTranslation();
  const sectionTabItems = sections.map((section, idx) => ({
    key: String(idx),
    label: section.title || section.label || `Section ${idx + 1}`,
    children: loadingSectionItems ? (
      <div style={{ padding: 40, textAlign: "center" }}>
        <Spin />
      </div>
    ) : (
      <div style={{ padding: 16 }}>
        <DataEntryForm
          items={formItems}
          initialValues={initialFormValues}
          statusConfig={statusConfig}
          onSave={onSave}
          enableAutoSave={enableAutoSave}
          saveErrorItemIds={saveErrorItemIds}
        />
      </div>
    ),
  }));

  const tabItems = [
    ...sectionTabItems,
    {
      key: "notes",
      label: t("entry.notes"),
      children: parsedEventCrfId ? (
        <div style={{ padding: 16 }}>
          <DiscrepancyNotes
            eventCrfId={parsedEventCrfId}
            studyId={0}
            entityId={parsedEventCrfId}
          />
        </div>
      ) : null,
    },
    {
      key: "attachments",
      label: t("entry.attachments"),
      children: (
        <div style={{ padding: 24, textAlign: "center" }}>
          <Button
            onClick={() =>
              window.open(`/DownloadAttachedFile?eventCrfId=${parsedEventCrfId}`, "_blank")
            }
          >
            {t("entry.viewAttachments")}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <Card style={{ borderRadius: 6 }} styles={{ body: { padding: 0 } }}>
      <Tabs
        activeKey={activeSectionIdx !== undefined ? String(activeSectionIdx) : "0"}
        onChange={onTabChange}
        tabPosition="left"
        style={{ minHeight: 400 }}
        items={tabItems}
      />
    </Card>
  );
}
