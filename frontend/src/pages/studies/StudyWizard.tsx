import { useState, useCallback } from "react";
import {
  Card,
  Steps,
  Button,
  Form,
  Input,
  Select,
  DatePicker,
  InputNumber,
  Typography,
  Space,
  Descriptions,
  Result,
  message,
} from "antd";
import type { Dayjs } from "dayjs";

import { useNavigate } from "react-router-dom";

const { Title, Text } = Typography;
const { TextArea } = Input;

interface StudyFormValues {
  name?: string;
  uniqueIdentifier?: string;
  officialTitle?: string;
  phase?: string;
  summary?: string;
  sponsor?: string;
  collaborators?: string;
  protocolType?: string;
  protocolDescription?: string;
  purpose?: string;
  allocation?: string;
  masking?: string;
  control?: string;
  assignment?: string;
  endpoint?: string;
  interventions?: string;
  duration?: string;
  selection?: string;
  timing?: string;
  gender?: string;
  eligibility?: string;
  conditions?: string;
  keywords?: string;
  expectedTotalEnrollment?: number;
  datePlannedStart?: Dayjs;
  datePlannedEnd?: Dayjs;
  facilityName?: string;
  facilityCity?: string;
  facilityState?: string;
  facilityCountry?: string;
  facilityRecruitmentStatus?: string;
  facilityContactName?: string;
  facilityContactDegree?: string;
  facilityContactPhone?: string;
}

interface StepDef {
  title: string;
  fields: (keyof StudyFormValues)[];
}

const STEPS: StepDef[] = [
  {
    title: "协议信息",
    fields: ["name", "uniqueIdentifier", "officialTitle", "phase", "summary"],
  },
  {
    title: "赞助信息",
    fields: ["sponsor", "collaborators", "protocolType", "protocolDescription"],
  },
  {
    title: "研究设计",
    fields: [
      "purpose", "allocation", "masking", "control", "assignment",
      "endpoint", "interventions", "duration", "selection", "timing", "gender",
    ],
  },
  {
    title: "入组标准",
    fields: ["eligibility", "conditions", "keywords"],
  },
  {
    title: "招募计划",
    fields: ["expectedTotalEnrollment", "datePlannedStart", "datePlannedEnd"],
  },
  {
    title: "研究机构",
    fields: [
      "facilityName", "facilityCity", "facilityState",
      "facilityCountry", "facilityRecruitmentStatus",
    ],
  },
  {
    title: "联系方式",
    fields: [
      "facilityContactName", "facilityContactDegree",
      "facilityContactPhone",
    ],
  },
  {
    title: "确认提交",
    fields: [],
  },
];

const PHASE_OPTIONS = ["Phase I", "Phase II", "Phase III", "Phase IV", "N/A"].map((v) => ({
  label: v,
  value: v,
}));

const PROTOCOL_TYPE_OPTIONS = [
  "Interventional",
  "Observational",
  "Expanded Access",
  "Program",
].map((v) => ({ label: v, value: v }));

const PURPOSE_OPTIONS = [
  "Treatment",
  "Prevention",
  "Diagnostic",
  "Supportive Care",
  "Screening",
  "Health Services Research",
  "Basic Science",
  "Device Feasibility",
  "Other",
].map((v) => ({ label: v, value: v }));

const ALLOCATION_OPTIONS = [
  "Randomized",
  "Non-Randomized",
].map((v) => ({ label: v, value: v }));

const MASKING_OPTIONS = [
  "None",
  "Single",
  "Double",
  "Triple",
  "Quadruple",
].map((v) => ({ label: v, value: v }));

const CONTROL_OPTIONS = [
  "Placebo",
  "Active Comparator",
  "No Intervention",
  "Other",
].map((v) => ({ label: v, value: v }));

const ASSIGNMENT_OPTIONS = [
  "Parallel Assignment",
  "Crossover Assignment",
  "Single Group Assignment",
  "Factorial Assignment",
  "Sequential Assignment",
].map((v) => ({ label: v, value: v }));

const SELECTION_OPTIONS = [
  "Probability Sample",
  "Non-Probability Sample",
].map((v) => ({ label: v, value: v }));

const TIMING_OPTIONS = [
  "Cross-Sectional",
  "Longitudinal",
].map((v) => ({ label: v, value: v }));

