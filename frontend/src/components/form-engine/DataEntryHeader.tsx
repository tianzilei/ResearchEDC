import { Breadcrumb, Card, Button, Space, Typography } from "antd";
import { Link } from "react-router-dom";
import type { FormRecordStatus } from "@/components/form-engine/FormStatus";
import { statusClassName } from "@/utils/crfStatus";
import { SaveIndicator } from "@/components/form-engine/SaveIndicator";
import { useTranslation } from "react-i18next";

const { Title, Text } = Typography;

type SaveStatus = "idle" | "saving" | "saved" | "error";

interface DataEntryHeaderProps {
  crfName: string;
  sectionsCount: number;
  recordStatus: FormRecordStatus;
  saveStatus: SaveStatus;
  canComplete: boolean;
  isCompleting: boolean;
  parsedEventCrfId: number | undefined;
  parsedSubjectId: string | undefined;
  onComplete: () => void;
  onBack: () => void;
}

export function DataEntryHeader({
  crfName,
  sectionsCount,
  recordStatus,
  saveStatus,
  canComplete,
  isCompleting,
  parsedEventCrfId,
  parsedSubjectId,
  onComplete,
  onBack,
}: DataEntryHeaderProps) {
  const { t } = useTranslation();
  return (
    <div>
      <Breadcrumb
        items={[
          { title: <Link to="/app/subjects">Subjects</Link> },
          { title: <Link to={`/app/subjects/${parsedSubjectId}`}>Subject #{Number(parsedSubjectId)}</Link> },
          { title: <Link to={`/app/subjects/${parsedSubjectId}/events`}>Events</Link> },
          { title: crfName },
        ]}
        style={{ marginBottom: 16 }}
      />

      <Card
        style={{ marginBottom: 16, borderRadius: 6 }}
        styles={{ body: { padding: "16px 24px" } }}
      >
        <Space style={{ width: "100%", justifyContent: "space-between" }} align="center">
          <Space>
            <div>
              <Title level={4} style={{ margin: 0 }}>
                {crfName}
              </Title>
              <Text type="secondary">
                {sectionsCount} section{sectionsCount !== 1 ? "s" : ""}
              </Text>
            </div>
            <span className={`status ${statusClassName(recordStatus)}`}>{recordStatus}</span>
          </Space>
          <Space>
            <SaveIndicator status={saveStatus} />
            {canComplete && (
              <Button
                type="primary"
                onClick={onComplete}
                loading={isCompleting}
              >
                {t("entry.completeEvent")}
              </Button>
            )}
            <Button onClick={() => window.open(`/AdministrativeEditing?eventCrfId=${parsedEventCrfId}`, "_blank")}>
              {t("entry.adminEdit")}
            </Button>
            <Button onClick={onBack}>
              {t("entry.back")}
            </Button>
          </Space>
        </Space>
      </Card>
    </div>
  );
}
