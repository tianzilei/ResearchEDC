import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button, Spin, Result } from "antd";

import { DataEntryHeader } from "@/components/form-engine/DataEntryHeader";
import { SectionTabs } from "@/components/form-engine/SectionTabs";
import type { FormItemConfig } from "@/components/form-engine/FormField";
import type { FormStatusConfig } from "@/components/form-engine/FormStatus";
import { useCrfVersion, useEventCrfData, useCrfSectionItems } from "@/hooks/useCrf";
import { useEventCrfs, useCompleteEvent } from "@/hooks/useEvents";
import type { ItemDTO } from "@/types/crf";
import { useBatchSaveItems } from "@/hooks/useDataCapture";
import type { SaveItemDataRequest } from "@/types/datacapture";
import { deriveRecordStatus } from "@/utils/crfStatus";

function itemToFormConfig(item: ItemDTO): FormItemConfig {
  return {
    itemId: item.itemId,
    name: item.name,
    description: item.description,
    dataType: item.dataType,
    responseType: item.responseType,
    required: item.required,
    ordinal: item.ordinal,
    units: item.units,
    defaultValue: item.defaultValue,
    regexp: item.regexp,
    regexpErrorMsg: item.regexpErrorMsg,
  };
}

export default function DataEntryPage() {
  const navigate = useNavigate();
  const { subjectId, eventId, eventCrfId } = useParams<{
    subjectId: string;
    eventId: string;
    eventCrfId: string;
  }>();

  const parsedEventCrfId = eventCrfId ? Number(eventCrfId) : undefined;
  const parsedEventId = eventId ? Number(eventId) : undefined;

  const { data: eventCrfs, isLoading: loadingCrfs } = useEventCrfs(parsedEventId);
  const { data: itemData, isLoading: loadingData } = useEventCrfData(parsedEventCrfId);
  const completeEventMutation = useCompleteEvent();
  const batchSaveMutation = useBatchSaveItems(parsedEventCrfId);

  const [activeTab, setActiveTab] = useState<string>("0");
  const [saveResult, setSaveResult] = useState<{ type: "success" | "error" } | null>(null);
  const [saveErrorItemIds, setSaveErrorItemIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (batchSaveMutation.isSuccess) {
      setSaveResult({ type: "success" });
      setSaveErrorItemIds(new Set());
      const timer = setTimeout(() => setSaveResult(null), 2000);
      return () => clearTimeout(timer);
    }
    if (batchSaveMutation.isError) {
      setSaveResult({ type: "error" });
      const timer = setTimeout(() => setSaveResult(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [batchSaveMutation.isSuccess, batchSaveMutation.isError]);

  const saveStatus = useMemo((): "idle" | "saving" | "saved" | "error" => {
    if (batchSaveMutation.isPending) return "saving";
    if (saveResult?.type === "success") return "saved";
    if (saveResult?.type === "error") return "error";
    return "idle";
  }, [batchSaveMutation.isPending, saveResult]);

  const eventCrf = eventCrfs?.find((ec) => ec.eventCrfId === parsedEventCrfId);
  const crfVersionId = eventCrf?.crfVersionId;

  const { data: crfVersion, isLoading: loadingVersion } = useCrfVersion(crfVersionId);

  const sections = crfVersion?.sections ?? [];
  const activeSectionIdx = Number(activeTab);
  const activeSection = sections[activeSectionIdx];

  const { data: sectionItems = [], isLoading: loadingSectionItems } = useCrfSectionItems(
    crfVersionId,
    activeSection?.sectionId,
  );

  const statusId = eventCrf?.statusId ?? 1;
  const recordStatus = deriveRecordStatus(statusId);

  const statusConfig: FormStatusConfig = useMemo(
    () => ({
      status: recordStatus,
      isInitialEntry: recordStatus === "INITIAL" || recordStatus === "DRAFT",
    }),
    [recordStatus],
  );

  const initialFormValues = useMemo(() => {
    if (!itemData) return {};
    const values: Record<string, string> = {};
    for (const item of itemData) {
      if (item.value && !item.deleted) {
        values[`item_${item.itemId}`] = item.value;
      }
    }
    return values;
  }, [itemData]);

  const handleSave = useCallback(
    async (values: Record<string, string>) => {
      if (!parsedEventCrfId) return;

      const items: SaveItemDataRequest[] = Object.entries(values)
        .filter(([key]) => key.startsWith("item_"))
        .map(([key, value]) => ({
          eventCrfId: parsedEventCrfId,
          itemId: Number(key.replace("item_", "")),
          value,
        }));

      if (items.length === 0) return;

      const itemIds = new Set(items.map((item) => item.itemId));

      try {
        await batchSaveMutation.mutateAsync({
          eventCrfId: parsedEventCrfId,
          items,
        });
      } catch {
        setSaveErrorItemIds(itemIds);
      }
    },
    [parsedEventCrfId, batchSaveMutation],
  );

  const handleCompleteEvent = useCallback(async () => {
    if (!parsedEventId) return;
    try {
      await completeEventMutation.mutateAsync(parsedEventId);
      navigate(`/app/subjects/${subjectId}/events`);
    } catch {
      /* mutation error handling is automatic */
    }
  }, [parsedEventId, completeEventMutation, navigate, subjectId]);

  const handleBack = useCallback(() => {
    navigate(`/app/subjects/${subjectId}/events`);
  }, [navigate, subjectId]);

  const formItems: FormItemConfig[] = useMemo(
    () => sectionItems.map((item) => itemToFormConfig(item)),
    [sectionItems],
  );

  const isLoading = loadingCrfs || loadingVersion || loadingData;

  if (isLoading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", padding: 80 }}>
        <Spin size="large" tip="Loading CRF data..." />
      </div>
    );
  }

  if (!eventCrf || !crfVersion) {
    return (
      <Result
        status="404"
        title="CRF Not Found"
        subTitle="The requested CRF data could not be found."
        extra={
          <Button type="primary" onClick={handleBack}>
            Back to Events
          </Button>
        }
      />
    );
  }

  const canComplete =
    recordStatus !== "LOCKED" &&
    recordStatus !== "FROZEN" &&
    recordStatus !== "SIGNED";

  return (
    <div>
      <DataEntryHeader
        crfName={crfVersion.name}
        sectionsCount={sections.length}
        recordStatus={recordStatus}
        saveStatus={saveStatus}
        canComplete={canComplete}
        isCompleting={completeEventMutation.isPending}
        parsedEventCrfId={parsedEventCrfId}
        parsedSubjectId={subjectId}
        onComplete={handleCompleteEvent}
        onBack={handleBack}
      />

      <SectionTabs
        sections={sections}
        activeSectionIdx={activeSectionIdx}
        onTabChange={setActiveTab}
        loadingSectionItems={loadingSectionItems}
        formItems={formItems}
        initialFormValues={initialFormValues}
        statusConfig={statusConfig}
        onSave={handleSave}
        parsedEventCrfId={parsedEventCrfId}
        saveErrorItemIds={saveErrorItemIds}
      />
    </div>
  );
}
