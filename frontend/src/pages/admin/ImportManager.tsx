import { useState } from "react";
import {
  Card, Button, Typography, Space, Modal, Upload,
  message, Empty, Breadcrumb,
} from "antd";
import {
  UploadOutlined, ImportOutlined, FileTextOutlined,
  HistoryOutlined, LinkOutlined,
} from "@ant-design/icons";
import { Link } from "react-router-dom";

const { Title, Text } = Typography;

export default function ImportManager() {
  const [uploadOpen, setUploadOpen] = useState(false);

  const handleUpload = async (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    try {
      const res = await fetch("/api/legacy/import/upload", {
        method: "POST",
        body: formData,
      });
      if (res.ok) {
        message.success("File uploaded for import");
        setUploadOpen(false);
      } else {
        window.open("/legacy/ImportCRFData", "_blank");
      }
    } catch {
      window.open("/legacy/ImportCRFData", "_blank");
    }
    return false;
  };

  const importTypes = [
    { type: "CRF Data", jsp: "ImportCRFData", desc: "Import CRF data from ODM XML files" },
    { type: "CRF Definition", jsp: "ImportCRFInfo", desc: "Import CRF definitions and metadata" },
    { type: "Rules", jsp: "ImportRuleServlet", desc: "Import rule definitions" },
    { type: "Subjects", jsp: "ImportSubject", desc: "Import subject data" },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/admin">Admin</Link> },
          { title: "Import Data" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16, borderRadius: 14 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <ImportOutlined style={{ fontSize: 22, color: "var(--color-primary, #099A87)" }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>Import Data</Title>
              <Text type="secondary">Import CRF data, definitions, rules, and subjects</Text>
            </div>
          </Space>
          <Space>
            <Button type="primary" icon={<UploadOutlined />} onClick={() => setUploadOpen(true)}>
              Upload & Import
            </Button>
          </Space>
        </div>
      </Card>

      <Card style={{ marginBottom: 16, borderRadius: 14 }} title="Import Types">
        <Space direction="vertical" style={{ width: "100%" }} size={12}>
          {importTypes.map((item) => (
            <Card key={item.type} size="small" hoverable style={{ borderRadius: 10 }}
              onClick={() => window.open(`/legacy/${item.jsp}`, "_blank")}>
              <Space>
                <FileTextOutlined style={{ fontSize: 18, color: "var(--color-primary, #099A87)" }} />
                <div>
                  <Text strong>{item.type}</Text>
                  <br />
                  <Text type="secondary">{item.desc}</Text>
                </div>
                <LinkOutlined style={{ color: "var(--color-primary, #099A87)" }} />
              </Space>
            </Card>
          ))}
        </Space>
      </Card>

      <Card style={{ borderRadius: 14 }} title={<span><HistoryOutlined /> Import History</span>}>
        <Empty description="Import via legacy pages. History available through LegacyFrame.">
          <Button type="primary" onClick={() => window.open("/legacy/ViewImportJobs", "_blank")}>
            View Import Jobs
          </Button>
        </Empty>
      </Card>

      <Modal title="Upload File for Import" open={uploadOpen}
        onCancel={() => setUploadOpen(false)} footer={null}>
        <Space direction="vertical" style={{ width: "100%", marginTop: 16 }}>
          <Upload.Dragger
            name="file"
            multiple={false}
            beforeUpload={handleUpload}
            showUploadList={false}
          >
            <p className="ant-upload-drag-icon"><UploadOutlined /></p>
            <p className="ant-upload-text">Click or drag file to upload</p>
            <p className="ant-upload-hint">Supports ODM XML, CSV, and XLSX formats</p>
          </Upload.Dragger>
          <Text type="secondary" style={{ textAlign: "center", display: "block", marginTop: 8 }}>
            For complex imports, use the
            <a onClick={() => { setUploadOpen(false); window.open("/legacy/ImportCRFData", "_blank"); }}
              style={{ marginLeft: 4 }}>
              legacy import page
            </a>
          </Text>
        </Space>
      </Modal>
    </div>
  );
}
