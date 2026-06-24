import { Button, Card, Empty, List, Space, Spin, Tabs, Tag, Typography } from "antd";
import { DownloadOutlined, UploadOutlined } from "@ant-design/icons";
import { useEffect, useRef, useState } from "react";
import { DataEntryForm } from "@/components/form-engine/DataEntryForm";
import type { FormItemConfig } from "@/components/form-engine/FormField";
import type { FormStatusConfig } from "@/components/form-engine/FormStatus";
import DiscrepancyNotes from "@/components/DiscrepancyNotes";
import { useEventCrfRules } from "@/hooks/useCrf";
import { useTranslation } from "react-i18next";

interface SectionTabsProps {
  sections: { sectionId?: number; title?: string; label?: string }[];
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
  groupInstances?: Record<number, number[]>;
  onAddGroupInstance?: (groupId: number) => void;
  onRemoveGroupInstance?: (groupId: number, index: number) => void;
  hiddenItemIds?: Set<number>;
  itemDnCounts?: Map<number, number>;
  isAdminEdit?: boolean;
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
  groupInstances,
  onAddGroupInstance,
  onRemoveGroupInstance,
  hiddenItemIds,
  itemDnCounts,
  isAdminEdit,
}: SectionTabsProps) {
  const { t } = useTranslation();
  const [attachmentFiles, setAttachmentFiles] = useState<{ id: string; fileName: string; size: number }[]>([]);
  const [loadingAttachments, setLoadingAttachments] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { data: ruleData, isLoading: loadingRules } = useEventCrfRules(parsedEventCrfId);

  const refreshAttachments = () => {
    if (parsedEventCrfId) {
      setLoadingAttachments(true);
      fetch(`/api/v1/data-capture/events/${parsedEventCrfId}/attachments`)
        .then((r) => r.json())
        .then(setAttachmentFiles)
        .catch(() => setAttachmentFiles([]))
        .finally(() => setLoadingAttachments(false));
    }
  };

  useEffect(() => {
    refreshAttachments();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [parsedEventCrfId]);

  const handleUploadOk = async (file: File) => {
    if (!parsedEventCrfId) return;
    const formData = new FormData();
    formData.append("file", file);
    try {
      await fetch(`/api/v1/data-capture/events/${parsedEventCrfId}/attachments`, {
        method: "POST",
        body: formData,
      });
      refreshAttachments();
    } catch {}
  };

  const sectionTabItems = sections.map((section, idx) => ({
    key: String(idx),
    label: section.title ?? section.label ?? `Section ${idx + 1}`,
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
          groupInstances={groupInstances}
          onAddGroupInstance={onAddGroupInstance}
          onRemoveGroupInstance={onRemoveGroupInstance}
          hiddenItemIds={hiddenItemIds}
          itemDnCounts={itemDnCounts}
          isAdminEdit={isAdminEdit}
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
          <input
            type="file"
            ref={fileInputRef}
            style={{ display: "none" }}
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) handleUploadOk(file);
              e.target.value = "";
            }}
          />
          <Space style={{ marginBottom: 12 }}>
            <Button
              icon={<UploadOutlined />}
              onClick={() => fileInputRef.current?.click()}
            >
              {t("entry.uploadAttachment")}
            </Button>
          </Space>
          {loadingAttachments ? (
            <Spin />
          ) : attachmentFiles.length === 0 ? (
            <Empty description={t("entry.noAttachments")} />
          ) : (
            <List
              size="small"
              dataSource={attachmentFiles}
              renderItem={(attachment) => (
                <List.Item
                  actions={[
                    <Button
                      type="link"
                      icon={<DownloadOutlined />}
                      href={`/api/v1/data-capture/events/${parsedEventCrfId}/attachments/${encodeURIComponent(attachment.id)}`}
                      target="_blank"
                      key="download"
                    >
                      {t("common.download")}
                    </Button>,
                  ]}
                >
                  <Typography.Text>{attachment.fileName}</Typography.Text>
                </List.Item>
              )}
            />
          )}
        </div>
      ) : (
        <Empty description={t("entry.noEventCrf")} />
      ),
    },
    {
      key: "rules",
      label: t("entry.rules"),
      children: parsedEventCrfId ? (
        <div style={{ padding: 16 }}>
          {loadingRules ? (
            <Spin />
          ) : !ruleData || ruleData.rules.length === 0 ? (
            <Empty description={t("entry.noRules")} />
          ) : (
            <List
              size="small"
              header={
                <Typography.Text type="secondary">
                  {ruleData.ruleSetCount} rule set{ruleData.ruleSetCount !== 1 ? "s" : ""}, {ruleData.rules.length} rule{ruleData.rules.length !== 1 ? "s" : ""}
                </Typography.Text>
              }
              dataSource={ruleData.rules}
              renderItem={(rule) => (
                <List.Item
                  actions={[
                    <Tag color={rule.enabled ? "green" : "default"} key="status">
                      {rule.enabled ? "Active" : "Disabled"}
                    </Tag>,
                  ]}
                >
                  <List.Item.Meta
                    title={rule.ruleName || "Unnamed Rule"}
                    description={
                      <div>
                        {rule.ruleDescription && (
                          <Typography.Text type="secondary" style={{ display: "block" }}>
                            {rule.ruleDescription}
                          </Typography.Text>
                        )}
                        {rule.expressionValue && (
                          <Typography.Text code style={{ fontSize: 11 }}>
                            {rule.expressionValue}
                          </Typography.Text>
                        )}
                      </div>
                    }
                  />
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
