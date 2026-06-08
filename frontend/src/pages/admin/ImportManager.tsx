import { useState } from "react";
import {
  Card, Button, Typography, Space, Modal, Upload,
  Select, message, Empty, Breadcrumb, Steps, Tag, Alert,
} from "antd";
import { Link } from "react-router-dom";

const { Title, Text } = Typography;

interface ImportType {
  key: string;
  label: string;
  jsp: string;
  desc: string;
  accept: string;
}

const IMPORT_TYPES: ImportType[] = [
  { key: "crf-data", label: "CRF 数据", jsp: "ImportCRFData", desc: "从 ODM XML 文件导入 CRF 数据", accept: ".xml" },
  { key: "crf-def", label: "CRF 定义", jsp: "ImportCRFInfo", desc: "导入 CRF 定义和元数据", accept: ".xml,.xls,.xlsx" },
];

export default function ImportManager() {
  const [uploadOpen, setUploadOpen] = useState(false);
  const [selectedType, setSelectedType] = useState<string | undefined>();
  const [uploadResult, setUploadResult] = useState<{fileName: string; fileSize: number; storedAs: string} | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);

  const currentImport = IMPORT_TYPES.find(t => t.key === selectedType);

  const handleUpload = async (file: File) => {
    if (!selectedType) {
      message.warning("请选择导入类型");
      return false;
    }
    setUploading(true);
    setUploadError(null);
    const formData = new FormData();
    formData.append("file", file);
    try {
      const res = await fetch("/api/legacy/import/upload", {
        method: "POST",
        body: formData,
      });
      if (res.ok) {
        const data = await res.json();
        setUploadResult(data);
        message.success("文件上传成功");
      } else {
        setUploadError("文件上传失败，请尝试使用旧版导入页面");
      }
    } catch {
      setUploadError("网络错误，请检查连接后重试");
    } finally {
      setUploading(false);
    }
    return false;
  };

  const handleOpenImport = () => {
    if (currentImport) {
      window.open(`/${currentImport.jsp}`, "_blank");
    }
    setUploadOpen(false);
  };

  const handleReset = () => {
    setSelectedType(undefined);
    setUploadResult(null);
    setUploadError(null);
    setUploadOpen(false);
  };

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
              <Text type="secondary">
                导入 CRF 数据和定义 — 选择导入类型并上传文件
              </Text>
            </div>
          </Space>
          <Space>
            <Button onClick={() => setUploadOpen(true)}>
              上传并导入
            </Button>
          </Space>
        </div>
      </Card>

      <Card style={{ marginBottom: 16 }} title="导入类型">
        <Space direction="vertical" style={{ width: "100%" }} size={12}>
          {IMPORT_TYPES.map((item) => (
            <Card key={item.key} size="small" hoverable
              onClick={() => { setSelectedType(item.key); setUploadOpen(true); }}
              style={selectedType === item.key ? { borderColor: "var(--accent)", borderWidth: 2 } : undefined}
            >
              <Space style={{ width: "100%", justifyContent: "space-between" }}>
                <div>
                  <Text strong>{item.label}</Text>
                  <Tag style={{ marginLeft: 8 }}>{item.accept}</Tag>
                  <br />
                  <Text type="secondary">{item.desc}</Text>
                </div>
                {selectedType === item.key && (
                  <Tag color="blue">已选择</Tag>
                )}
              </Space>
            </Card>
          ))}
        </Space>
      </Card>

      <Card title="导入任务">
        <Empty description="导入任务通过旧版 Quartz 调度系统管理。">
          <Button onClick={() => window.open("/ViewImportJob", "_blank")}>
            查看导入任务
          </Button>
        </Empty>
      </Card>

      <Modal
        title={currentImport ? `导入 ${currentImport.label}` : "上传文件导入"}
        open={uploadOpen}
        onCancel={handleReset}
        footer={
          uploadResult ? [
            <Button key="reset" onClick={handleReset}>返回</Button>,
            <Button key="import" type="primary" onClick={handleOpenImport}>
              打开旧版导入页面
            </Button>,
          ] : null
        }
        width={560}
      >
        {!selectedType ? (
          <div style={{ padding: "16px 0" }}>
            <Text type="secondary">请在下方选择导入类型，或点击导入类型卡片开始导入</Text>
            <Select
              placeholder="选择导入类型"
              style={{ width: "100%", marginTop: 12 }}
              value={selectedType}
              onChange={setSelectedType}
              options={IMPORT_TYPES.map(t => ({ value: t.key, label: t.label }))}
            />
          </div>
        ) : !uploadResult ? (
          <Space direction="vertical" style={{ width: "100%", marginTop: 16 }}>
            <Alert
              type="info"
              message={`导入类型: ${currentImport?.label}`}
              description={`支持格式: ${currentImport?.accept}`}
              style={{ marginBottom: 8 }}
            />
            {uploadError && (
              <Alert type="error" message={uploadError} showIcon />
            )}
            <Upload.Dragger
              name="file"
              multiple={false}
              beforeUpload={handleUpload}
              showUploadList={false}
              accept={currentImport?.accept}
              disabled={uploading}
            >
              <p className="ant-upload-text">
                {uploading ? "上传中..." : `点击或拖拽 ${currentImport?.accept} 文件上传`}
              </p>
              <p className="ant-upload-hint">
                文件将暂存于服务器，然后通过旧版页面导入
              </p>
            </Upload.Dragger>
          </Space>
        ) : (
          <div style={{ marginTop: 16 }}>
            <Steps
              current={1}
              size="small"
              items={[
                { title: "选择类型", description: currentImport?.label },
                { title: "上传文件", description: uploadResult.fileName },
                { title: "导入处理", description: "打开旧版导入页面" },
              ]}
            />
            <div style={{ marginTop: 16, padding: "12px 16px", background: "var(--panel-muted)", borderRadius: 6 }}>
              <Text strong>上传完成</Text>
              <br />
              <Text>文件名: {uploadResult.fileName}</Text>
              <br />
              <Text type="secondary">
                大小: {(uploadResult.fileSize / 1024).toFixed(1)} KB
              </Text>
            </div>
            <Alert
              type="warning"
              style={{ marginTop: 12 }}
              message="文件已暂存"
              description="点击下方按钮打开旧版导入页面进行导入处理。完全 SPA 化的导入功能正在开发中。"
              showIcon
            />
          </div>
        )}
      </Modal>
    </div>
  );
}
