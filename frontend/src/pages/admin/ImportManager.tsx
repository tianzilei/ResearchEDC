import { useState } from "react";
import {
  Card, Button, Typography, Space, Modal, Upload,
  message, Empty, Breadcrumb,
} from "antd";
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
    { type: "CRF 数据", jsp: "ImportCRFData", desc: "从 ODM XML 文件导入 CRF 数据" },
    { type: "CRF 定义", jsp: "ImportCRFInfo", desc: "导入 CRF 定义和元数据" },
    { type: "规则", jsp: "ImportRuleServlet", desc: "导入规则定义" },
    { type: "受试者", jsp: "ImportSubject", desc: "导入受试者数据" },
  ];

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/admin">管理</Link> },
          { title: "数据导入" },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: "16px 24px" } }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>数据导入</Title>
              <Text type="secondary">导入 CRF 数据、定义、规则和受试者</Text>
            </div>
          </Space>
          <Space>
            <Button type="primary" onClick={() => setUploadOpen(true)}>
              上传并导入
            </Button>
          </Space>
        </div>
      </Card>

      <Card style={{ marginBottom: 16 }} title="导入类型">
        <Space direction="vertical" style={{ width: "100%" }} size={12}>
          {importTypes.map((item) => (
            <Card key={item.type} size="small" hoverable
              onClick={() => window.open(`/legacy/${item.jsp}`, "_blank")}>
              <Space>
                <div>
                  <Text strong>{item.type}</Text>
                  <br />
                  <Text type="secondary">{item.desc}</Text>
                </div>
              </Space>
            </Card>
          ))}
        </Space>
      </Card>

      <Card title="导入历史">
        <Empty description="通过旧版页面导入。历史记录可通过 LegacyFrame 查看。">
          <Button type="primary" onClick={() => window.open("/legacy/ViewImportJobs", "_blank")}>
            查看导入任务
          </Button>
        </Empty>
      </Card>

      <Modal title="上传文件导入" open={uploadOpen}
        onCancel={() => setUploadOpen(false)} footer={null}>
        <Space direction="vertical" style={{ width: "100%", marginTop: 16 }}>
          <Upload.Dragger
            name="file"
            multiple={false}
            beforeUpload={handleUpload}
            showUploadList={false}
          >
            <p className="ant-upload-text">点击或拖拽文件上传</p>
            <p className="ant-upload-hint">支持 ODM XML、CSV 和 XLSX 格式</p>
          </Upload.Dragger>
          <Text type="secondary" style={{ textAlign: "center", display: "block", marginTop: 8 }}>
            复杂导入请使用
            <a onClick={() => { setUploadOpen(false); window.open("/legacy/ImportCRFData", "_blank"); }}
              style={{ marginLeft: 4 }}>
              旧版导入页面
            </a>
          </Text>
        </Space>
      </Modal>
    </div>
  );
}