const GENDER_OPTIONS = [
  { label: "All", value: "All" },
  { label: "Male", value: "Male" },
  { label: "Female", value: "Female" },
].map((v) => ({ label: v.label, value: v.value }));

const RECRUITMENT_STATUS_OPTIONS = [
  "Recruiting",
  "Not Yet Recruiting",
  "Enrolling by Invitation",
  "Active, Not Recruiting",
  "Completed",
  "Suspended",
  "Terminated",
  "Withdrawn",
].map((v) => ({ label: v, value: v }));

function reviewRow(label: string, value: unknown): React.ReactNode {
  if (value === undefined || value === null || value === "") return null;
  let display: string;
  if (typeof value === "string" || typeof value === "number" || typeof value === "boolean") {
    display = String(value);
  } else if (typeof value === "object" && "format" in value) {
    display = (value as { format: (f: string) => string }).format("YYYY-MM-DD");
  } else {
    display = JSON.stringify(value);
  }
  return (
    <Descriptions.Item key={label} label={label}>
      <Text>{display}</Text>
    </Descriptions.Item>
  );
}

function buildReviewSections(values: StudyFormValues) {
  return [
    {
      title: "协议信息",
      items: [
        reviewRow("研究名称", values.name),
        reviewRow("唯一标识", values.uniqueIdentifier),
        reviewRow("正式标题", values.officialTitle),
        reviewRow("阶段", values.phase),
        reviewRow("摘要", values.summary),
      ],
    },
    {
      title: "赞助信息",
      items: [
        reviewRow("赞助方", values.sponsor),
        reviewRow("合作方", values.collaborators),
        reviewRow("方案类型", values.protocolType),
        reviewRow("方案描述", values.protocolDescription),
      ],
    },
    {
      title: "研究设计",
      items: [
        reviewRow("主要目的", values.purpose),
        reviewRow("分配方式", values.allocation),
        reviewRow("盲法", values.masking),
        reviewRow("对照", values.control),
        reviewRow("分组方式", values.assignment),
        reviewRow("主要终点", values.endpoint),
        reviewRow("干预措施", values.interventions),
        reviewRow("研究周期", values.duration),
        reviewRow("抽样方法", values.selection),
        reviewRow("时序", values.timing),
        reviewRow("性别", values.gender),
      ],
    },
    {
      title: "入组标准",
      items: [
        reviewRow("入排标准", values.eligibility),
        reviewRow("疾病条件", values.conditions),
        reviewRow("关键词", values.keywords),
      ],
    },
    {
      title: "招募计划",
      items: [
        reviewRow("预计招募人数", values.expectedTotalEnrollment),
        reviewRow("计划开始日期", values.datePlannedStart),
        reviewRow("计划结束日期", values.datePlannedEnd),
      ],
    },
    {
      title: "研究机构",
      items: [
        reviewRow("机构名称", values.facilityName),
        reviewRow("城市", values.facilityCity),
        reviewRow("州/省", values.facilityState),
        reviewRow("国家", values.facilityCountry),
        reviewRow("招募状态", values.facilityRecruitmentStatus),
      ],
    },
    {
      title: "联系方式",
      items: [
        reviewRow("联系人姓名", values.facilityContactName),
        reviewRow("学位/职称", values.facilityContactDegree),
        reviewRow("电话", values.facilityContactPhone),
      ],
    },
  ];
}

function StepProtocolInfo() {
  return (
    <>
      <Form.Item
        name="name"
        label="研究名称"
        rules={[{ required: true, message: "请输入研究名称" }]}
      >
        <Input placeholder="例如：ASPIRE-2024" />
      </Form.Item>
      <Form.Item name="uniqueIdentifier" label="唯一标识">
        <Input placeholder="例如：NCT12345678" />
      </Form.Item>
      <Form.Item name="officialTitle" label="正式标题">
        <Input placeholder="研究完整正式标题" />
      </Form.Item>
      <Form.Item name="phase" label="阶段">
        <Select allowClear placeholder="选择阶段" options={PHASE_OPTIONS} />
      </Form.Item>
      <Form.Item name="summary" label="摘要">
        <TextArea rows={4} placeholder="研究简要描述" />
      </Form.Item>
    </>
  );
}

