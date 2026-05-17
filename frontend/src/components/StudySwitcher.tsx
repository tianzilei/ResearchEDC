import { useState } from "react";
import { Select, Typography, Space } from "antd";
import { MedicineBoxOutlined, HomeOutlined } from "@ant-design/icons";
import { useStudies, useCurrentStudy } from "@/hooks/useStudies";
import type { Study } from "@/types/study";

const { Text } = Typography;

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
      value={currentStudy ? `${currentStudy.name}` : undefined}
      placeholder="Select a study..."
      open={open}
      onDropdownVisibleChange={setOpen}
      onChange={handleChange}
      style={{ minWidth: 240 }}
     dropdownStyle={{ width: 360 }}
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
        const item = option.data.item as Study;
        return (
          <Space>
            {item.type === "site" ? <HomeOutlined /> : <MedicineBoxOutlined />}
            <span>{item.name}</span>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {item.identifier}
            </Text>
          </Space>
        );
      }}
    />
  );
}
