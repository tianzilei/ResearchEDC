import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
  Card, Typography, Button, Space, Spin, Result, Breadcrumb, Tag,
} from "antd";
import { apiClient } from "@/api/client";

const { Title, Text, Paragraph } = Typography;

interface EntityInfo {
  id: number;
  name: string;
  status?: string;
}

const ENTITY_CONFIG: Record<string, { label: string; fetchUrl: (id: number) => string; apiPath: string; backLink: string }> = {
  "study": { label: "Study", fetchUrl: (id) => `/api/v1/studies/${id}`, apiPath: "/api/v1/studies", backLink: "/app/studies" },
  "site": { label: "Site", fetchUrl: (id) => `/api/v1/studies/${id}`, apiPath: "/api/v1/studies", backLink: "/app/studies" },
  "subject": { label: "Subject", fetchUrl: (id) => `/api/v1/subjects/${id}`, apiPath: "/api/v1/subjects", backLink: "/app/subjects" },
  "study-subject": { label: "Study Subject", fetchUrl: (id) => `/api/v1/subjects/enrollment/${id}`, apiPath: "/api/v1/subjects/enrollment", backLink: "/app/subjects" },
  "study-event": { label: "Study Event", fetchUrl: (id) => `/api/v1/events/${id}`, apiPath: "/api/v1/events", backLink: "/app/events" },
  "event-crf": { label: "Event CRF", fetchUrl: (id) => `/api/v1/events/crfs/${id}`, apiPath: "/api/v1/events/crfs", backLink: "/app/events" },
  "event-definition": { label: "Event Definition", fetchUrl: (id) => `/api/v1/events/definitions/${id}`, apiPath: "/api/v1/events/definitions", backLink: "/app/studies" },
  "subject-group-class": { label: "Subject Group Class", fetchUrl: (id) => `/api/v1/subject-groups/classes/${id}`, apiPath: "/api/v1/subject-groups/classes", backLink: "/app/admin" },
  "study-user-role": { label: "Study User Role", fetchUrl: (id) => `/api/v1/identity/roles/${id}`, apiPath: "/api/v1/identity/roles", backLink: "/app/admin" },
  "crf": { label: "CRF", fetchUrl: (id) => `/api/legacy/crfs/${id}`, apiPath: "/api/legacy/crfs", backLink: "/app/admin/crf-library" },
  "crf-version": { label: "CRF Version", fetchUrl: (id) => `/api/legacy/crfs/versions/${id}`, apiPath: "/api/legacy/crfs/versions", backLink: "/app/admin/crf-library" },
  "rule": { label: "Rule", fetchUrl: (id) => `/api/legacy/rules/${id}`, apiPath: "/api/legacy/rules", backLink: "/app/studies" },
};

function parseEntityInfo(entity: string, id: number, data: any): EntityInfo | null {
  if (!data) return null;
  switch (entity) {
    case "study": case "site":
      return { id: data.studyId ?? id, name: data.name, status: data.status };
    case "subject":
      return { id, name: data.uniqueIdentifier ?? `Subject #${id}` };
    case "study-subject":
      return { id, name: data.label ?? data.subjectUniqueIdentifier ?? `Study Subject #${id}` };
    case "study-event":
      return { id, name: data.label ?? `Event #${id}`, status: String(data.statusId ?? "") };
    case "event-definition":
      return { id, name: data.name ?? `Definition #${id}`, status: data.statusId != null ? String(data.statusId) : undefined };
    case "subject-group-class":
      return { id, name: data.name ?? `Class #${id}`, status: data.statusId != null ? String(data.statusId) : undefined };
    case "study-user-role":
      return { id, name: data.roleName ?? `Role #${id}`, status: data.statusId != null ? String(data.statusId) : undefined };
    case "crf": case "crf-version":
      return { id, name: data.name ?? `#${id}`, status: data.status };
    case "rule":
      return { id, name: data.name ?? `Rule #${id}` };
    default:
      return { id, name: `${entity} #${id}` };
  }
}

