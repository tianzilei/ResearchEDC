import { useState, useCallback } from "react";
import { Select, Space, Spin } from "antd";
import { useTranslation } from "react-i18next";
import { useStudies, useCurrentStudy } from "@/hooks/useStudies";
import { apiClient } from "@/api/client";
import type { Study, StudySummary } from "@/types/study";

export default function StudySwitcher() {
  const { t } = useTranslation();
  const { data: studies, isLoading } = useStudies();
  const { currentStudy, setCurrentStudy } = useCurrentStudy();
  const [open, setOpen] = useState(false);
  const [searchResults, setSearchResults] = useState<StudySummary[] | null>(null);
  const [searchLoading, setSearchLoading] = useState(false);

  const allStudies = searchResults ?? studies ?? [];

  const handleChange = (_value: string, option: unknown) => {
    const opt = option as { item: Study };
    setCurrentStudy(opt.item);
    setOpen(false);
  };

  const handleSearch = useCallback((value: string) => {
    if (!value || value.trim().length === 0) {
      setSearchResults(null);
      return;
    }
    setSearchLoading(true);
    apiClient
      .get<StudySummary[]>("/api/v1/studies/search", { name: value.trim() })
      .then((results) => {
        setSearchResults(results);
        setSearchLoading(false);
      })
      .catch(() => {
        setSearchResults(null);
        setSearchLoading(false);
      });
  }, []);

  return (
    <Select
      showSearch
      value={currentStudy ? currentStudy.id.toString() : undefined}
      placeholder={isLoading ? t("common.loading") : t("layout.selectStudy")}
      open={open}
      onOpenChange={setOpen}
      onChange={handleChange}
      onSearch={handleSearch}
      filterOption={false}
      variant="borderless"
      className="study-switcher-select"
      notFoundContent={searchLoading ? <Spin size="small" /> : null}
      style={{
        minWidth: 200,
        color: "var(--header-text)",
        fontSize: 13,
      }}
      popupMatchSelectWidth={320}
      options={allStudies.flatMap((group) => [
        {
          key: `study-${group.study.id}`,
          value: group.study.id.toString(),
          label: group.study.name,
          item: group.study,
        },
        ...(group.sites ?? []).map((site) => ({
          key: `site-${site.id}`,
          value: site.id.toString(),
          label: `  \u00a0\u00a0 ${site.name}`,
          item: site,
        })),
      ])}
      optionRender={(option) => {
        const item = option.data.item as Study;
        return (
          <Space>
            <span>{item.name}</span>
            <span style={{ color: "var(--text-muted)", fontSize: 12 }}>
              {item.identifier ?? item.oid ?? ""}
            </span>
          </Space>
        );
      }}
    />
  );
}
