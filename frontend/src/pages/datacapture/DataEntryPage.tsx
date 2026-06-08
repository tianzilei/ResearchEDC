import { useCallback, useMemo, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
  Breadcrumb,
  Card,
  Tabs,
  Typography,
  Space,
  Button,
  Spin,
  Result,
} from "antd";

import { DataEntryForm } from "@/components/form-engine/DataEntryForm";
import type { FormItemConfig } from "@/components/form-engine/FormField";
import type { FormStatusConfig, FormRecordStatus } from "@/components/form-engine/FormStatus";
import { useCrfVersion, useEventCrfData, useCrfSectionItems } from "@/hooks/useCrf";
import { useEventCrfs, useCompleteEvent } from "@/hooks/useEvents";
import { apiClient } from "@/api/client";
import { useQueryClient } from "@tanstack/react-query";
import DiscrepancyNotes from "@/components/DiscrepancyNotes";
import type { ItemDTO } from "@/types/crf";
import type { SaveItemDataRequest } from "@/types/datacapture";

const { Title, Text } = Typography;

const STATUS_MAP: Record<number, FormRecordStatus> = {
  1: "INITIAL",
  2: "DRAFT",
  3: "SUBMITTED",
  4: "LOCKED",
  5: "FROZEN",
  6: "SIGNED",
};

const STATUS_CLASSES: Record<string, string> = {
  INITIAL: "status-default",
  DRAFT: "status-info",
  SUBMITTED: "status-success",
  LOCKED: "status-warning",
  FROZEN: "status-default",
  SIGNED: "status-success",
};

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
  const queryClient = useQueryClient();
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

  const [activeTab, setActiveTab] = useState<string>("0");
  const [saveStatus, setSaveStatus] = useState<"idle" | "saving" | "saved" | "error">("idle");

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
  const recordStatus: FormRecordStatus = STATUS_MAP[statusId] ?? "INITIAL";

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
      setSaveStatus("saving");

      try {
        const items: SaveItemDataRequest[] = Object.entries(values)
          .filter(([key]) => key.startsWith("item_"))
          .map(([key, value]) => ({
            eventCrfId: parsedEventCrfId,
            itemId: Number(key.replace("item_", "")),
            value,
          }));

        if (items.length === 0) {
          setSaveStatus("idle");
          return;
        }

        await apiClient.post("/api/v1/data-capture/items/batch", {
          eventCrfId: parsedEventCrfId,
          items,
        });

        setSaveStatus("saved");
        queryClient.invalidateQueries({ queryKey: ["event-crf-data", parsedEventCrfId] });
        setTimeout(() => setSaveStatus("idle"), 2000);
      } catch {
        setSaveStatus("error");
        setTimeout(() => setSaveStatus("idle"), 3000);
      }
    },
    [parsedEventCrfId, queryClient],
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

  const saveIndicator = () => {
    if (saveStatus === "saving")
      return { text: "Saving...", color: "var(--text-muted)" };
    if (saveStatus === "saved")
      return { text: "Saved", color: "var(--success)" };
    if (saveStatus === "error")
      return { text: "Save failed", color: "var(--danger)" };
    return null;
  };

  const indicator = saveIndicator();

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
          onSave={handleSave}
          enableAutoSave={true}
        />
      </div>
    ),
  }));

  const tabItems = [
    ...sectionTabItems,
    {
      key: "notes",
      label: "Notes",
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
      label: "Attachments",
      children: (
        <div style={{ padding: 24, textAlign: "center" }}>
          <Button onClick={() => window.open(`/DownloadAttachedFile?eventCrfId=${parsedEventCrfId}`, "_blank")}>
            View Attachments
          </Button>
        </div>
      ),
    },
    {
      key: "rules",
      label: "Rules",
      children: (
        <div style={{ padding: 24, textAlign: "center" }}>
          <Button onClick={() => window.open(`/ViewRuleSetAudit?eventCrfId=${parsedEventCrfId}`, "_blank")}>
            Evaluate Rules
          </Button>
        </div>
      ),
    },
  ];

  const canComplete =
    recordStatus !== "LOCKED" &&
    recordStatus !== "FROZEN" &&
    recordStatus !== "SIGNED";

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/subjects">Subjects</Link> },
          { title: <Link to={`/app/subjects/${subjectId}`}>Subject #{Number(subjectId)}</Link> },
          { title: <Link to={`/app/subjects/${subjectId}/events`}>Events</Link> },
          { title: crfVersion.name },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card
        style={{ marginBottom: 16, borderRadius: 6 }}
        styles={{ body: { padding: "16px 24px" } }}
      >
        <Space style={{ width: "100%", justifyContent: "space-between" }} align="center">
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>
                {crfVersion.name}
              </Title>
              <Text type="secondary">
                {sections.length} section{sections.length !== 1 ? "s" : ""}
              </Text>
            </div>
            <span className={`status ${STATUS_CLASSES[recordStatus]}`}>{recordStatus}</span>
          </Space>
          <Space>
            {indicator && (
              <Text style={{ color: indicator.color, fontSize: 13 }}>
                {indicator.text}
              </Text>
            )}
            {canComplete && (
              <Button
                type="primary"
                onClick={handleCompleteEvent}
                loading={completeEventMutation.isPending}
              >
                Complete Event
              </Button>
            )}
            <Button onClick={() => window.open(`/AdministrativeEditing?eventCrfId=${parsedEventCrfId}`, "_blank")}>
              Admin Edit
            </Button>
            <Button onClick={() => window.open(`/PrintCRF?eventCrfId=${parsedEventCrfId}`, "_blank")}>
              Print CRF
            </Button>
            <Button onClick={handleBack}>
              Back
            </Button>
          </Space>
        </Space>
      </Card>

      <Card style={{ borderRadius: 6 }} styles={{ body: { padding: 0 } }}>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          tabPosition="left"
          style={{ minHeight: 400 }}
          items={tabItems}
        />
      </Card>
    </div>
  );
}
