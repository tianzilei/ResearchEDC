import { useState, useCallback, useMemo } from "react";
import {
  Layout,
  Button,
  Space,
  Typography,
  message,
  Input,
  Select,
  Switch,
  Modal,
  Empty,
  Dropdown,
} from "antd";
import { Model } from "survey-core";
import { Survey } from "survey-react-ui";
import "survey-core/survey-core.min.css";
import { useTranslation } from "react-i18next";

const { Sider, Content } = Layout;
const { Text } = Typography;
const { TextArea } = Input;

export interface QuestionDef {
  name: string;
  type: string;
  title: string;
  isRequired: boolean;
  choices: { value: string; text: string }[];
  visibleIf: string;
}

export interface PageDef {
  name: string;
  title: string;
  elements: QuestionDef[];
}

export interface SurveyDef {
  title: string;
  description: string;
  pages: PageDef[];
}

const QUESTION_TYPES = [
  { value: "radiogroup", label: "builder.questionTypes.radiogroup" },
  { value: "checkbox", label: "builder.questionTypes.checkbox" },
  { value: "dropdown", label: "builder.questionTypes.dropdown" },
  { value: "text", label: "builder.questionTypes.text" },
  { value: "comment", label: "builder.questionTypes.comment" },
  { value: "rating", label: "builder.questionTypes.rating" },
  { value: "boolean", label: "builder.questionTypes.boolean" },
  { value: "expression", label: "builder.questionTypes.expression" },
];

let questionCounter = 0;
let pageCounter = 0;

function generateQuestionName(prefix = "Q"): string {
  questionCounter += 1;
  return `${prefix}_${String(questionCounter).padStart(2, "0")}`;
}

function generatePageName(): string {
  pageCounter += 1;
  return `page_${pageCounter}`;
}

function defaultChoices(type: string): { value: string; text: string }[] {
  if (["radiogroup", "checkbox", "dropdown"].includes(type)) {
    return [
      { value: "1", text: "Option 1" },
      { value: "2", text: "Option 2" },
      { value: "3", text: "Option 3" },
    ];
  }
  return [];
}

function createDefaultQuestion(type = "radiogroup"): QuestionDef {
  return {
    name: generateQuestionName(),
    type,
    title: "New Question",
    isRequired: false,
    choices: defaultChoices(type),
    visibleIf: "",
  };
}

function toSurveyJS(survey: SurveyDef): Record<string, unknown> {
  const pages = survey.pages.map((page) => ({
    name: page.name,
    title: page.title,
    elements: page.elements.map((q) => {
      const el: Record<string, unknown> = {
        type: q.type,
        name: q.name,
        title: q.title,
        isRequired: q.isRequired,
      };
      if (q.choices.length > 0 && ["radiogroup", "checkbox", "dropdown"].includes(q.type)) {
        el.choices = q.choices;
      }
      if (q.visibleIf) {
        el.visibleIf = q.visibleIf;
      }
      return el;
    }),
  }));
  return {
    title: survey.title,
    description: survey.description,
    pages,
  };
}

function fromSurveyJS(json: Record<string, unknown>): SurveyDef {
  const rawPages = (json.pages as Record<string, unknown>[]) ?? [];
  const pages: PageDef[] = rawPages.map((p) => ({
    name: (p.name as string) ?? generatePageName(),
    title: (p.title as string) ?? "",
    elements: ((p.elements as Record<string, unknown>[]) ?? []).map((el) => ({
      name: (el.name as string) ?? generateQuestionName(),
      type: (el.type as string) ?? "text",
      title: (el.title as string) ?? "",
      isRequired: (el.isRequired as boolean) ?? false,
      choices: ((el.choices as (Record<string, unknown> | string)[]) ?? []).map((c) =>
        typeof c === "object" ? { value: c.value as string, text: c.text as string } : { value: c, text: c },
      ),
      visibleIf: typeof el.visibleIf === "string" ? el.visibleIf : "",
    })),
  }));
  return {
    title: (json.title as string) ?? "",
    description: (json.description as string) ?? "",
    pages: pages.length > 0 ? pages : [createDefaultPage()],
  };
}

function createDefaultPage(): PageDef {
  return {
    name: generatePageName(),
    title: "Page 1",
    elements: [],
  };
}

function createDefaultSurvey(): SurveyDef {
  return {
    title: "New Questionnaire",
    description: "",
    pages: [createDefaultPage()],
  };
}

interface Props {
  value?: Record<string, unknown>;
  onChange: (json: Record<string, unknown>) => void;
}

