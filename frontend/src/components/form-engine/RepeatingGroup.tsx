import { Button, Card, Space, Typography } from "antd";
import { PlusOutlined, DeleteOutlined } from "@ant-design/icons";
import { FormField, type FormItemConfig } from "@/components/form-engine/FormField";
import { useTranslation } from "react-i18next";

const { Text } = Typography;

interface RepeatingGroupProps {
  groupLabel: string;
  items: FormItemConfig[];
  instanceIndices: number[];
  formValues: Record<string, string>;
  onFieldChange: (name: string) => (value: unknown) => void;
  onAddInstance: () => void;
  onRemoveInstance: (index: number) => void;
  disabled?: boolean;
  saveErrorItemIds?: Set<number>;
}

function fieldName(itemId: number, instanceIndex: number): string {
  return `item_${itemId}_${instanceIndex}`;
}

export function RepeatingGroup({
  groupLabel,
  items,
  instanceIndices,
  formValues,
  onFieldChange,
  onAddInstance,
  onRemoveInstance,
  disabled,
  saveErrorItemIds,
}: RepeatingGroupProps) {
  const { t } = useTranslation();
  if (items.length === 0) return null;

  return (
    <Card
      size="small"
      title={<Text strong>{groupLabel}</Text>}
      extra={
        <Button
          type="dashed"
          size="small"
          icon={<PlusOutlined />}
          onClick={onAddInstance}
          disabled={disabled}
        >
          {t("entry.addGroup")}
        </Button>
      }
      style={{ marginBottom: 16 }}
    >
      {instanceIndices.length === 0 && (
        <Text type="secondary">{t("entry.noGroupInstances")}</Text>
      )}
      {instanceIndices.map((idx) => (
        <Card
          key={idx}
          size="small"
          type="inner"
          title={`${groupLabel} #${idx + 1}`}
          extra={
            instanceIndices.length > 1 && (
              <Button
                type="text"
                danger
                size="small"
                icon={<DeleteOutlined />}
                onClick={() => onRemoveInstance(idx)}
                disabled={disabled}
              />
            )
          }
          style={{ marginBottom: 12 }}
        >
          <Space direction="vertical" style={{ width: "100%" }}>
            {items
              .sort((a, b) => a.ordinal - b.ordinal)
              .map((item) => {
                const name = fieldName(item.itemId, idx);
                return (
                  <FormField
                    key={name}
                    item={item}
                    value={formValues[name]}
                    onChange={onFieldChange(name)}
                    disabled={disabled}
                    hasError={saveErrorItemIds?.has(item.itemId) ?? false}
                  />
                );
              })}
          </Space>
        </Card>
      ))}
    </Card>
  );
}
