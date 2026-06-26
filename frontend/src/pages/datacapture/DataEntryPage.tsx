import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button, Spin, Result } from "antd";

import { DataEntryHeader } from "@/components/form-engine/DataEntryHeader";
import { SectionTabs } from "@/components/form-engine/SectionTabs";
import DataEntryPrintView from "@/components/form-engine/DataEntryPrintView";
import type { FormItemConfig } from "@/components/form-engine/FormField";
import type { FormStatusConfig } from "@/components/form-engine/FormStatus";
import { useCrfVersion, useEventCrfData, useCrfSectionItems, useItemGroups, useScdRules, useAllSectionItems } from "@/hooks/useCrf";
import { useEventCrfs, useCompleteEvent } from "@/hooks/useEvents";
import { useEventCrfNotes } from "@/hooks/useDiscrepancyNotes";
import type { ItemDTO } from "@/types/crf";
import type { ItemGroupDTO } from "@/types/datacapture";
import { useBatchSaveItems } from "@/hooks/useDataCapture";
import type { SaveItemDataRequest } from "@/types/datacapture";
import { deriveRecordStatus } from "@/utils/crfStatus";
import { useCurrentStudy } from "@/hooks/useStudies";

function itemToFormConfig(item: ItemDTO, groupMap: Map<number, { groupId: number; groupLabel: string }>): FormItemConfig {
  const group = groupMap.get(item.itemId);
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
    groupId: group?.groupId,
    groupLabel: group?.groupLabel,
  };
}

function buildItemGroupMap(
  itemGroups: ItemGroupDTO[],
): Map<number, { groupId: number; groupLabel: string }> {
  const map = new Map<number, { groupId: number; groupLabel: string }>();
  for (const g of itemGroups) {
    for (const itemId of g.items) {
      map.set(itemId, { groupId: g.itemGroupId, groupLabel: g.name });
    }
  }
  return map;
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

  const { currentStudy } = useCurrentStudy();
  const { data: eventCrfs, isLoading: loadingCrfs } = useEventCrfs(parsedEventId);
  const { data: itemData, isLoading: loadingData } = useEventCrfData(parsedEventCrfId);
  const { data: notes = [] } = useEventCrfNotes(parsedEventCrfId);
  const completeEventMutation = useCompleteEvent();
  const batchSaveMutation = useBatchSaveItems(parsedEventCrfId);

  const [activeTab, setActiveTab] = useState<string>("0");
  const [saveResult, setSaveResult] = useState<{ type: "success" | "error" } | null>(null);
  const [saveErrorItemIds, setSaveErrorItemIds] = useState<Set<number>>(new Set());
  const [groupInstances, setGroupInstances] = useState<Record<number, number[]>>({});
  const [isAdminEdit, setIsAdminEdit] = useState(false);
  const [isPrinting, setIsPrinting] = useState(false);

  const handleAddGroupInstance = useCallback((groupId: number) => {
    setGroupInstances(prev => ({
      ...prev,
      [groupId]: [...(prev[groupId] ?? [0]), (prev[groupId]?.length ?? 1)],
    }));
  }, []);

  const handleRemoveGroupInstance = useCallback((groupId: number, index: number) => {
    setGroupInstances(prev => ({
      ...prev,
      [groupId]: (prev[groupId] ?? []).filter(i => i !== index),
    }));
  }, []);

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
  const { data: itemGroups = [], isLoading: loadingGroups } = useItemGroups(crfVersionId);

  const { data: allSectionItems, isLoading: loadingAllItems } = useAllSectionItems(
    crfVersion,
    isPrinting,
  );

  const sections = crfVersion?.sections ?? [];
  const activeSectionIdx = Number(activeTab);
  const activeSection = sections[activeSectionIdx];

  const { data: sectionItems = [], isLoading: loadingSectionItems } = useCrfSectionItems(
    crfVersionId,
    activeSection?.sectionId,
  );
  const { data: scdRules = [] } = useScdRules(activeSection?.sectionId);

  const groupMap = useMemo(() => buildItemGroupMap(itemGroups), [itemGroups]);

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
        const key = item.ordinal != null
          ? `item_${item.itemId}_${item.ordinal}`
          : `item_${item.itemId}`;
        values[key] = item.value;
      }
    }
    return values;
  }, [itemData]);

  const hiddenItemIds = useMemo(() => {
    const hidden = new Set<number>();
    for (const rule of scdRules) {
      if (rule.optionValue) {
        const controlKey = `item_${rule.controlItemId}`;
        const controlValue = initialFormValues[controlKey] ?? "";
        if (controlValue !== rule.optionValue) {
          hidden.add(rule.targetItemId);
        }
      } else {
        hidden.add(rule.targetItemId);
      }
    }
    return hidden;
  }, [scdRules, initialFormValues]);

  const itemDnCounts = useMemo(() => {
    const counts = new Map<number, number>();
    for (const note of notes) {
      if (note.resolutionStatusId === 5) continue;
      const itemDataId = note.entityId;
      const id = itemData?.find(d => d.itemDataId === itemDataId);
      if (id) {
        const itemId = id.itemId;
        counts.set(itemId, (counts.get(itemId) ?? 0) + 1);
      }
    }
    return counts;
  }, [notes, itemData]);

  const handleSave = useCallback(
    async (values: Record<string, string>) => {
      if (!parsedEventCrfId) return;

      const items: SaveItemDataRequest[] = Object.entries(values)
        .filter(([key]) => key.startsWith("item_"))
        .map(([key, value]) => {
          const parts = key.split("_");
          const itemId = Number(parts[1]);
          const ordinal = parts.length > 2 ? Number(parts[2]) : undefined;
          return {
            eventCrfId: parsedEventCrfId,
            itemId,
            value,
            ordinal,
          };
        });

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
    () => sectionItems.map((item) => itemToFormConfig(item, groupMap)),
    [sectionItems, groupMap],
  );

  const handlePrint = useCallback(() => {
    setIsPrinting(true);
    setTimeout(() => {
      window.print();
      setIsPrinting(false);
    }, 100);
  }, []);

  const isLoading = loadingCrfs || loadingVersion || loadingData || loadingGroups;

  if (isLoading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", padding: 80 }}>
        <Spin size="large" tip="Loading CRF data..." />
      </div>
    );
  }

  if (!eventCrf || !crfVersion || !currentStudy) {
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
        parsedSubjectId={subjectId}
        onComplete={handleCompleteEvent}
        onBack={handleBack}
        isAdminEdit={isAdminEdit}
        onToggleAdminEdit={() => setIsAdminEdit(e => !e)}
        onPrint={handlePrint}
      />

      {isPrinting && !loadingAllItems && (
        <DataEntryPrintView
          crfVersion={crfVersion}
          allSectionItems={allSectionItems}
          itemData={itemData ?? []}
          groupMap={groupMap}
          hiddenItemIds={hiddenItemIds}
        />
      )}

      <div className={isPrinting ? "print-hide" : undefined}>
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
          groupInstances={groupInstances}
          onAddGroupInstance={handleAddGroupInstance}
          onRemoveGroupInstance={handleRemoveGroupInstance}
          hiddenItemIds={hiddenItemIds}
          itemDnCounts={itemDnCounts}
          isAdminEdit={isAdminEdit}
          studyId={currentStudy.id}
        />
      </div>
    </div>
  );
}
