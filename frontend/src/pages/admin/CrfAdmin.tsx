import { useState } from "react";
import {
  Card, Table, Button, Typography, Space, Modal, List,
  Form, Input, message, Spin, Empty, Popconfirm,
} from "antd";
import { useNavigate } from "react-router-dom";
import { useAppQuery } from "@/hooks/useQuery";
import { crfManageApi, type CrfManageItem, type CrfVersionManageItem } from "@/api/crfManage";

const { Title, Text } = Typography;

function statusLabel(statusId: number | null | undefined) {
  return statusId === 1 ? "available" : `status ${statusId ?? "unknown"}`;
}

export default function CrfAdmin() {
  const navigate = useNavigate();
  const { data: crfs = [], isLoading, refetch: refetchCrfs } = useAppQuery<CrfManageItem[]>({
    queryKey: ["crfs", "manage"],
    queryFn: () => crfManageApi.listCrfs(),
  });
  const [selectedCrf, setSelectedCrf] = useState<CrfManageItem | null>(null);
  const { data: versions = [], isLoading: versionLoading, refetch: refetchVersions } = useAppQuery<CrfVersionManageItem[]>({
    queryKey: ["crfs", "manage", selectedCrf?.crfId, "versions"],
    queryFn: () =>
      selectedCrf
        ? crfManageApi.listVersions(selectedCrf.crfId)
        : Promise.resolve([]),
    enabled: !!selectedCrf,
  });
  const [versionsOpen, setVersionsOpen] = useState(false);

  const [createCrfOpen, setCreateCrfOpen] = useState(false);
  const [createVersionOpen, setCreateVersionOpen] = useState(false);
  const [crfForm] = Form.useForm();
  const [versionForm] = Form.useForm();

  const viewVersions = (crf: CrfManageItem) => {
    setSelectedCrf(crf);
    setVersionsOpen(true);
  };

  const handleCreateCrf = async () => {
    try {
      const vals = await crfForm.validateFields();
      await crfManageApi.createCrf(vals);
      message.success("CRF 已创建");
      setCreateCrfOpen(false);
      crfForm.resetFields();
      void refetchCrfs();
    } catch { /* validation or API error */ }
  };

  const handleCreateVersion = async () => {
    if (!selectedCrf) return;
    try {
      const vals = await versionForm.validateFields();
      await crfManageApi.createVersion(selectedCrf.crfId, vals);
      message.success("版本已创建");
      setCreateVersionOpen(false);
      versionForm.resetFields();
      void refetchVersions();
    } catch { /* validation or API error */ }
  };

  const handleDeleteVersion = async (versionId: number) => {
    try {
      await crfManageApi.deleteVersion(versionId);
      message.success("版本已删除");
      void refetchVersions();
    } catch {
      message.error("删除版本失败");
    }
  };

  if (isLoading) {
    return <div style={{ padding: 80, textAlign: "center" }}><Spin size="large" /></div>;
  }

  const columns = [
    {
      title: "名称", dataIndex: "name", key: "name",
      render: (text: string) => <>{text}</>,
    },
    { title: "OID", dataIndex: "ocOid", key: "ocOid", render: (v: string | null) => v ?? "-" },
    {
      title: "状态", dataIndex: "statusId", key: "status",
      render: (v: number | null) => <span className={v === 1 ? "status status-success" : "status status-default"}>{statusLabel(v)}</span>,
    },
    {
      title: "创建时间", dataIndex: "dateCreated", key: "created",
      render: (d: string | null) => d ? new Date(d).toLocaleDateString() : "-",
    },
    {
      title: "", key: "actions",
      render: (_: unknown, record: CrfManageItem) => (
        <Space>
          <Button size="small" onClick={() => viewVersions(record)}>
            版本
          </Button>
          <Button size="small"
            onClick={() => {
              const firstVersion = versions.find(v => v.crfId === record.crfId);
              if (firstVersion) navigate(`/app/crfs/${firstVersion.crfVersionId}`);
              else navigate(`/app/crfs/${record.crfId}`);
            }}>
            预览
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>CRF 库</Title>
              <Text type="secondary">{crfs.length} 个病例报告表</Text>
            </div>
          </Space>
          <Button type="primary" onClick={() => setCreateCrfOpen(true)}>
            新建 CRF
          </Button>
        </div>
      </Card>

      <Card>
        <Table dataSource={crfs} columns={columns} rowKey="crfId" pagination={{ pageSize: 20 }}
          locale={{ emptyText: <Empty description="暂无 CRF" /> }} />
      </Card>

      <Modal title="创建 CRF" open={createCrfOpen}
        onOk={handleCreateCrf} onCancel={() => { setCreateCrfOpen(false); crfForm.resetFields(); }}>
        <Form form={crfForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="CRF 名称" rules={[{ required: true }]}>
            <Input placeholder="例如：不良事件表" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={`版本：${selectedCrf?.name ?? ""}`} open={versionsOpen}
        onCancel={() => setVersionsOpen(false)} footer={null} width={640}>
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" size="small"
            onClick={() => setCreateVersionOpen(true)}>
            新建版本
          </Button>
        </Space>
        {versionLoading ? <div style={{ textAlign: "center", padding: 24 }}><Spin /></div> : (
            versions.length === 0 ? <Empty description="暂无版本" /> : (
            <List dataSource={versions} renderItem={(v: CrfVersionManageItem) => (
              <List.Item
                actions={[
                  <Button size="small"
                    onClick={() => navigate(`/app/crfs/${v.crfVersionId}`)}>
                    预览
                  </Button>,
                  <Popconfirm title="确定删除此版本？" onConfirm={() => handleDeleteVersion(v.crfVersionId)}>
                    <Button size="small" danger />
                  </Popconfirm>,
                ]}
              >
                <List.Item.Meta
                  title={<Space>{v.name} <span className="status status-default">{statusLabel(v.statusId)}</span></Space>}
                  description={v.description ?? v.revisionNotes ?? "无描述"}
                />
              </List.Item>
            )} />
          )
        )}
      </Modal>

      <Modal title="创建版本" open={createVersionOpen}
        onOk={handleCreateVersion}
        onCancel={() => { setCreateVersionOpen(false); versionForm.resetFields(); }}>
        <Form form={versionForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="版本名称" rules={[{ required: true }]}>
            <Input placeholder="例如：v1.0" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="revisionNotes" label="修订说明">
            <Input.TextArea rows={2} placeholder="此版本的变更说明" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
