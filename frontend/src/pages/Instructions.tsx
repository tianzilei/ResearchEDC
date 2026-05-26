import { useParams, Link } from "react-router-dom";
import { Card, Typography, Breadcrumb, Button, Space, Result } from "antd";


const { Title, Paragraph } = Typography;

const INSTRUCTIONS: Record<string, { title: string; content: string; back: string }> = {
  "enter-data": {
    title: "数据录入说明",
    content: `按照 CRF 指南填写每个字段。必填字段用星号 (*) 标记。
      使用 Tab 键在字段之间导航。使用「保存」按钮或启用「自动保存」功能频繁保存工作。
      对于下拉选择和单选字段，从提供的列表中选择合适选项。对于日期字段，使用日期选择器或按 YYYY-MM-DD 格式输入。
      完成所有必填字段后，可以将事件标记为完成。完成后，数据进入审核工作流。`,
    back: "/app/subjects",
  },
  "enroll-subject": {
    title: "受试者入组说明",
    content: `要入组新受试者，首先创建受试者记录，填写唯一标识符和人口学信息。
      然后通过选择研究并指定入组详情，将其分配到研究中。
      受试者可以分配到研究内的特定分组，用于随机化或队列跟踪。
      入组后，可以安排事件并开始为该受试者进行 CRF 数据录入。`,
    back: "/app/subjects",
  },
  "set-up-event": {
    title: "研究事件设置说明",
    content: `研究事件代表临床试验中预定的数据收集点。
      事件在研究级别定义，并指定特定的 CRF 分配。
      设置事件：1) 定义事件类型（如筛选、基线、随访），
      2) 分配相关 CRF 版本，3) 为特定受试者安排事件。
      事件可以是单次或重复的，每个事件跟踪其自身的完成状态。`,
    back: "/app/studies",
  },
  "admin-edit": {
    title: "管理性编辑说明",
    content: `管理性编辑允许数据管理员对已提交数据进行更正。
      所有更改均记录在审计日志中。编辑时：1) 选择受试者和事件，
      2) 定位需要更改的 CRF，3) 进行更正并附上更改说明。
      管理性编辑需要理由，并永久记录在审计跟踪中。`,
    back: "/app/admin",
  },
};

export default function Instructions() {
  const { topic } = useParams<{ topic: string }>();
  const instruction = topic ? INSTRUCTIONS[topic] : null;

  if (!instruction) {
    return (
      <div>
        <Breadcrumb items={[{ title: <Link to="/app">首页</Link> }, { title: "说明" }]}
          style={{ marginBottom: 16 }} />
        <Result
          status="info"
          title="操作说明"
          subTitle="请选择以下主题"
          extra={
            <Space direction="vertical">
              {Object.entries(INSTRUCTIONS).map(([key, val]) => (
                <Link key={key} to={`/app/instructions/${key}`}>
                  <Button type="link">{val.title}</Button>
                </Link>
              ))}
            </Space>
          }
        />
      </div>
    );
  }

  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app">首页</Link> },
          { title: <Link to="/app/instructions">说明</Link> },
          { title: instruction.title },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>{instruction.title}</Title>
        </Space>
        <Paragraph style={{ fontSize: 14, lineHeight: 1.6, whiteSpace: "pre-line" }}>
          {instruction.content}
        </Paragraph>
        <Button onClick={() => window.history.back()}>
          返回
        </Button>
      </Card>
    </div>
  );
}