function StepSponsorship() {
  return (
    <>
      <Form.Item name="sponsor" label="赞助方">
        <Input placeholder="例如：国家卫生研究院" />
      </Form.Item>
      <Form.Item name="collaborators" label="合作方">
        <TextArea rows={3} placeholder="列出合作机构" />
      </Form.Item>
      <Form.Item name="protocolType" label="方案类型">
        <Select allowClear placeholder="选择类型" options={PROTOCOL_TYPE_OPTIONS} />
      </Form.Item>
      <Form.Item name="protocolDescription" label="方案描述">
        <TextArea rows={4} placeholder="详细方案描述" />
      </Form.Item>
    </>
  );
}

function StepStudyDesign() {
  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0 24px" }}>
      <Form.Item name="purpose" label="主要目的">
        <Select allowClear placeholder="选择目的" options={PURPOSE_OPTIONS} />
      </Form.Item>
      <Form.Item name="allocation" label="分配方式">
        <Select allowClear placeholder="选择分配方式" options={ALLOCATION_OPTIONS} />
      </Form.Item>
      <Form.Item name="masking" label="盲法">
        <Select allowClear placeholder="选择盲法" options={MASKING_OPTIONS} />
      </Form.Item>
      <Form.Item name="control" label="对照">
        <Select allowClear placeholder="选择对照类型" options={CONTROL_OPTIONS} />
      </Form.Item>
      <Form.Item name="assignment" label="分组方式">
        <Select allowClear placeholder="选择分组方式" options={ASSIGNMENT_OPTIONS} />
      </Form.Item>
      <Form.Item name="gender" label="性别">
        <Select allowClear placeholder="选择性别" options={GENDER_OPTIONS} />
      </Form.Item>
      <Form.Item name="selection" label="抽样方法" style={{ gridColumn: "span 1" }}>
        <Select allowClear placeholder="选择抽样方法" options={SELECTION_OPTIONS} />
      </Form.Item>
      <Form.Item name="timing" label="时序">
        <Select allowClear placeholder="选择时序" options={TIMING_OPTIONS} />
      </Form.Item>
      <Form.Item name="duration" label="研究周期">
        <Input placeholder="例如：12 个月" />
      </Form.Item>
      <Form.Item name="endpoint" label="主要终点" style={{ gridColumn: "span 1" }}>
        <Input placeholder="主要终点描述" />
      </Form.Item>
      <Form.Item name="interventions" label="干预措施" style={{ gridColumn: "span 2" }}>
        <TextArea rows={3} placeholder="描述研究干预措施" />
      </Form.Item>
    </div>
  );
}

function StepEligibility() {
  return (
    <>
      <Form.Item name="eligibility" label="入排标准">
        <TextArea rows={6} placeholder="描述纳入/排除标准" />
      </Form.Item>
      <Form.Item name="conditions" label="疾病条件">
        <TextArea rows={3} placeholder="列出研究疾病条件" />
      </Form.Item>
      <Form.Item name="keywords" label="关键词">
        <Input placeholder="逗号分隔的关键词" />
      </Form.Item>
    </>
  );
}

function StepEnrollment() {
  return (
    <>
      <Form.Item name="expectedTotalEnrollment" label="预计招募人数">
        <InputNumber
          min={1}
          style={{ width: "100%" }}
          placeholder="参与人数"
        />
      </Form.Item>
      <Form.Item name="datePlannedStart" label="计划开始日期">
        <DatePicker style={{ width: "100%" }} />
      </Form.Item>
      <Form.Item name="datePlannedEnd" label="计划结束日期">
        <DatePicker style={{ width: "100%" }} />
      </Form.Item>
    </>
  );
}

function StepFacility() {
  return (
    <>
      <Form.Item name="facilityName" label="机构名称">
        <Input placeholder="例如：北京协和医院" />
      </Form.Item>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0 24px" }}>
        <Form.Item name="facilityCity" label="城市">
          <Input placeholder="城市" />
        </Form.Item>
        <Form.Item name="facilityState" label="州/省">
          <Input placeholder="州/省" />
        </Form.Item>
      </div>
      <Form.Item name="facilityCountry" label="国家">
        <Input placeholder="国家" />
      </Form.Item>
      <Form.Item name="facilityRecruitmentStatus" label="招募状态">
        <Select
          allowClear
          placeholder="选择状态"
          options={RECRUITMENT_STATUS_OPTIONS}
        />
      </Form.Item>
    </>
  );
}

