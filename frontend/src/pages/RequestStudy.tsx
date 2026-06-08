import { Card, Form, Input, Button, Select, message } from "antd";
import { useNavigate } from "react-router-dom";
import { apiClient } from "@/api/client";

const ROLE_OPTIONS = [
  { value: "investigator", label: "Investigator" },
  { value: "clinical_research_coordinator", label: "Clinical Research Coordinator" },
  { value: "data_entry_person", label: "Data Entry Person" },
  { value: "monitor", label: "Monitor" },
  { value: "data_specialist", label: "Data Specialist" },
];

export default function RequestStudy() {
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const handleSubmit = async (values: Record<string, unknown>) => {
    try {
      await apiClient.post("/api/v1/legacy/request-study", values);
      message.success("Study access request submitted. An administrator will review your request.");
      navigate("/login");
    } catch {
      message.error("Failed to submit request. Please try again.");
    }
  };

  return (
    <div style={{ maxWidth: 480, margin: "60px auto", padding: "0 24px" }}>
      <Card
        title="Request Study Access"
        style={{ border: "1px solid var(--border-light)" }}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="studyName"
            label="Study Name"
            rules={[{ required: true, message: "Required" }]}
          >
            <Input placeholder="Enter study name or identifier" />
          </Form.Item>
          <Form.Item
            name="role"
            label="Requested Role"
            rules={[{ required: true, message: "Required" }]}
          >
            <Select options={ROLE_OPTIONS} placeholder="Select a role" />
          </Form.Item>
          <Form.Item
            name="reason"
            label="Reason for Access"
            rules={[{ required: true, message: "Required" }]}
          >
            <Input.TextArea rows={3} placeholder="Describe why you need access to this study" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block>
            Submit Request
          </Button>
        </Form>
      </Card>
      <div style={{ textAlign: "center", marginTop: 16 }}>
        <Button type="link" onClick={() => navigate("/login")}>
          Back to Login
        </Button>
      </div>
    </div>
  );
}
