import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
  Card, Typography, Button, Space, Spin, Result, Breadcrumb, Tag, message,
} from "antd";
import {
  WarningOutlined, CheckCircleOutlined, UndoOutlined,
  DeleteOutlined, ArrowLeftOutlined,
} from "@ant-design/icons";

const { Title, Text, Paragraph } = Typography;

const ENTITY_LABELS: Record<string, string> = {
  "study": "Study",
  "site": "Site",
  "subject": "Subject",
  "study-subject": "Study Subject",
  "study-event": "Study Event",
  "event-crf": "Event CRF",
  "event-definition": "Event Definition",
  "subject-group-class": "Subject Group Class",
  "study-user-role": "Study User Role",
  "crf": "CRF",
  "crf-version": "CRF Version",
};

interface EntityInfo {
  id: number;
  name: string;
  status?: string;
}

export default function EntityAction() {
  const { entity, action, id } = useParams<{ entity: string; action: string; id: string }>();
  const navigate = useNavigate();
  const [info, setInfo] = useState<EntityInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [performing, setPerforming] = useState(false);
  const [result, setResult] = useState<"idle" | "success" | "error">("idle");

  const entityLabel = ENTITY_LABELS[entity ?? ""] ?? entity ?? "Entity";
  const isRemove = action === "remove";
  const isRestore = action === "restore";

  useEffect(() => {
    if (!entity || !id) return;
    setLoading(true);
    const entityId = Number(id);

    const fetchPromises: Promise<unknown>[] = [];
    if (entity === "study" || entity === "site") {
      fetchPromises.push(
        fetch(`/api/v1/studies/${entityId}`).then(r => r.ok ? r.json() : null)
          .then((data: any) => {
            if (data) setInfo({ id: data.studyId ?? entityId, name: data.name, status: data.status });
          })
      );
    } else if (entity === "subject" || entity === "study-subject") {
      fetchPromises.push(
        fetch(`/api/v1/subjects/${entityId}`).then(r => r.ok ? r.json() : null)
          .then((data: any) => {
            if (data) setInfo({ id: entityId, name: data.uniqueIdentifier ?? `Subject #${entityId}` });
          })
      );
    } else if (entity === "study-event") {
      fetchPromises.push(
        fetch(`/api/v1/events/${entityId}`).then(r => r.ok ? r.json() : null)
          .then((data: any) => {
            if (data) setInfo({ id: entityId, name: data.label ?? `Event #${entityId}`, status: String(data.statusId ?? "") });
          })
      );
    } else if (entity === "crf") {
      fetchPromises.push(
        fetch(`/api/legacy/crfs/${entityId}`).then(r => r.ok ? r.json() : null)
          .then((data: any) => {
            if (data) setInfo({ id: entityId, name: data.name, status: data.status });
          })
      );
    }

    Promise.all(fetchPromises).finally(() => setLoading(false));
  }, [entity, id]);

  const handleConfirm = async () => {
    if (!entity || !id) return;
    setPerforming(true);
    const entityId = Number(id);

    let apiUrl = "";
    if (isRemove) {
      if (entity === "study" || entity === "site") apiUrl = `/api/v1/studies/${entityId}`;
      else if (entity === "crf") apiUrl = `/api/legacy/crfs/${entityId}`;
    } else if (isRestore) {
      // Restore endpoints vary — fallback to legacy
    }

    try {
      if (apiUrl) {
        const method = isRemove ? "DELETE" : "PATCH";
        const res = await fetch(apiUrl, { method });
        if (res.ok || res.status === 204) {
          setResult("success");
          message.success(`${entityLabel} ${isRemove ? "removed" : "restored"} successfully`);
        } else {
          throw new Error("API returned error");
        }
      } else {
        window.location.href = `/legacy/${isRemove ? "Remove" : "Restore"}${entityLabel.replace(/-/g, "")}?id=${id}`;
        return;
      }
    } catch {
      window.location.href = `/legacy/${isRemove ? "Remove" : "Restore"}${entityLabel.replace(/-/g, "")}?id=${id}`;
    }
    setPerforming(false);
  };

  if (loading) return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;

  if (result === "success") {
    return (
      <Result
        status="success"
        icon={<CheckCircleOutlined />}
        title={`${entityLabel} ${isRemove ? "Removed" : "Restored"}`}
        subTitle={info ? `${info.name} has been ${isRemove ? "removed" : "restored"}.` : ""}
        extra={<Button type="primary" onClick={() => navigate(-1)}>Go Back</Button>}
      />
    );
  }

  const backLink = entity === "study" || entity === "site" ? "/app/studies" :
    entity === "subject" || entity === "study-subject" ? "/app/subjects" :
    entity === "crf" ? "/app/admin/crf-library" : "/app/admin";

  return (
    <div>
      <Breadcrumb items={[
        { title: <Link to={backLink}>Back</Link> },
        { title: `${isRemove ? "Remove" : "Restore"} ${entityLabel}` },
      ]} style={{ marginBottom: 16 }} />

      <Card style={{ borderRadius: 14, maxWidth: 560, margin: "40px auto" }}>
        <Space direction="vertical" style={{ width: "100%", textAlign: "center" }} size={16}>
          <div style={{ fontSize: 48, color: isRemove ? "#ff4d4f" : "#52c41a" }}>
            {isRemove ? <WarningOutlined /> : <UndoOutlined />}
          </div>
          <Title level={4}>
            {isRemove ? "Confirm Removal" : "Confirm Restoration"}
          </Title>
          <Paragraph type="secondary">
            Are you sure you want to <Text strong>{isRemove ? "remove" : "restore"}</Text> this {entityLabel}?
            {info && (
              <div style={{ marginTop: 8 }}>
                <Tag>{info.name}</Tag>
                {info.status && <Tag>{info.status}</Tag>}
              </div>
            )}
          </Paragraph>
          <Space>
            <Button type="primary" danger={isRemove}
              icon={isRemove ? <DeleteOutlined /> : <UndoOutlined />}
              onClick={handleConfirm} loading={performing}>
              {isRemove ? "Remove" : "Restore"}
            </Button>
            <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)}>
              Cancel
            </Button>
          </Space>
        </Space>
      </Card>
    </div>
  );
}
