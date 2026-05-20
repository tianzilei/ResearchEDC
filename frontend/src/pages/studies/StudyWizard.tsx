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
  theme as antTheme,
} from "antd";
import type { Dayjs } from "dayjs";
import {
  ArrowLeftOutlined,
  ArrowRightOutlined,
  SendOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  BankOutlined,
  ExperimentOutlined,
  TeamOutlined,
  CalendarOutlined,
  HomeOutlined,
  PhoneOutlined,
} from "@ant-design/icons";
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
  facilityContactEmail?: string;
}

interface StepDef {
  title: string;
  icon: React.ReactNode;
  fields: (keyof StudyFormValues)[];
}

const STEPS: StepDef[] = [
  {
    title: "Protocol Info",
    icon: <FileTextOutlined />,
    fields: ["name", "uniqueIdentifier", "officialTitle", "phase", "summary"],
  },
  {
    title: "Sponsorship",
    icon: <BankOutlined />,
    fields: ["sponsor", "collaborators", "protocolType", "protocolDescription"],
  },
  {
    title: "Study Design",
    icon: <ExperimentOutlined />,
    fields: [
      "purpose", "allocation", "masking", "control", "assignment",
      "endpoint", "interventions", "duration", "selection", "timing", "gender",
    ],
  },
  {
    title: "Eligibility",
    icon: <TeamOutlined />,
    fields: ["eligibility", "conditions", "keywords"],
  },
  {
    title: "Enrollment",
    icon: <CalendarOutlined />,
    fields: ["expectedTotalEnrollment", "datePlannedStart", "datePlannedEnd"],
  },
  {
    title: "Facility",
    icon: <HomeOutlined />,
    fields: [
      "facilityName", "facilityCity", "facilityState",
      "facilityCountry", "facilityRecruitmentStatus",
    ],
  },
  {
    title: "Contact",
    icon: <PhoneOutlined />,
    fields: [
      "facilityContactName", "facilityContactDegree",
      "facilityContactPhone", "facilityContactEmail",
    ],
  },
  {
    title: "Review & Confirm",
    icon: <CheckCircleOutlined />,
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
      title: "Protocol Info",
      items: [
        reviewRow("Study Name", values.name),
        reviewRow("Unique Identifier", values.uniqueIdentifier),
        reviewRow("Official Title", values.officialTitle),
        reviewRow("Phase", values.phase),
        reviewRow("Summary", values.summary),
      ],
    },
    {
      title: "Sponsorship",
      items: [
        reviewRow("Sponsor", values.sponsor),
        reviewRow("Collaborators", values.collaborators),
        reviewRow("Protocol Type", values.protocolType),
        reviewRow("Protocol Description", values.protocolDescription),
      ],
    },
    {
      title: "Study Design",
      items: [
        reviewRow("Purpose", values.purpose),
        reviewRow("Allocation", values.allocation),
        reviewRow("Masking", values.masking),
        reviewRow("Control", values.control),
        reviewRow("Assignment", values.assignment),
        reviewRow("Primary Endpoint", values.endpoint),
        reviewRow("Interventions", values.interventions),
        reviewRow("Duration", values.duration),
        reviewRow("Selection", values.selection),
        reviewRow("Timing", values.timing),
        reviewRow("Gender", values.gender),
      ],
    },
    {
      title: "Eligibility",
      items: [
        reviewRow("Eligibility Criteria", values.eligibility),
        reviewRow("Conditions", values.conditions),
        reviewRow("Keywords", values.keywords),
      ],
    },
    {
      title: "Enrollment",
      items: [
        reviewRow("Expected Total Enrollment", values.expectedTotalEnrollment),
        reviewRow("Planned Start Date", values.datePlannedStart),
        reviewRow("Planned End Date", values.datePlannedEnd),
      ],
    },
    {
      title: "Facility",
      items: [
        reviewRow("Facility Name", values.facilityName),
        reviewRow("City", values.facilityCity),
        reviewRow("State", values.facilityState),
        reviewRow("Country", values.facilityCountry),
        reviewRow("Recruitment Status", values.facilityRecruitmentStatus),
      ],
    },
    {
      title: "Contact",
      items: [
        reviewRow("Contact Name", values.facilityContactName),
        reviewRow("Degree / Title", values.facilityContactDegree),
        reviewRow("Phone", values.facilityContactPhone),
        reviewRow("Email", values.facilityContactEmail),
      ],
    },
  ];
}

function StepProtocolInfo() {
  return (
    <>
      <Form.Item
        name="name"
        label="Study Name"
        rules={[{ required: true, message: "Please enter the study name" }]}
      >
        <Input placeholder="e.g. ASPIRE-2024" />
      </Form.Item>
      <Form.Item name="uniqueIdentifier" label="Unique Identifier">
        <Input placeholder="e.g. NCT12345678" />
      </Form.Item>
      <Form.Item name="officialTitle" label="Official Title">
        <Input placeholder="Full official title of the study" />
      </Form.Item>
      <Form.Item name="phase" label="Phase">
        <Select allowClear placeholder="Select phase" options={PHASE_OPTIONS} />
      </Form.Item>
      <Form.Item name="summary" label="Summary">
        <TextArea rows={4} placeholder="Brief description of the study" />
      </Form.Item>
    </>
  );
}