export default function EntityAction() {
  const { entity, action, id } = useParams<{ entity: string; action: string; id: string }>();
  const navigate = useNavigate();
  const [info, setInfo] = useState<EntityInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [performing, setPerforming] = useState(false);
  const [apiSupported, setApiSupported] = useState(true);
  const [result, setResult] = useState<"idle" | "success" | "error">("idle");

  const config = ENTITY_CONFIG[entity ?? ""];
  const entityLabel = config?.label ?? entity ?? "Entity";
  const isRemove = action === "remove";
  const entityId = id ? Number(id) : 0;

  useEffect(() => {
    if (!entity || !id) return;

    const fetchUrl = config?.fetchUrl(entityId);
    if (!fetchUrl) {
      setLoading(false);
      setApiSupported(false);
      return;
    }

    apiClient
      .get<any>(fetchUrl)
      .then((data) => setInfo(parseEntityInfo(entity, entityId, data)))
      .catch(() => setInfo(null))
      .finally(() => setLoading(false));
  }, [entity, id]);

  const handleConfirm = async () => {
    if (!entity || !config || !config.apiPath) {
      setResult("error");
      return;
    }

    setPerforming(true);
    try {
      if (isRemove) {
        await apiClient.delete(`${config.apiPath}/${entityId}`);
      } else {
        await apiClient.patch(`${config.apiPath}/${entityId}`);
      }
      setResult("success");
    } catch {
      setResult("error");
    } finally {
      setPerforming(false);
    }
  };

  if (loading) {
    return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  }

  if (!apiSupported) {
    return (
      <Result
        status="info"
        title={`${entityLabel} — Action Unavailable`}
        subTitle={`Remove and restore operations for "${entityLabel}" are not yet available in the SPA. Use the legacy admin panel for this action.`}
        extra={<Button type="primary" onClick={() => navigate(-1)}>Back</Button>}
      />
    );
  }

  if (result === "success") {
    return (
      <Result
        status="success"
        title={`${entityLabel} — ${isRemove ? "Removed" : "Restored"}`}
      subTitle={info ? `${info.name} has been ${isRemove ? "removed" : "restored"}.` : ""}
      extra={<Button type="primary" onClick={() => navigate(config?.backLink ?? "/app/admin")}>Go Back</Button>}
      />
    );
  }

  if (result === "error") {
    return (
      <Result
        status="error"
        title="Operation Failed"
        subTitle={`Could not ${isRemove ? "remove" : "restore"} this ${entityLabel.toLowerCase()}. It may be referenced by other data.`}
        extra={<Button onClick={() => setResult("idle")}>Try Again</Button>}
      />
    );
  }

  const backLink = config?.backLink ?? "/app/admin";

  return (
    <div>
      <Breadcrumb items={[
        { title: <Link to={backLink}>Back</Link> },
        { title: `${isRemove ? "Remove" : "Restore"} ${entityLabel}` },
      ]} style={{ marginBottom: 16 }} />

      <Card style={{ maxWidth: 560, margin: "40px auto" }}>
        <Space direction="vertical" style={{ width: "100%", textAlign: "center" }} size={16}>
          <Title level={4}>
            Confirm {isRemove ? "Removal" : "Restore"}
          </Title>
          <Paragraph type="secondary">
            Are you sure you want to <Text strong>{isRemove ? "remove" : "restore"}</Text> this {entityLabel.toLowerCase()}?
            {info && (
              <div style={{ marginTop: 8 }}>
                <Tag>{info.name}</Tag>
                {info.status && <Tag>{info.status}</Tag>}
              </div>
            )}
          </Paragraph>
          <Space>
            <Button
              type="primary"
              danger={isRemove}
              onClick={handleConfirm}
              loading={performing}
            >
              {isRemove ? "Remove" : "Restore"}
            </Button>
            <Button onClick={() => navigate(-1)}>
              Cancel
            </Button>
          </Space>
        </Space>
      </Card>
    </div>
  );
}
