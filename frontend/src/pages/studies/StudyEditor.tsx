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
import { SaveOutlined, ArrowLeftOutlined } from "@ant-design/icons";
import type { StudyDetail } from "@/types/study";

const { Title } = Typography;
const { TextArea } = Input;

export default function StudyEditor() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    fetch(`/api/v1/studies/${id}`)
      .then((r) => (r.ok ? r.json() : null))
      .then((data: StudyDetail | null) => {
        if (data) {
          form.setFieldsValue({
            ...data,
            datePlannedStart: data.datePlannedStart ? (data.datePlannedStart as any) : undefined,
            datePlannedEnd: data.datePlannedEnd ? (data.datePlannedEnd as any) : undefined,
          });
        }
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, [id, form]);

  const handleSave = async () => {
    if (!id) return;
    try {
      const values = await form.validateFields();
      setSaving(true);
      const res = await fetch(`/api/v1/studies/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(values),
      });
      if (!res.ok) { message.error("Failed to update study"); return; }
      message.success("Study updated");
      navigate(`/app/studies/${id}`);
    } catch { void 0; }
    finally { setSaving(false); }
  };

  if (loading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 80 }}><Spin size="large" /></div>;
  }

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/studies">Studies</Link> },
          { title: <Link to={`/app/studies/${id}`}>Study #{id}</Link> },
          { title: "Edit" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ borderRadius: 14 }} title={<Title level={4} style={{ margin: 0 }}>Edit Study</Title>}>
        <Form form={form} layout="vertical" style={{ maxWidth: 800 }}>
          <Divider orientation="left" plain>Protocol Information</Divider>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="name" label="Study Name" rules={[{ required: true }]} style={{ flex: 1 }}>
              <Input />
            </Form.Item>
            <Form.Item name="uniqueIdentifier" label="Unique Identifier" style={{ flex: 1 }}>
              <Input />
            </Form.Item>
          </Space>
          <Form.Item name="officialTitle" label="Official Title">
            <Input />
          </Form.Item>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="phase" label="Phase" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Phase I">Phase I</Select.Option>
                <Select.Option value="Phase II">Phase II</Select.Option>
                <Select.Option value="Phase III">Phase III</Select.Option>
                <Select.Option value="Phase IV">Phase IV</Select.Option>
                <Select.Option value="N/A">N/A</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="principalInvestigator" label="Principal Investigator" style={{ flex: 1 }}>
              <Input />
            </Form.Item>
          </Space>
          <Form.Item name="summary" label="Summary">
            <TextArea rows={3} />
          </Form.Item>

          <Divider orientation="left" plain>Sponsorship</Divider>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="sponsor" label="Sponsor" style={{ flex: 1 }}><Input /></Form.Item>
            <Form.Item name="collaborators" label="Collaborators" style={{ flex: 1 }}><Input /></Form.Item>
          </Space>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="protocolType" label="Protocol Type" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Clinical Trial">Clinical Trial</Select.Option>
                <Select.Option value="Observational">Observational</Select.Option>
                <Select.Option value="Expanded Access">Expanded Access</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="expectedTotalEnrollment" label="Expected Total Enrollment" style={{ flex: 1 }}>
              <Input type="number" />
            </Form.Item>
          </Space>

          <Divider orientation="left" plain>Study Design</Divider>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="purpose" label="Purpose" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Treatment">Treatment</Select.Option>
                <Select.Option value="Prevention">Prevention</Select.Option>
                <Select.Option value="Diagnostic">Diagnostic</Select.Option>
                <Select.Option value="Basic Science">Basic Science</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="allocation" label="Allocation" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Randomized">Randomized</Select.Option>
                <Select.Option value="Non-Randomized">Non-Randomized</Select.Option>
                <Select.Option value="N/A">N/A</Select.Option>
              </Select>
            </Form.Item>
          </Space>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="masking" label="Masking" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="Double">Double</Select.Option>
                <Select.Option value="Single">Single</Select.Option>
                <Select.Option value="Open">Open</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="gender" label="Gender" style={{ flex: 1 }}>
              <Select allowClear>
                <Select.Option value="All">All</Select.Option>
                <Select.Option value="Male">Male</Select.Option>
                <Select.Option value="Female">Female</Select.Option>
              </Select>
            </Form.Item>
          </Space>
          <Form.Item name="eligibility" label="Eligibility Criteria">
            <TextArea rows={4} />
          </Form.Item>
          <Form.Item name="conditions" label="Conditions">
            <Input placeholder="Comma-separated" />
          </Form.Item>

          <Divider orientation="left" plain>Facility</Divider>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="facilityName" label="Facility Name" style={{ flex: 2 }}><Input /></Form.Item>
            <Form.Item name="facilityCity" label="City" style={{ flex: 1 }}><Input /></Form.Item>
          </Space>
          <Space style={{ width: "100%" }} size={16}>
            <Form.Item name="facilityState" label="State" style={{ flex: 1 }}><Input /></Form.Item>
            <Form.Item name="facilityCountry" label="Country" style={{ flex: 1 }}><Input /></Form.Item>
          </Space>

          <Divider />
          <Space>
            <Button type="primary" icon={<SaveOutlined />} onClick={handleSave} loading={saving}>
              Save Changes
            </Button>
            <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(`/app/studies/${id}`)}>
              Cancel
            </Button>
          </Space>
        </Form>
      </Card>
    </div>
  );
}
