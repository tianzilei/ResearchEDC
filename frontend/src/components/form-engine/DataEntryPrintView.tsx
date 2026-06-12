import { useMemo } from "react";
import { Typography, Divider } from "antd";
import type { CrfVersion, ItemDTO } from "@/types/crf";
import type { ItemDataDTO } from "@/types/datacapture";
import { useTranslation } from "react-i18next";

const { Title, Text } = Typography;

interface DataEntryPrintViewProps {
  crfVersion: CrfVersion;
  allSectionItems: Map<number, ItemDTO[]>;
  itemData: ItemDataDTO[];
  groupMap: Map<number, { groupId: number; groupLabel: string }>;
  hiddenItemIds: Set<number>;
}

function PrintField({ item, value }: { item: ItemDTO; value: string | undefined }) {
  return (
    <div
      style={{
        display: "flex",
        padding: "4px 0",
        borderBottom: "1px solid #e8e8e8",
        fontSize: 11,
      }}
    >
      <Text strong style={{ width: "38%", minWidth: 120, paddingRight: 8 }}>
        {item.name}
        {item.units && <Text type="secondary"> ({item.units})</Text>}
      </Text>
      <Text style={{ flex: 1, whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
        {value || "\u2014"}
      </Text>
    </div>
  );
}

export default function DataEntryPrintView({
  crfVersion,
  allSectionItems,
  itemData,
  groupMap,
  hiddenItemIds,
}: DataEntryPrintViewProps) {
  const { t } = useTranslation();
  const sections = (crfVersion.sections ?? []).slice().sort((a, b) => a.ordinal - b.ordinal);

  const itemValueMap = useMemo(() => {
    const map = new Map<string, string>();
    for (const d of itemData) {
      if (d.deleted) continue;
      const key = d.ordinal != null ? `${d.itemId}_${d.ordinal}` : `${d.itemId}`;
      map.set(key, d.value ?? "");
    }
    return map;
  }, [itemData]);

  return (
    <div
      id="print-view"
      className="print-only"
      style={{
        padding: "20px 28px",
        fontFamily: "Tahoma, Arial, Helvetica, sans-serif",
        fontSize: 11,
        lineHeight: 1.4,
        color: "#4D4D4D",
        maxWidth: 820,
      }}
    >
      <Title level={4} style={{ margin: "0 0 4px", fontSize: 16 }}>
        {crfVersion.name}
      </Title>
      <Text type="secondary" style={{ display: "block", marginBottom: 16 }}>
        {sections.length} {sections.length === 1 ? t("form.section") : t("form.sections")}
      </Text>

      {sections.map((section) => {
        const items = allSectionItems.get(section.sectionId) ?? [];
        const visibleItems = items.filter((i) => !hiddenItemIds.has(i.itemId));
        if (visibleItems.length === 0) return null;

        const ungrouped: ItemDTO[] = [];
        const grouped = new Map<number, ItemDTO[]>();
        for (const item of visibleItems) {
          const g = groupMap.get(item.itemId);
          if (g) {
            const arr = grouped.get(g.groupId) || [];
            arr.push(item);
            grouped.set(g.groupId, arr);
          } else {
            ungrouped.push(item);
          }
        }

        const groupOrdinals = new Map<number, number[]>();
        for (const [gid, gItems] of grouped) {
          const ordinals = new Set<number>();
          for (const d of itemData) {
            if (d.deleted) continue;
            if (d.ordinal != null && gItems.some((gi) => gi.itemId === d.itemId)) {
              ordinals.add(d.ordinal);
            }
          }
          if (ordinals.size === 0) ordinals.add(0);
          groupOrdinals.set(gid, Array.from(ordinals).sort());
        }

        return (
          <div key={section.sectionId} style={{ marginBottom: 20, pageBreakInside: "avoid" }}>
            <Divider orientation="left" style={{ margin: "0 0 10px", fontSize: 13 }}>
              <Text strong>{section.title || section.label || `Section`}</Text>
            </Divider>

            {ungrouped.length > 0 && (
              <div style={{ marginBottom: 8 }}>
                {ungrouped
                  .sort((a, b) => a.ordinal - b.ordinal)
                  .map((item) => (
                    <PrintField
                      key={`item_${item.itemId}`}
                      item={item}
                      value={itemValueMap.get(`${item.itemId}`)}
                    />
                  ))}
              </div>
            )}

            {Array.from(grouped.entries()).map(([gid, gItems]) => {
              const info = groupMap.get(gItems[0]?.itemId ?? 0);
              const label = info?.groupLabel ?? `Group ${gid}`;
              const ords = groupOrdinals.get(gid) ?? [0];

              return (
                <div key={`group-${gid}`} style={{ marginBottom: 8 }}>
                  <Text strong style={{ fontSize: 12 }}>
                    {label}
                  </Text>
                  {ords.map((ord) => (
                    <div
                      key={`group-${gid}-${ord}`}
                      style={{
                        marginLeft: 12,
                        marginTop: 4,
                        padding: "2px 8px",
                        borderLeft: "2px solid #e8e8e8",
                      }}
                    >
                      {ords.length > 1 && (
                        <Text type="secondary" style={{ fontSize: 10, display: "block" }}>
                          {label} #{ord + 1}
                        </Text>
                      )}
                      {gItems
                        .sort((a, b) => a.ordinal - b.ordinal)
                        .map((item) => (
                          <PrintField
                            key={`item_${item.itemId}_${ord}`}
                            item={item}
                            value={itemValueMap.get(`${item.itemId}_${ord}`)}
                          />
                        ))}
                    </div>
                  ))}
                </div>
              );
            })}
          </div>
        );
      })}
    </div>
  );
}