function StepContact() {
  return (
    <>
      <Form.Item name="facilityContactName" label="联系人姓名">
        <Input placeholder="机构联系人全名" />
      </Form.Item>
      <Form.Item name="facilityContactDegree" label="学位/职称">
        <Input placeholder="例如：医学博士" />
      </Form.Item>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0 24px" }}>
        <Form.Item name="facilityContactPhone" label="电话">
          <Input placeholder="+86 10-1234-5678" />
        </Form.Item>
      </div>
    </>
  );
}

function StepReview({ values }: { values: StudyFormValues }) {
  const sections = buildReviewSections(values);

  return (
    <div>
      <Text type="secondary" style={{ display: "block", marginBottom: 20 }}>
        请确认所有信息无误后提交。点击"上一步"返回修改。
      </Text>
      {sections.map((section) => {
        const visibleItems = section.items.filter(Boolean);
        if (visibleItems.length === 0) return null;
        return (
          <div key={section.title} style={{ marginBottom: 20 }}>
            <Text
              strong
              style={{
                fontSize: 16,
                fontWeight: 600,
                color: "var(--text)",
                display: "block",
                marginBottom: 8,
                paddingBottom: 6,
                borderBottom: "1px solid var(--border)",
              }}
            >
              {section.title}
            </Text>
            <Descriptions column={2} size="small" bordered>
              {visibleItems}
            </Descriptions>
          </div>
        );
      })}
    </div>
  );
}

function StepContent({ current }: { current: number }) {
  switch (current) {
    case 0: return <StepProtocolInfo />;
    case 1: return <StepSponsorship />;
    case 2: return <StepStudyDesign />;
    case 3: return <StepEligibility />;
    case 4: return <StepEnrollment />;
    case 5: return <StepFacility />;
    case 6: return <StepContact />;
    default: return null;
  }
}

