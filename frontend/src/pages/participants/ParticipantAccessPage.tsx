import { useState } from "react";
import { Alert, Button, Card, Empty, Form, Input, InputNumber, Modal, Space, Table, Tag, Typography, message } from "antd";
import { LinkOutlined, StopOutlined, UserAddOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import { formatApiError } from "@/api/errors";
import { useCurrentStudy } from "@/hooks/useStudies";
import { SkeletonPage } from "@/components/SkeletonCard";
import {
  useCreateParticipantAccount,
  useIssueParticipantToken,
  useParticipantAccounts,
  useParticipantTokens,
  useRevokeParticipantToken,
  type IssuedParticipantTokenDTO,
  type ParticipantAccessTokenDTO,
  type ParticipantAccountDTO,
} from "@/hooks/useParticipantAccess";

const { Title, Text } = Typography;

const TOKEN_COLORS: Record<string, string> = {
  ACTIVE: "success",
  USED: "processing",
  REVOKED: "warning",
  EXPIRED: "error",
};

interface AccountFormValues {
  studySubjectId: number;
  displayLabel?: string;
  preferredLocale?: string;
}

interface TokenFormValues {
  expiresInHours?: number;
  scope?: string;
}

export default function ParticipantAccessPage() {
  const { t } = useTranslation();
  const { currentStudy } = useCurrentStudy();
  const studyId = currentStudy?.id;
  const [accountModalOpen, setAccountModalOpen] = useState(false);
  const [tokenModalOpen, setTokenModalOpen] = useState(false);
  const [issuedToken, setIssuedToken] = useState<IssuedParticipantTokenDTO | null>(null);
  const [selectedAccount, setSelectedAccount] = useState<ParticipantAccountDTO | null>(null);
  const [accountForm] = Form.useForm<AccountFormValues>();
  const [tokenForm] = Form.useForm<TokenFormValues>();

  const { data: accounts = [], isLoading } = useParticipantAccounts(studyId);
  const { data: tokens = [], isLoading: tokensLoading } = useParticipantTokens(selectedAccount?.id);
  const createAccount = useCreateParticipantAccount(studyId);
  const issueToken = useIssueParticipantToken(selectedAccount?.id);
  const revokeToken = useRevokeParticipantToken(selectedAccount?.id);

  if (!currentStudy) {
    return <Alert type="info" message={t("participantAccess.selectStudy")} />;
  }
  if (isLoading) {
    return <SkeletonPage />;
  }

  const submitAccount = async () => {
    try {
      const values = await accountForm.validateFields();
      await createAccount.mutateAsync(values);
      message.success(t("participantAccess.accountCreated"));
      setAccountModalOpen(false);
      accountForm.resetFields();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("participantAccess.error.accountCreate")));
    }
  };

  const submitToken = async () => {
    if (!selectedAccount) return;
    try {
      const values = await tokenForm.validateFields();
      const result = await issueToken.mutateAsync({
        participantAccountId: selectedAccount.id,
        expiresInHours: values.expiresInHours,
        scope: values.scope,
      });
      setIssuedToken(result);
      message.success(t("participantAccess.tokenIssued"));
      tokenForm.resetFields();
    } catch (err) {
      if (err instanceof Error && err.name === "ValidationError") return;
      message.error(formatApiError(err, t("participantAccess.error.tokenIssue")));
    }
  };

  const accountColumns = [
    { title: t("participantAccess.column.label"), dataIndex: "displayLabel", key: "displayLabel" },
    { title: t("participantAccess.column.studySubject"), dataIndex: "studySubjectId", key: "studySubjectId", width: 150 },
    { title: t("participantAccess.column.locale"), dataIndex: "preferredLocale", key: "preferredLocale", width: 130,
      render: (value: string | null) => value ?? "-",
    },
    { title: t("participantAccess.column.status"), dataIndex: "status", key: "status", width: 120,
      render: (value: string) => <Tag color={value === "ACTIVE" ? "success" : "warning"}>{value}</Tag>,
    },
    { title: t("participantAccess.column.created"), dataIndex: "createdDate", key: "createdDate", width: 190,
      render: (value: string) => value ? new Date(value).toLocaleString() : "-",
    },
    {
      title: t("participantAccess.column.actions"),
      key: "actions",
      width: 160,
      render: (_: unknown, record: ParticipantAccountDTO) => (
        <Button
          size="small"
          icon={<LinkOutlined />}
          onClick={() => {
            setSelectedAccount(record);
            setTokenModalOpen(true);
            setIssuedToken(null);
          }}
        >
          {t("participantAccess.action.tokens")}
        </Button>
      ),
    },
  ];

  const tokenColumns = [
    { title: t("participantAccess.token.scope"), dataIndex: "scope", key: "scope" },
    { title: t("participantAccess.token.status"), dataIndex: "status", key: "status",
      render: (value: string) => <Tag color={TOKEN_COLORS[value] ?? "default"}>{value}</Tag>,
    },
    { title: t("participantAccess.token.expires"), dataIndex: "expiresAt", key: "expiresAt",
      render: (value: string) => new Date(value).toLocaleString(),
    },
    { title: t("participantAccess.token.lastUsed"), dataIndex: "lastUsedAt", key: "lastUsedAt",
      render: (value: string | null) => value ? new Date(value).toLocaleString() : "-",
    },
    {
      title: t("participantAccess.column.actions"),
      key: "actions",
      render: (_: unknown, record: ParticipantAccessTokenDTO) => (
        <Button
          size="small"
          danger
          icon={<StopOutlined />}
          disabled={record.status === "REVOKED" || record.status === "EXPIRED"}
          onClick={() => revokeToken.mutate({ tokenId: record.id, reason: "operator revoked" })}
        >
          {t("participantAccess.action.revoke")}
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ justifyContent: "space-between", width: "100%" }} align="start">
        <div>
          <Title level={4} style={{ marginTop: 0 }}>{t("participantAccess.title")}</Title>
          <Text type="secondary">{currentStudy.name}</Text>
        </div>
        <Button type="primary" icon={<UserAddOutlined />} onClick={() => setAccountModalOpen(true)}>
          {t("participantAccess.newAccount")}
        </Button>
      </Space>

      <Card style={{ marginTop: 16 }}>
        <Table
          dataSource={accounts}
          columns={accountColumns}
          rowKey="id"
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: <Empty description={t("participantAccess.empty")} /> }}
        />
      </Card>

      <Modal
        title={t("participantAccess.modal.accountTitle")}
        open={accountModalOpen}
        onCancel={() => {
          setAccountModalOpen(false);
          accountForm.resetFields();
        }}
        onOk={submitAccount}
        confirmLoading={createAccount.isPending}
      >
        <Form form={accountForm} layout="vertical">
          <Form.Item
            name="studySubjectId"
            label={t("participantAccess.modal.studySubjectId")}
            rules={[{ required: true, message: t("participantAccess.modal.studySubjectRequired") }]}
          >
            <InputNumber min={1} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="displayLabel" label={t("participantAccess.modal.displayLabel")}>
            <Input maxLength={120} />
          </Form.Item>
          <Form.Item name="preferredLocale" label={t("participantAccess.modal.locale")}>
            <Input maxLength={20} placeholder="zh-CN" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        width={860}
        title={selectedAccount ? t("participantAccess.modal.tokensTitle", { label: selectedAccount.displayLabel }) : t("participantAccess.modal.tokens")}
        open={tokenModalOpen}
        onCancel={() => {
          setTokenModalOpen(false);
          setIssuedToken(null);
          tokenForm.resetFields();
        }}
        footer={null}
      >
        {issuedToken && (
          <Alert
            type="success"
            showIcon
            style={{ marginBottom: 16 }}
            message={t("participantAccess.oneTimeLink")}
            description={`${window.location.origin}${issuedToken.entryUrl}`}
          />
        )}
        <Form form={tokenForm} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="expiresInHours" label={t("participantAccess.token.expiresInHours")}>
            <InputNumber min={1} max={2160} placeholder="168" />
          </Form.Item>
          <Form.Item name="scope" label={t("participantAccess.token.scope")}>
            <Input placeholder="participant" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<LinkOutlined />} loading={issueToken.isPending} onClick={submitToken}>
              {t("participantAccess.action.issue")}
            </Button>
          </Form.Item>
        </Form>
        <Table
          size="small"
          loading={tokensLoading}
          dataSource={tokens}
          columns={tokenColumns}
          rowKey="id"
          pagination={{ pageSize: 6 }}
          locale={{ emptyText: <Empty description={t("participantAccess.token.empty")} /> }}
        />
      </Modal>
    </div>
  );
}
