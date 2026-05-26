import { useState } from "react";
import { Select, Space } from "antd";
import { useStudies, useCurrentStudy } from "@/hooks/useStudies";
import type { Study } from "@/types/study";

const PLACEHOLDER_STYLE: React.CSSProperties = {
  color: "var(--text-secondary)",
};

export default function StudySwitcher() {
  const { data: studies } = useStudies();
  const { currentStudy, setCurrentStudy } = useCurrentStudy();
  const [open, setOpen] = useState(false);

  const groupedStudies = studies ?? [];
  const topStudies = groupedStudies.filter((s) => s.study.type === "study");

  const handleChange = (_value: string, option: unknown) => {
    const opt = option as { item: Study };
    setCurrentStudy(opt.item);
    setOpen(false);
  };

  return (
    <Select
      showSearch
      value={currentStudy ? currentStudy.name : undefined}
      placeholder={<span style={PLACEHOLDER_STYLE}>选择项目</span>}
      open={open}
      onDropdownVisibleChange={setOpen}
      onChange={handleChange}
      variant="borderless"
      className="study-switcher-select"
      style={{
        minWidth: 200,
        color: "var(--header-text)",
        fontSize: 13,
      }}
      popupMatchSelectWidth={320}
      optionFilterProp="label"
      notFoundContent={null}
      options={topStudies.flatMap((group) => {
        const studyOpts = [
          {
            key: group.study.id.toString(),
            value: group.study.id.toString(),
            label: group.study.name,
            item: group.study,
          },
        ];

        const siteOpts = (group.sites ?? []).map((site) => ({
          key: site.id.toString(),
          value: site.id.toString(),
          label: `  \u00a0\u00a0 ${site.name}`,
          item: site,
        }));

        return [...studyOpts, ...siteOpts];
      })}
      optionRender={(option) => {
        const item = option.data.item;
        return (
          <Space>
            <span>{item.name}</span>
            <span style={{ color: "var(--text-muted)", fontSize: 12 }}>
              {item.identifier ?? ""}
            </span>
          </Space>
        );
      }}
    />
  );
}