import { useParams, Link } from "react-router-dom";
import { Card, Typography, Breadcrumb, Button, Space, Result } from "antd";
import { BookOutlined, ArrowLeftOutlined } from "@ant-design/icons";

const { Title, Paragraph } = Typography;

const INSTRUCTIONS: Record<string, { title: string; content: string; back: string }> = {
  "enter-data": {
    title: "Data Entry Instructions",
    content: `Complete each field according to the CRF guidelines. Required fields are marked with an asterisk (*). 
      Use the Tab key to navigate between fields. Save your work frequently using the Save button or enable Auto-Save.
      For select and radio fields, choose the appropriate option from the provided list. For date fields, use the date picker or enter in YYYY-MM-DD format.
      After completing all required fields, you may mark the event as complete. Once completed, the data enters a review workflow.`,
    back: "/app/subjects",
  },
  "enroll-subject": {
    title: "Subject Enrollment Instructions",
    content: `To enroll a new subject, first create the subject record with their unique identifier and demographic information. 
      Then assign them to a study by selecting the study and specifying their enrollment details. 
      Subjects can be assigned to specific groups within a study for randomization or cohort tracking.
      After enrollment, you can schedule events and begin CRF data entry for the subject.`,
    back: "/app/subjects",
  },
  "set-up-event": {
    title: "Study Event Setup Instructions",
    content: `Study events represent scheduled data collection points in a clinical trial. 
      Events are defined at the study level with specific CRF assignments. 
      To set up an event: 1) Define the event type (e.g., Screening, Baseline, Follow-up), 
      2) Assign the relevant CRF versions, 3) Schedule the event for specific subjects.
      Events can be single or repeating, and each event tracks its own completion status.`,
    back: "/app/studies",
  },
  "admin-edit": {
    title: "Administrative Editing Instructions",
    content: `Administrative editing allows data managers to make corrections to submitted data. 
      All changes are tracked in the audit log. When editing: 1) Select the subject and event, 
      2) Locate the CRF requiring changes, 3) Make corrections with an explanation for the change.
      Administrative edits require a reason and are permanently recorded in the audit trail.`,
    back: "/app/admin",
  },
};

export default function Instructions() {
  const { topic } = useParams<{ topic: string }>();
  const instruction = topic ? INSTRUCTIONS[topic] : null;

  if (!instruction) {
    return (
      <div>
        <Breadcrumb items={[{ title: <Link to="/app">Home</Link> }, { title: "Instructions" }]}
          style={{ marginBottom: 16 }} />
        <Result
          status="info"
          title="Instructions"
          subTitle="Select a topic below"
          extra={
            <Space direction="vertical">
              {Object.entries(INSTRUCTIONS).map(([key, val]) => (
                <Link key={key} to={`/app/instructions/${key}`}>
                  <Button type="link" icon={<BookOutlined />}>{val.title}</Button>
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
          { title: <Link to="/app">Home</Link> },
          { title: <Link to="/app/instructions">Instructions</Link> },
          { title: instruction.title },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card style={{ borderRadius: 14 }}>
        <Space style={{ marginBottom: 16 }}>
          <BookOutlined style={{ fontSize: 24, color: "var(--color-primary, #099A87)" }} />
          <Title level={4} style={{ margin: 0 }}>{instruction.title}</Title>
        </Space>
        <Paragraph style={{ fontSize: 15, lineHeight: 1.8, whiteSpace: "pre-line" }}>
          {instruction.content}
        </Paragraph>
        <Button icon={<ArrowLeftOutlined />} onClick={() => window.history.back()}>
          Back
        </Button>
      </Card>
    </div>
  );
}