function StepSponsorship() {
  return (
    <>
      <Form.Item name="sponsor" label="Sponsor">
        <Input placeholder="e.g. National Institutes of Health" />
      </Form.Item>
      <Form.Item name="collaborators" label="Collaborators">
        <TextArea rows={3} placeholder="List collaborating organizations" />
      </Form.Item>
      <Form.Item name="protocolType" label="Protocol Type">
        <Select allowClear placeholder="Select type" options={PROTOCOL_TYPE_OPTIONS} />
      </Form.Item>
      <Form.Item name="protocolDescription" label="Protocol Description">
        <TextArea rows={4} placeholder="Detailed protocol description" />
      </Form.Item>
    </>
  );
}

function StepStudyDesign() {
  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0 24px" }}>
      <Form.Item name="purpose" label="Primary Purpose">
        <Select allowClear placeholder="Select purpose" options={PURPOSE_OPTIONS} />
      </Form.Item>
      <Form.Item name="allocation" label="Allocation">
        <Select allowClear placeholder="Select allocation" options={ALLOCATION_OPTIONS} />
      </Form.Item>
      <Form.Item name="masking" label="Masking">
        <Select allowClear placeholder="Select masking" options={MASKING_OPTIONS} />
      </Form.Item>
      <Form.Item name="control" label="Control">
        <Select allowClear placeholder="Select control type" options={CONTROL_OPTIONS} />
      </Form.Item>
      <Form.Item name="assignment" label="Assignment">
        <Select allowClear placeholder="Select assignment" options={ASSIGNMENT_OPTIONS} />
      </Form.Item>
      <Form.Item name="gender" label="Gender">
        <Select allowClear placeholder="Select gender eligibility" options={GENDER_OPTIONS} />
      </Form.Item>
      <Form.Item name="selection" label="Selection" style={{ gridColumn: "span 1" }}>
        <Select allowClear placeholder="Select sampling method" options={SELECTION_OPTIONS} />
      </Form.Item>
      <Form.Item name="timing" label="Timing">
        <Select allowClear placeholder="Select timing" options={TIMING_OPTIONS} />
      </Form.Item>
      <Form.Item name="duration" label="Study Duration">
        <Input placeholder="e.g. 12 months" />
      </Form.Item>
      <Form.Item name="endpoint" label="Primary Endpoint" style={{ gridColumn: "span 1" }}>
        <Input placeholder="Primary endpoint description" />
      </Form.Item>
      <Form.Item name="interventions" label="Interventions" style={{ gridColumn: "span 2" }}>
        <TextArea rows={3} placeholder="Describe study interventions" />
      </Form.Item>
    </div>
  );
}

function StepEligibility() {
  return (
    <>
      <Form.Item name="eligibility" label="Eligibility Criteria">
        <TextArea rows={6} placeholder="Describe inclusion/exclusion criteria" />
      </Form.Item>
      <Form.Item name="conditions" label="Conditions">
        <TextArea rows={3} placeholder="List study conditions" />
      </Form.Item>
      <Form.Item name="keywords" label="Keywords">
        <Input placeholder="Comma-separated keywords" />
      </Form.Item>
    </>
  );
}

function StepEnrollment() {
  return (
    <>
      <Form.Item name="expectedTotalEnrollment" label="Expected Total Enrollment">
        <InputNumber
          min={1}
          style={{ width: "100%" }}
          placeholder="Number of participants"
        />
      </Form.Item>
      <Form.Item name="datePlannedStart" label="Planned Start Date">
        <DatePicker style={{ width: "100%" }} />
      </Form.Item>
      <Form.Item name="datePlannedEnd" label="Planned End Date">
        <DatePicker style={{ width: "100%" }} />
      </Form.Item>
    </>
  );
}

function StepFacility() {
  return (
    <>
      <Form.Item name="facilityName" label="Facility Name">
        <Input placeholder="e.g. Boston Medical Center" />
      </Form.Item>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0 24px" }}>
        <Form.Item name="facilityCity" label="City">
          <Input placeholder="City" />
        </Form.Item>
        <Form.Item name="facilityState" label="State / Province">
          <Input placeholder="State" />
        </Form.Item>
      </div>
      <Form.Item name="facilityCountry" label="Country">
        <Input placeholder="Country" />
      </Form.Item>
      <Form.Item name="facilityRecruitmentStatus" label="Recruitment Status">
        <Select
          allowClear
          placeholder="Select status"
          options={RECRUITMENT_STATUS_OPTIONS}
        />
      </Form.Item>
    </>
  );
}

