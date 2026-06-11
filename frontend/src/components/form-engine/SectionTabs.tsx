import { Button, Card, Empty, List, Spin, Tabs, Typography } from "antd";
import { DownloadOutlined } from "@ant-design/icons";
import { useEffect, useState } from "react";
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
  const [attachmentFiles, setAttachmentFiles] = useState<string[]>([]);
  const [loadingAttachments, setLoadingAttachments] = useState(false);

  useEffect(() => {
    if (parsedEventCrfId) {
      setLoadingAttachments(true);
      fetch(`/api/v1/data-capture/attachments/list-by-event-crf?eventCrfId=${parsedEventCrfId}`)
        .then((r) => r.json())
        .then(setAttachmentFiles)
        .catch(() => setAttachmentFiles([]))
        .finally(() => setLoadingAttachments(false));
    }
  }, [parsedEventCrfId]);

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
      children: parsedEventCrfId ? (
        <div style={{ padding: 16 }}>
          {loadingAttachments ? (
            <Spin />
          ) : attachmentFiles.length === 0 ? (
            <Empty description={t("entry.noAttachments")} />
          ) : (
            <List
              size="small"
              dataSource={attachmentFiles}
              renderItem={(fileName: string) => (
                <List.Item
                  actions={[
                    <Button
                      type="link"
                      icon={<DownloadOutlined />}
                      href={`/api/v1/data-capture/attachments/by-event-crf?eventCrfId=${parsedEventCrfId}&fileName=${encodeURIComponent(fileName)}`}
                      target="_blank"
                      key="download"
                    >
                      {t("common.download")}
                    </Button>,
                  ]}
                >
                  <Typography.Text>{fileName}</Typography.Text>
                </List.Item>
              )}
            />
          )}
        </div>
      ) : (
        <Empty description={t("entry.noEventCrf")} />
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
