import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
  Card, Typography, Button, Space, Spin, Result, Breadcrumb, Tag, message,
} from "antd";


const { Title, Text, Paragraph } = Typography;

const ENTITY_LABELS: Record<string, string> = {
  "study": "研究",
  "site": "站点",
  "subject": "受试者",
  "study-subject": "研究受试者",
  "study-event": "研究事件",
  "event-crf": "事件 CRF",
  "event-definition": "事件定义",
  "subject-group-class": "受试者分组类别",
  "study-user-role": "研究用户角色",
  "crf": "CRF",
  "crf-version": "CRF 版本",
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
          message.success(`${entityLabel} ${isRemove ? "已删除" : "已恢复"}`);
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
        title={`${entityLabel} — ${isRemove ? "已删除" : "已恢复"}`}
        subTitle={info ? `${info.name} 已${isRemove ? "删除" : "恢复"}。` : ""}
        extra={<Button type="primary" onClick={() => navigate(-1)}>返回</Button>}
      />
    );
  }

  const backLink = entity === "study" || entity === "site" ? "/app/studies" :
    entity === "subject" || entity === "study-subject" ? "/app/subjects" :
    entity === "crf" ? "/app/admin/crf-library" : "/app/admin";

  return (
    <div>
      <Breadcrumb items={[
        { title: <Link to={backLink}>返回</Link> },
        { title: `${isRemove ? "删除" : "恢复"} ${entityLabel}` },
      ]} style={{ marginBottom: 16 }} />

      <Card style={{ maxWidth: 560, margin: "40px auto" }}>
        <Space direction="vertical" style={{ width: "100%", textAlign: "center" }} size={16}>

          <Title level={4}>
            {isRemove ? "确认删除" : "确认恢复"}
          </Title>
          <Paragraph type="secondary">
            确定要<Text strong>{isRemove ? "删除" : "恢复"}</Text>此{entityLabel}吗？
            {info && (
              <div style={{ marginTop: 8 }}>
                <Tag>{info.name}</Tag>
                {info.status && <Tag>{info.status}</Tag>}
              </div>
            )}
          </Paragraph>
          <Space>
            <Button type="primary" danger={isRemove}
              onClick={handleConfirm} loading={performing}>
              {isRemove ? "删除" : "恢复"}
            </Button>
            <Button onClick={() => navigate(-1)}>
              取消
            </Button>
          </Space>
        </Space>
      </Card>
    </div>
  );
}
