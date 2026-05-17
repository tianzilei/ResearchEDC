import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Model } from "survey-core";
import { Survey } from "survey-react-ui";
import { Spin, Alert, Typography, Button, Space, message } from "antd";
import { CheckCircleOutlined, ClockCircleOutlined } from "@ant-design/icons";
import { apiClient } from "@/api/client";
import "survey-core/survey-core.min.css";

const { Title, Text } = Typography;

interface QuestionnaireInfo {
  assignment_id: string;
  questionnaire_code: string;
  questionnaire_name: string;
  version_no: string;
  surveyjs_schema: Record<string, unknown>;
  status: string;
  due_at: string | null;
}

export default function QuestionnaireFill() {
  const { token } = useParams<{ token: string }>();
  const [info, setInfo] = useState<QuestionnaireInfo | null>(null);
  const [survey, setSurvey] = useState<Model | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [startedAt] = useState(() => new Date().toISOString());

  const questionnaireBaseUrl = import.meta.env.VITE_QUESTIONNAIRE_API_URL ?? "/api/v1";

  useEffect(() => {
    if (!token) {
      setError("Invalid questionnaire link");
      setLoading(false);
      return;
    }
    apiClient
      .get<QuestionnaireInfo>(
        `${questionnaireBaseUrl}/public/questionnaires/${token}`,
      )
      .then((data: QuestionnaireInfo) => {
        setInfo(data);
        const model = new Model(data.surveyjs_schema);
        setSurvey(model);
        setLoading(false);
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : "Failed to load questionnaire");
        setLoading(false);
      });
  }, [token, questionnaireBaseUrl]);

  const handleSubmit = async () => {
    if (!survey || !info || !token) return;
    setSubmitting(true);
    try {
      const responseData = survey.data as Record<string, unknown>;
      await apiClient.post(
        `${questionnaireBaseUrl}/public/questionnaires/${token}/submit`,
        {
          started_at: startedAt,
          submitted_at: new Date().toISOString(),
          response: responseData,
        },
      );
      setSubmitted(true);
      message.success("Questionnaire submitted successfully");
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : "Failed to submit";
      message.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  const handleSaveDraft = async () => {
    if (!survey || !info || !token) return;
    try {
      const responseData = survey.data as Record<string, unknown>;
      await apiClient.post(
        `${questionnaireBaseUrl}/public/questionnaires/${token}/draft`,
        {
          started_at: startedAt,
          response: responseData,
        },
      );
      message.success("Draft saved");
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : "Failed to save draft";
      message.error(errorMessage);
    }
  };

  if (loading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "100vh" }}>
        <Spin size="large" tip="Loading questionnaire..." />
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ maxWidth: 600, margin: "80px auto", padding: 24 }}>
        <Alert
          message="Unable to Load Questionnaire"
          description={error}
          type="error"
          showIcon
        />
      </div>
    );
  }

  if (submitted) {
    return (
      <div style={{ maxWidth: 600, margin: "80px auto", padding: 24, textAlign: "center" }}>
        <CheckCircleOutlined style={{ fontSize: 64, color: "#52c41a" }} />
        <Title level={3} style={{ marginTop: 16 }}>
          Questionnaire Submitted
        </Title>
        <Text type="secondary">
          Thank you for completing the {info?.questionnaire_name}. Your responses
          have been recorded.
        </Text>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 800, margin: "40px auto", padding: "0 16px" }}>
      <div style={{ marginBottom: 24 }}>
        <Title level={3} style={{ marginBottom: 4 }}>
          {info?.questionnaire_name}
        </Title>
        <Space>
          <Text type="secondary">Version: {info?.version_no}</Text>
          {info?.due_at && (
            <Text type="warning">
              <ClockCircleOutlined /> Due: {new Date(info.due_at).toLocaleDateString()}
            </Text>
          )}
        </Space>
      </div>

      {survey && (
        <div
          style={{
            background: "#fff",
            borderRadius: 8,
            padding: 24,
            boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
          }}
        >
          <Survey model={survey} />
        </div>
      )}

      <div style={{ marginTop: 24, textAlign: "center" }}>
        <Space size="middle">
          <Button size="large" onClick={handleSaveDraft} disabled={submitting}>
            Save Draft
          </Button>
          <Button
            type="primary"
            size="large"
            onClick={handleSubmit}
            loading={submitting}
            disabled={!survey}
          >
            Submit
          </Button>
        </Space>
      </div>
    </div>
  );
}
