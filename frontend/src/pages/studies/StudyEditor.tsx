import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
  Breadcrumb,
  Card,
  Form,
  Input,
  Select,
  Button,
  Space,
  Typography,
  Spin,
  message,
  Divider,
} from "antd";

import type { StudyDetail } from "@/types/study";
import { studyApi } from "@/api/studies";
import { useAppQuery } from "@/hooks/useQuery";

const { Title } = Typography;
const { TextArea } = Input;

export default function StudyEditor() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);
  const parsedId = id ? Number(id) : undefined;
  const { data, isLoading } = useAppQuery<StudyDetail | null>({
    queryKey: ["studies", "detail", parsedId],
    queryFn: () =>
      parsedId
        ? studyApi.getDetail(parsedId)
        : Promise.resolve(null),
    enabled: !!parsedId,
  });

  useEffect(() => {
    if (!data) return;
    form.setFieldsValue({
      ...data,
      datePlannedStart: data.datePlannedStart ?? undefined,
      datePlannedEnd: data.datePlannedEnd ?? undefined,
    });
  }, [data, form]);

  const handleSave = async () => {
    if (!parsedId) return;
    try {
      const values = await form.validateFields();
      setSaving(true);
      await studyApi.update(parsedId, values);
      message.success("Study updated");
      navigate(`/app/studies/${id}`);
    } catch { void 0; }
    finally { setSaving(false); }
  };

  if (isLoading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 80 }}><Spin size="large" /></div>;
  }

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/studies">研究</Link> },
          { title: <Link to={`/app/studies/${id}`}>研究 #{id}</Link> },
          { title: "编辑" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card title={<Title level={4} style={{ margin: 0 }}>编辑研究</Title>}>
        <Form form={form} layout="vertical" style={{ maxWidth: 800 }}>
          <Divider orientation="left" plain>方案信息</Divider>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="name" label="研究名称" rules={[{ required: true }]} style={{ flex: 1 }}>
              <Input />
            </Form.Item>
            <Form.Item name="uniqueIdentifier" label="唯一标识符" style={{ flex: 1 }}>
              <Input />
            </Form.Item>
          </Space>
          <Form.Item name="officialTitle" label="正式标题">
            <Input />
          </Form.Item>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="phase" label="阶段" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Phase I">I 期</Select.Option>
                <Select.Option value="Phase II">II 期</Select.Option>
                <Select.Option value="Phase III">III 期</Select.Option>
                <Select.Option value="Phase IV">IV 期</Select.Option>
                <Select.Option value="N/A">不适用</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="principalInvestigator" label="主要研究者" style={{ flex: 1 }}>
              <Input />
            </Form.Item>
          </Space>
          <Form.Item name="summary" label="摘要">
            <TextArea rows={3} />
          </Form.Item>

          <Divider orientation="left" plain>赞助信息</Divider>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="sponsor" label="赞助方" style={{ flex: 1 }}><Input /></Form.Item>
            <Form.Item name="collaborators" label="合作方" style={{ flex: 1 }}><Input /></Form.Item>
          </Space>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="protocolType" label="方案类型" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Clinical Trial">临床试验</Select.Option>
                <Select.Option value="Observational">观察性研究</Select.Option>
                <Select.Option value="Expanded Access">扩展访问</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="expectedTotalEnrollment" label="预期总入组人数" style={{ flex: 1 }}>
              <Input type="number" />
            </Form.Item>
          </Space>

          <Divider orientation="left" plain>研究设计</Divider>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="purpose" label="目的" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Treatment">治疗</Select.Option>
                <Select.Option value="Prevention">预防</Select.Option>
                <Select.Option value="Diagnostic">诊断</Select.Option>
                <Select.Option value="Basic Science">基础科学</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="allocation" label="分配方式" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Randomized">随机</Select.Option>
                <Select.Option value="Non-Randomized">非随机</Select.Option>
                <Select.Option value="N/A">不适用</Select.Option>
              </Select>
            </Form.Item>
          </Space>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="masking" label="盲法" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Double">双盲</Select.Option>
                <Select.Option value="Single">单盲</Select.Option>
                <Select.Option value="Open">开放</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="gender" label="性别" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="All">全部</Select.Option>
                <Select.Option value="Male">男</Select.Option>
                <Select.Option value="Female">女</Select.Option>
              </Select>
            </Form.Item>
          </Space>
          <Form.Item name="eligibility" label="入选标准">
            <TextArea rows={4} />
          </Form.Item>
          <Form.Item name="conditions" label="疾病条件">
            <Input placeholder="逗号分隔" />
          </Form.Item>

          <Divider orientation="left" plain>机构信息</Divider>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="facilityName" label="机构名称" style={{ flex: 2 }}><Input /></Form.Item>
            <Form.Item name="facilityCity" label="城市" style={{ flex: 1 }}><Input /></Form.Item>
          </Space>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="facilityState" label="州/省" style={{ flex: 1 }}><Input /></Form.Item>
            <Form.Item name="facilityCountry" label="国家" style={{ flex: 1 }}><Input /></Form.Item>
          </Space>

          <Divider />
          <Space>
            <Button type="primary" onClick={handleSave} loading={saving}>
              保存修改
            </Button>
            <Button onClick={() => navigate(`/app/studies/${id}`)}>
              取消
            </Button>
          </Space>
        </Form>
      </Card>
    </div>
  );
}