export default function QuestionnaireBuilder({ value, onChange }: Props) {
  const { t } = useTranslation();
  const initial = useMemo(() => {
    questionCounter = 0;
    pageCounter = 0;
    return value?.pages ? fromSurveyJS(value) : createDefaultSurvey();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  const [survey, setSurvey] = useState<SurveyDef>(initial);
  const [selectedPageIdx, setSelectedPageIdx] = useState(0);
  const [selectedQIdx, setSelectedQIdx] = useState<number | null>(null);
  const [showPreview, setShowPreview] = useState(false);
  const [importModalOpen, setImportModalOpen] = useState(false);
  const [importJson, setImportJson] = useState("");

  const currentPage = survey.pages[selectedPageIdx];
  const selectedQ =
    selectedQIdx !== null ? currentPage?.elements[selectedQIdx] ?? null : null;

  const surveyJSJson = useMemo(() => toSurveyJS(survey), [survey]);

  const notifyChange = useCallback(
    (s: SurveyDef) => {
      setSurvey(s);
      onChange(toSurveyJS(s));
    },
    [onChange],
  );

  function updateTitle(title: string) {
    notifyChange({ ...survey, title });
  }

  function updateDescription(desc: string) {
    notifyChange({ ...survey, description: desc });
  }

  function addPage() {
    const newPage = createDefaultPage();
    newPage.title = `${t("builder.pageDefaultTitle")} ${survey.pages.length + 1}`;
    notifyChange({ ...survey, pages: [...survey.pages, newPage] });
    setSelectedPageIdx(survey.pages.length);
    setSelectedQIdx(null);
  }

  function deletePage(idx: number) {
    if (survey.pages.length <= 1) {
      message.warning(t("builder.minOnePage"));
      return;
    }
    const pages = survey.pages.filter((_, i) => i !== idx);
    notifyChange({ ...survey, pages });
    setSelectedPageIdx(Math.min(idx, pages.length - 1));
    setSelectedQIdx(null);
  }

  function addQuestion(type = "radiogroup") {
    const page = currentPage;
    if (!page) return;
    const q = createDefaultQuestion(type);
    const elements = [...page.elements, q] as QuestionDef[];
    const pages = [...survey.pages];
    const updatedPage: PageDef = { ...page, elements };
    pages[selectedPageIdx] = updatedPage;
    notifyChange({ ...survey, pages });
    setSelectedQIdx(elements.length - 1);
  }

  function deleteQuestion(idx: number) {
    const page = currentPage;
    if (!page) return;
    const elements = page.elements.filter((_, i) => i !== idx);
    const pages = [...survey.pages];
    const updatedPage: PageDef = { ...page, elements };
    pages[selectedPageIdx] = updatedPage;
    notifyChange({ ...survey, pages });
    setSelectedQIdx(null);
  }

  function moveQuestion(idx: number, direction: -1 | 1) {
    const page = currentPage;
    if (!page) return;
    const newIdx = idx + direction;
    if (newIdx < 0 || newIdx >= page.elements.length) return;
    const elements = [...page.elements];
    const a = elements[idx];
    const b = elements[newIdx];
    if (a == null || b == null) return;
    elements[idx] = b;
    elements[newIdx] = a;
    const pages = [...survey.pages];
    const updatedPage: PageDef = { ...page, elements };
    pages[selectedPageIdx] = updatedPage;
    notifyChange({ ...survey, pages });
    setSelectedQIdx(newIdx);
  }

  function updateQuestion(idx: number, updates: Partial<QuestionDef>) {
    const page = currentPage;
    if (!page) return;
    const elements = page.elements.map((q, i) =>
      i === idx ? { ...q, ...updates } : q,
    );
    const pages = [...survey.pages];
    const updatedPage: PageDef = { ...page, elements };
    pages[selectedPageIdx] = updatedPage;
    notifyChange({ ...survey, pages });
  }

  function handleImport() {
    try {
      const parsed = JSON.parse(importJson);
      const imported = fromSurveyJS(parsed);
      notifyChange(imported);
      setImportModalOpen(false);
      setImportJson("");
      message.success(t("builder.importSuccess"));
    } catch {
      message.error(t("builder.invalidJson"));
    }
  }

  function handleExport() {
    const json = JSON.stringify(surveyJSJson, null, 2);
    navigator.clipboard.writeText(json).then(
      () => message.success(t("builder.exportSuccess")),
      () => message.error(t("builder.exportFailed")),
    );
  }

  const previewModel = useMemo(
    () => new Model(surveyJSJson),
    [surveyJSJson],
  );

  const questionTypeActions = QUESTION_TYPES.map((qt) => ({
    key: qt.value,
    label: t(qt.label),
    onClick: () => addQuestion(qt.value),
  }));

  return (
    <Layout style={{ minHeight: 500, background: "var(--panel)" }}>
      <Sider width={280} theme="light" style={{ borderRight: "1px solid var(--border-light)", overflow: "auto", padding: 12 }}>
        <Space direction="vertical" style={{ width: "100%" }}>
          <div style={{ fontSize: 12, fontWeight: 600, color: "var(--text-muted)", marginBottom: 4 }}>{t("builder.pages")}</div>
          {survey.pages.map((page, pi) => (
            <div key={page.name}>
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  padding: "4px 8px",
                  cursor: "pointer",
                  borderRadius: 4,
                  background: selectedPageIdx === pi ? "var(--info-bg)" : undefined,
                }}
                onClick={() => {
                  setSelectedPageIdx(pi);
                  setSelectedQIdx(null);
                }}
              >
                <Text strong style={{ fontSize: 13 }}>
                  {page.title || `${t("builder.pageDefaultTitle")} ${pi + 1}`}
                </Text>
                <Button type="text" size="small" danger onClick={() => deletePage(pi)}>
                  {t("common.delete")}
                </Button>
              </div>
              {selectedPageIdx === pi && (
                <div style={{ paddingLeft: 16, marginTop: 4 }}>
                  {page.elements.length === 0 && (
                    <Text type="secondary" style={{ fontSize: 12, display: "block", padding: "4px 0" }}>
                      {t("builder.noQuestions")}
                    </Text>
                  )}
                  {page.elements.map((q, qi) => (
                    <div
                      key={q.name}
                      style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                        padding: "3px 6px",
                        borderRadius: 3,
                        cursor: "pointer",
                        background: selectedQIdx === qi ? "var(--info-bg)" : undefined,
                        fontSize: 12,
                        marginBottom: 2,
                      }}
                      onClick={() => setSelectedQIdx(qi)}
                    >
                      <Space size={4}>
                        <Text code style={{ fontSize: 12 }}>{q.name}</Text>
                        <Text ellipsis style={{ maxWidth: 100, fontSize: 12 }}>
                          {q.title || t("builder.untitled")}
                        </Text>
                      </Space>
                      <Space size={2}>
                        <Button type="text" size="small" onClick={() => moveQuestion(qi, -1)}>
                          {t("common.up")}
                        </Button>
                        <Button type="text" size="small" onClick={() => moveQuestion(qi, 1)}>
                          {t("common.down")}
                        </Button>
                        <Button type="text" size="small" danger onClick={() => deleteQuestion(qi)}>
                          {t("common.delete")}
                        </Button>
                      </Space>
                    </div>
                  ))}
                  <Dropdown menu={{ items: questionTypeActions }} trigger={["click"]}>
                    <Button size="small" type="dashed" style={{ width: "100%", marginTop: 8 }}>
                      {t("builder.addQuestion")}
                    </Button>
                  </Dropdown>
                </div>
              )}
            </div>
          ))}
          <Button size="small" onClick={addPage} block>
            {t("builder.addPage")}
          </Button>
        </Space>
      </Sider>

      <Content style={{ padding: 16, overflow: "auto", maxHeight: 600 }}>
        {selectedQ && selectedQIdx != null ? (() => {
          const qIdx = selectedQIdx;
          return (
          <Space direction="vertical" style={{ width: "100%" }}>
            <Text strong>Question: {selectedQ.name}</Text>

            <div>
              <Text style={{ fontSize: 12, display: "block", marginBottom: 4 }}>{t("builder.questionType")}</Text>
              <Select
                value={selectedQ.type}
                onChange={(v) => updateQuestion(qIdx, { type: v, choices: v !== selectedQ.type ? defaultChoices(v) : selectedQ.choices })}
                style={{ width: "100%" }}
                options={QUESTION_TYPES.map((qt) => ({ ...qt, label: t(qt.label) }))}
              />
            </div>

            <div>
              <Text style={{ fontSize: 12, display: "block", marginBottom: 4 }}>{t("builder.title")}</Text>
              <Input value={selectedQ.title} onChange={(e) => updateQuestion(qIdx, { title: e.target.value })} />
            </div>

            <div>
              <Space>
                <Switch checked={selectedQ.isRequired} onChange={(v) => updateQuestion(qIdx, { isRequired: v })} />
                <Text style={{ fontSize: 12 }}>{t("builder.required")}</Text>
              </Space>
            </div>

            {["radiogroup", "checkbox", "dropdown"].includes(selectedQ.type) && (
              <div>
                <Text style={{ fontSize: 12, display: "block", marginBottom: 4 }}>{t("builder.choices")}</Text>
                {selectedQ.choices.map((choice, ci) => (
                  <Space key={ci} style={{ display: "flex", marginBottom: 4 }}>
                    <Input
                      size="small"
                      value={choice.value}
                      onChange={(e) => {
                        updateQuestion(qIdx, {
                          choices: selectedQ.choices.map((ch, i) =>
                            i === ci ? { ...ch, value: e.target.value } : ch,
                          ),
                        });
                      }}
                      style={{ width: 60 }}
                      placeholder={t("builder.value")}
                    />
                    <Input
                      size="small"
                      value={choice.text}
                      onChange={(e) => {
                        updateQuestion(qIdx, {
                          choices: selectedQ.choices.map((ch, i) =>
                            i === ci ? { ...ch, text: e.target.value } : ch,
                          ),
                        });
                      }}
                      placeholder={t("builder.label")}
                    />
                    <Button
                      type="text"
                      size="small"
                      danger
                      onClick={() => {
                        const c = selectedQ.choices.filter((_, i) => i !== ci);
                        updateQuestion(qIdx, { choices: c });
                      }}
                    >
                      {t("common.delete")}
                    </Button>
                  </Space>
                ))}
                <Button
                  size="small"
                  type="dashed"
                  onClick={() => {
                    const c = [...selectedQ.choices, { value: String(selectedQ.choices.length + 1), text: `${t("builder.option")} ${selectedQ.choices.length + 1}` }];
                    updateQuestion(qIdx, { choices: c });
                  }}
                >
                  {t("builder.addChoice")}
                </Button>
              </div>
            )}

            {selectedQ.type === "rating" && (
              <div>
                <Text style={{ fontSize: 12, display: "block", marginBottom: 4 }}>
                  {t("builder.rateScale")}
                </Text>
              </div>
            )}

            <div>
              <Text style={{ fontSize: 12, display: "block", marginBottom: 4 }}>{t("builder.visibleWhen")}</Text>
              <Input
                value={selectedQ.visibleIf}
                onChange={(e) => updateQuestion(qIdx, { visibleIf: e.target.value })}
                placeholder={t("builder.visibleWhenPlaceholder")}
              />
            </div>
          </Space>
          );
        })() : currentPage ? (
          <Space direction="vertical" style={{ width: "100%" }}>
            <Text style={{ fontSize: 12, display: "block", marginBottom: 4 }}>{t("builder.surveyTitle")}</Text>
            <Input value={survey.title} onChange={(e) => updateTitle(e.target.value)} />

            <Text style={{ fontSize: 12, display: "block", marginBottom: 4 }}>{t("builder.description")}</Text>
            <Input value={survey.description} onChange={(e) => updateDescription(e.target.value)} />

            <Empty description={t("builder.selectQuestion")} />
          </Space>
        ) : null}
      </Content>

      <div style={{ padding: 12, borderTop: "1px solid var(--border-light)", display: "flex", justifyContent: "space-between" }}>
        <Space>
          <Button onClick={() => setShowPreview(!showPreview)}>
            {showPreview ? t("builder.hidePreview") : t("builder.showPreview")}
          </Button>
          <Button onClick={() => setImportModalOpen(true)}>
            {t("builder.importJson")}
          </Button>
          <Button onClick={handleExport}>
            {t("builder.exportJson")}
          </Button>
        </Space>
      </div>

      {showPreview && (
        <div style={{ padding: 16, borderTop: "1px solid var(--border-light)", background: "var(--bg-secondary)" }}>
          <Text strong style={{ display: "block", marginBottom: 8 }}>{t("builder.livePreview")}</Text>
          <div style={{ maxWidth: 600, margin: "0 auto" }}>
            <Survey model={previewModel} />
          </div>
        </div>
      )}

      <Modal
        title={t("builder.importModalTitle")}
        open={importModalOpen}
        onCancel={() => { setImportModalOpen(false); setImportJson(""); }}
        onOk={handleImport}
      >
        <TextArea
          rows={12}
          value={importJson}
          onChange={(e) => setImportJson(e.target.value)}
          placeholder={t("builder.importPlaceholder")}
          style={{ fontFamily: "var(--font-mono)", fontSize: 12 }}
        />
      </Modal>
    </Layout>
  );
}
