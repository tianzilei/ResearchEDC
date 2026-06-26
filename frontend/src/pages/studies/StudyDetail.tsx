import { useParams, useNavigate, Link } from "react-router-dom";
import {
  Breadcrumb,
  Card,
  Descriptions,
  Tag,
  Typography,
  Button,
  Space,
  Tabs,
  Table,
  Divider,
  Result,
} from "antd";

import { SkeletonPage } from "@/components/SkeletonCard";
import type { StudySummaryItem } from "@/types/study";
import { useAppQuery } from "@/hooks/useQuery";
import { studyApi } from "@/api/studies";

const { Title, Text } = Typography;

export default function StudyDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const parsedId = id ? Number(id) : undefined;
  const { data: study, isLoading } = useAppQuery({
    queryKey: ["studies", "detail", parsedId],
    queryFn: () =>
      parsedId
        ? studyApi.getDetail(parsedId)
        : Promise.resolve(null),
    enabled: !!parsedId,
  });

  const statusLabel = (status: string) => {
    const map: Record<string, { label: string; cls: string }> = {
      available: { label: "进行中", cls: "status-success" },
      pending: { label: "待处理", cls: "status-warning" },
      frozen: { label: "已冻结", cls: "status-info" },
      locked: { label: "已锁定", cls: "status-danger" },
      removed: { label: "已删除", cls: "status-default" },
    };
    return map[status?.toLowerCase()] ?? { label: status, cls: "status-default" };
  };

  if (isLoading) return <SkeletonPage />;
  if (!study) {
    return (
      <Result
        status="404"
        title="项目未找到"
        subTitle={`项目 #${id} 不存在`}
        extra={<Button onClick={() => navigate("/app/studies")}>返回项目列表</Button>}
      />
    );
  }

  const s = statusLabel(study.status);

  const overviewItems = [
    { label: "名称", children: study.name },
    { label: "标识符", children: study.uniqueIdentifier ?? "-" },
    { label: "正式标题", children: study.officialTitle ?? "-" },
    { label: "阶段", children: study.phase ? <Tag>{study.phase}</Tag> : "-" },
    { label: "状态", children: <span className={s.cls}>{s.label}</span> },
    { label: "主要研究者", children: study.principalInvestigator ?? "-" },
    { label: "赞助方", children: study.sponsor ?? "-" },
    { label: "合作方", children: study.collaborators ?? "-" },
    { label: "摘要", children: study.summary ?? "-", span: 2 },
  ];

  const designItems = [
    { label: "目的", children: study.purpose ?? "-" },
    { label: "分组方式", children: study.allocation ?? "-" },
    { label: "盲法", children: study.masking ?? "-" },
    { label: "性别", children: study.gender ?? "-" },
    { label: "疾病/条件", children: study.conditions ?? "-" },
    { label: "关键词", children: study.keywords ?? "-" },
    { label: "纳入标准", children: study.eligibility ?? "-", span: 2 },
  ];

  const facilityItems = [
    { label: "机构名称", children: study.facilityName ?? "-" },
    { label: "城市", children: study.facilityCity ?? "-" },
    { label: "州/省", children: study.facilityState ?? "-" },
    { label: "国家", children: study.facilityCountry ?? "-" },
    { label: "计划开始", children: study.datePlannedStart ? new Date(study.datePlannedStart).toLocaleDateString() : "-" },
    { label: "计划结束", children: study.datePlannedEnd ? new Date(study.datePlannedEnd).toLocaleDateString() : "-" },
    { label: "计划入组人数", children: study.expectedTotalEnrollment ?? "-" },
    { label: "方案类型", children: study.protocolType ?? "-" },
  ];

  const siteColumns = [
    {
      title: "名称", dataIndex: "name", key: "name",
      render: (name: string, record: StudySummaryItem) => (
        <Link to={`/app/studies/${record.studyId}`}>{name}</Link>
      ),
    },
    { title: "标识符", dataIndex: "uniqueIdentifier", key: "uid", render: (v: string | null) => v ?? "-" },
    { title: "主要研究者", dataIndex: "principalInvestigator", key: "pi", render: (v: string | null) => v ?? "-" },
    {
      title: "状态", dataIndex: "status", key: "status",
      render: (s: string) => {
        const st = statusLabel(s);
        return <span className={st.cls}>{st.label}</span>;
      },
    },
  ];

  const tabItems = [
    {
      key: "overview",
      label: "概览",
      children: (
        <div style={{ padding: 16 }}>
          <Descriptions title="方案信息" column={2} items={overviewItems} bordered size="small" />
          <Divider />
          <Descriptions title="研究设计" column={2} items={designItems} bordered size="small" />
          <Divider />
          <Descriptions title="机构与入组" column={2} items={facilityItems} bordered size="small" />
        </div>
      ),
    },
    {
      key: "sites",
      label: <span>站点 ({study.sites?.length ?? 0})</span>,
      children: (
        <div style={{ padding: 16 }}>
          <Space style={{ marginBottom: 16, justifyContent: "space-between", width: "100%" }}>
            <Text strong>站点 — {study.name}</Text>
            <Button type="primary" size="small"
              onClick={() => navigate(`/app/studies/${id}/sites/create`)}>
              添加站点
            </Button>
          </Space>
          <Table
            dataSource={study.sites ?? []}
            columns={siteColumns}
            rowKey="studyId"
            pagination={false}
            locale={{ emptyText: "未定义站点" }}
          />
        </div>
      ),
    },
    {
      key: "actions",
      label: "快捷操作",
      children: (
        <div style={{ padding: 16 }}>
          <Space direction="vertical" style={{ width: "100%" }}>
            <Card hoverable onClick={() => navigate(`/app/studies/${id}/edit`)}
              styles={{ body: { padding: "14px 20px" } }}>
              <div><Text strong>编辑项目</Text><br /><Text style={{ color: "var(--text-secondary)", fontSize: 13 }}>更新项目详情和配置</Text></div>
            </Card>
            <Card hoverable onClick={() => navigate(`/app/subjects`)}
              styles={{ body: { padding: "14px 20px" } }}>
              <div><Text strong>管理受试者</Text><br /><Text style={{ color: "var(--text-secondary)", fontSize: 13 }}>查看和入组受试者</Text></div>
            </Card>
            <Card hoverable onClick={() => navigate(`/app/studies/${id}/event-definitions`)}
              styles={{ body: { padding: "14px 20px" } }}>
              <div><Text strong>事件定义</Text><br /><Text style={{ color: "var(--text-secondary)", fontSize: 13 }}>定义访视事件和 CRF 分配</Text></div>
            </Card>
            <Card hoverable onClick={() => navigate(`/app/studies/${id}/rules`)}
              styles={{ body: { padding: "14px 20px" } }}>
              <div><Text strong>规则</Text><br /><Text style={{ color: "var(--text-secondary)", fontSize: 13 }}>查看和管理规则集</Text></div>
            </Card>
          </Space>
        </div>
      ),
    },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/studies">项目</Link> },
          { title: study.name },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: "14px 20px" } }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>{study.name}</Title>
            <Space split={<Text style={{ color: "var(--text-muted)" }}>|</Text>} style={{ marginTop: 2 }}>
              <Text style={{ color: "var(--text-secondary)", fontSize: 13 }}>{study.uniqueIdentifier ?? "无标识符"}</Text>
              <span className={s.cls} style={{ fontSize: 12 }}>{s.label}</span>
              {study.site && <Tag>站点</Tag>}
            </Space>
          </div>
          <Space>
            <Button onClick={() => navigate(`/app/studies/${id}/edit`)}>
              编辑
            </Button>
            <Button onClick={() => navigate("/app/studies")}>返回</Button>
          </Space>
        </div>
      </Card>

      <Card styles={{ body: { padding: 0 } }}>
        <Tabs items={tabItems} style={{ minHeight: 280 }} />
      </Card>
    </div>
  );
}