function StepContact() {
  return (
    <>
      <Form.Item name="facilityContactName" label="Contact Name">
        <Input placeholder="Full name of facility contact" />
      </Form.Item>
      <Form.Item name="facilityContactDegree" label="Degree / Title">
        <Input placeholder="e.g. MD, PhD" />
      </Form.Item>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0 24px" }}>
        <Form.Item name="facilityContactPhone" label="Phone">
          <Input placeholder="+1 (555) 123-4567" />
        </Form.Item>
        <Form.Item name="facilityContactEmail" label="Email">
          <Input placeholder="contact@institution.edu" />
        </Form.Item>
      </div>
    </>
  );
}

function StepReview({ values }: { values: StudyFormValues }) {
  const sections = buildReviewSections(values);
  const { token } = antTheme.useToken();

  return (
    <div>
      <Text type="secondary" style={{ display: "block", marginBottom: 20 }}>
        Please review all entered information before submitting. Click &ldquo;Back&rdquo; to make changes.
      </Text>
      {sections.map((section) => {
        const visibleItems = section.items.filter(Boolean);
        if (visibleItems.length === 0) return null;
        return (
          <div key={section.title} style={{ marginBottom: 20 }}>
            <Text
              strong
              style={{
                fontFamily: "var(--font-heading)",
                fontSize: 14,
                color: token.colorText,
                display: "block",
                marginBottom: 8,
                paddingBottom: 6,
                borderBottom: `1px solid ${token.colorBorderSecondary}`,
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
  const { token } = antTheme.useToken();

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
        facilityContactEmail: vals.facilityContactEmail,
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
        const errText = await res.text().catch(() => "Failed to create study");
        message.error(errText || "Failed to create study");
        return;
      }

      const data = (await res.json()) as { id?: number; studyId?: number };
      const id = data.id ?? data.studyId;
      setCreatedId(id ?? null);
      setSubmitted(true);
      message.success("Study created successfully");
    } catch {
      message.error("An unexpected error occurred");
    } finally {
      setSubmitting(false);
    }
  }, [form]);

  if (submitted) {
    return (
      <div style={{ maxWidth: 680, margin: "0 auto" }}>
        <Card
          className="glass-panel"
          style={{ borderRadius: "var(--radius-lg)", textAlign: "center" }}
        >
          <Result
            status="success"
            title="Study Created"
            subTitle={
              <Text type="secondary">
                Your study has been created successfully.
                {createdId && (
                  <> You will be redirected to the study details page.</>
                )}
              </Text>
            }
            extra={
              <Space>
                <Button onClick={() => navigate("/app/studies")}>
                  Back to Studies
                </Button>
                {createdId && (
                  <Button
                    type="primary"
                    onClick={() => navigate(`/app/studies/${createdId}`)}
                  >
                    View Study
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
        <Title level={3} style={{ margin: 0 }}>Create New Study</Title>
        <Text type="secondary" style={{ marginTop: 4, display: "block" }}>
          Complete each step to set up your study protocol.
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
          className="glass-panel"
          style={{ borderRadius: "var(--radius-lg)" }}
          styles={{ body: { padding: "16px 12px" } }}
        >
          <Steps
            current={current}
            direction="vertical"
            size="small"
            items={STEPS.map((s) => ({
              title: s.title,
              icon: s.icon,
            }))}
          />
        </Card>

        <Card
          className="glass-panel"
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
                  background: token.colorPrimary,
                  color: "#fff",
                  fontSize: 14,
                  fontWeight: 600,
                  fontFamily: "var(--font-heading)",
                }}
              >
                {current + 1}
              </span>
              <Text
                strong
                style={{
                  fontFamily: "var(--font-heading)",
                  fontSize: 17,
                  color: token.colorText,
                }}
              >
                {STEPS[current]?.title}
              </Text>
            </Space>
            <div
              style={{
                height: 2,
                background: token.colorBorderSecondary,
                borderRadius: 1,
                marginTop: 16,
                position: "relative",
              }}
            >
              <div
                style={{
                  height: "100%",
                  width: `${((current + 1) / STEPS.length) * 100}%`,
                  background: token.colorPrimary,
                  borderRadius: 1,
                  transition: "width 0.35s cubic-bezier(0.22, 1, 0.36, 1)",
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
              borderTop: `1px solid ${token.colorBorderSecondary}`,
            }}
          >
            <Button
              icon={<ArrowLeftOutlined />}
              onClick={goPrev}
              disabled={isFirst}
            >
              Previous
            </Button>

            {isReview ? (
              <Button
                type="primary"
                icon={<SendOutlined />}
                loading={submitting}
                onClick={handleSubmit}
              >
                Create Study
              </Button>
            ) : (
              <Button
                type="primary"
                icon={<ArrowRightOutlined />}
                onClick={goNext}
              >
                Next
              </Button>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