export default function StudyWizard() {
  const navigate = useNavigate();
  const [form] = Form.useForm<StudyFormValues>();
  const [current, setCurrent] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [createdId, setCreatedId] = useState<number | null>(null);

  const watchedValues = Form.useWatch([], form) as StudyFormValues | undefined;
  const formValues = watchedValues ?? {};

  const isReview = current === STEPS.length - 1;
  const isFirst = current === 0;

  const validateCurrentStep = useCallback(async (): Promise<boolean> => {
    const step = STEPS[current];
    if (!step || step.fields.length === 0) return true;
    try {
      await form.validateFields(step.fields as string[]);
      return true;
    } catch {
      return false;
    }
  }, [current, form]);

  const goNext = useCallback(async () => {
    const valid = await validateCurrentStep();
    if (!valid) return;
    setCurrent((c) => Math.min(c + 1, STEPS.length - 1));
  }, [validateCurrentStep]);

  const goPrev = useCallback(() => {
    setCurrent((c) => Math.max(c - 1, 0));
  }, []);

  const handleSubmit = useCallback(async () => {
    setSubmitting(true);
    try {
      const vals = form.getFieldsValue(true);

      const payload: Record<string, unknown> = {
        name: vals.name,
        uniqueIdentifier: vals.uniqueIdentifier,
        officialTitle: vals.officialTitle,
        phase: vals.phase,
        summary: vals.summary,
        sponsor: vals.sponsor,
        collaborators: vals.collaborators,
        protocolType: vals.protocolType,
        protocolDescription: vals.protocolDescription,
        purpose: vals.purpose,
        allocation: vals.allocation,
        masking: vals.masking,
        control: vals.control,
        assignment: vals.assignment,
        endpoint: vals.endpoint,
        interventions: vals.interventions,
        duration: vals.duration,
        selection: vals.selection,
        timing: vals.timing,
        gender: vals.gender,
        eligibility: vals.eligibility,
        conditions: vals.conditions,
        keywords: vals.keywords,
        expectedTotalEnrollment: vals.expectedTotalEnrollment ?? undefined,
        datePlannedStart: vals.datePlannedStart?.toISOString() ?? undefined,
        datePlannedEnd: vals.datePlannedEnd?.toISOString() ?? undefined,
        facilityName: vals.facilityName,
        facilityCity: vals.facilityCity,
        facilityState: vals.facilityState,
        facilityCountry: vals.facilityCountry,
        facilityRecruitmentStatus: vals.facilityRecruitmentStatus,
        facilityContactName: vals.facilityContactName,
        facilityContactDegree: vals.facilityContactDegree,
        facilityContactPhone: vals.facilityContactPhone,
        typeId: 1,
        statusId: 1,
      };

      const clean = Object.fromEntries(
        Object.entries(payload).filter(([, v]) => v !== undefined && v !== ""),
      );

      const res = await fetch("/api/v1/studies", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(clean),
      });

      if (!res.ok) {
        const errText = await res.text().catch(() => "创建研究失败");
        message.error(errText || "创建研究失败");
        return;
      }

      const data = (await res.json()) as { id?: number; studyId?: number };
      const id = data.id ?? data.studyId;
      setCreatedId(id ?? null);
      setSubmitted(true);
      message.success("研究创建成功");
    } catch {
      message.error("发生意外错误");
    } finally {
      setSubmitting(false);
    }
  }, [form]);

  if (submitted) {
    return (
      <div style={{ maxWidth: 680, margin: "0 auto" }}>
        <Card
          style={{ borderRadius: "var(--radius-lg)", textAlign: "center" }}
        >
          <Result
            status="success"
            title="研究已创建"
            subTitle={
              <Text type="secondary">
                研究已成功创建。
                {createdId && (
                  <>即将跳转至研究详情页面。</>
                )}
              </Text>
            }
            extra={
              <Space>
                <Button onClick={() => navigate("/app/studies")}>
                  返回研究列表
                </Button>
                {createdId && (
                  <Button
                    type="primary"
                    onClick={() => navigate(`/app/studies/${createdId}`)}
                  >
                    查看研究
                  </Button>
                )}
              </Space>
            }
          />
        </Card>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 860, margin: "0 auto" }}>
      <div style={{ marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>新建研究</Title>
        <Text type="secondary" style={{ marginTop: 4, display: "block" }}>
          按步骤完成研究方案设置。
        </Text>
      </div>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "220px 1fr",
          gap: 24,
          alignItems: "start",
        }}
      >
        <Card
          style={{ borderRadius: "var(--radius-lg)" }}
          styles={{ body: { padding: "16px 12px" } }}
        >
          <Steps
            current={current}
            direction="vertical"
            size="small"
            items={STEPS.map((s) => ({
              title: s.title,
            }))}
          />
        </Card>

        <Card
          style={{ borderRadius: "var(--radius-lg)" }}
          styles={{ body: { padding: "28px 32px 20px" } }}
        >
          <div style={{ marginBottom: 24 }}>
            <Space align="center" size={10}>
              <span
                style={{
                  display: "inline-flex",
                  alignItems: "center",
                  justifyContent: "center",
                  width: 32,
                  height: 32,
                  borderRadius: "50%",
                  background: "var(--accent)",
                  color: "var(--accent-text)",
                  fontSize: 14,
                  fontWeight: 600,
                }}
              >
                {current + 1}
              </span>
              <Text
                strong
                style={{
                  fontSize: 18,
                  color: "var(--text)",
                }}
              >
                {STEPS[current]?.title}
              </Text>
            </Space>
            <div
              style={{
                height: 2,
                background: "var(--border)",
                borderRadius: 1,
                marginTop: 16,
                position: "relative",
              }}
            >
              <div
                style={{
                  height: "100%",
                  width: `${((current + 1) / STEPS.length) * 100}%`,
                  background: "var(--accent)",
                  borderRadius: 1,
                }}
              />
            </div>
          </div>

          <Form
            form={form}
            layout="vertical"
            requiredMark={false}
            style={{ marginBottom: 0 }}
          >
            {isReview ? (
              <StepReview values={formValues} />
            ) : (
              <StepContent current={current} />
            )}
          </Form>

          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              marginTop: 28,
              paddingTop: 20,
              borderTop: "1px solid var(--border)",
            }}
          >
            <Button
              onClick={goPrev}
              disabled={isFirst}
            >
              上一步
            </Button>

            {isReview ? (
              <Button
                type="primary"
                loading={submitting}
                onClick={handleSubmit}
              >
                创建研究
              </Button>
            ) : (
              <Button
                type="primary"
                onClick={goNext}
              >
                下一步
              </Button>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
