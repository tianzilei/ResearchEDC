import { Input, InputNumber, Select, DatePicker, Checkbox, Radio, Space, Form, Typography, Badge } from "antd";

const { TextArea } = Input;

export interface FormItemConfig {
  itemId: number;
  name: string;
  description?: string;
  dataType: string;
  responseType?: string;
  required: boolean;
  ordinal: number;
  units?: string;
  defaultValue?: string;
  regexp?: string;
  regexpErrorMsg?: string;
  options?: { label: string; value: string }[];
  groupId?: number;
  groupLabel?: string;
}

interface FormFieldProps {
  item: FormItemConfig;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any -- Ant Design input components accept diverse value types
  value?: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any -- Ant Design input components have heterogeneous onChange signatures
  onChange?: (value: any) => void;
  disabled?: boolean;
  hasError?: boolean;
  dnCount?: number;
}

const typeMap: Record<string, string> = {
  "text": "text",
  "number": "number",
  "date": "date",
  "textarea": "textarea",
  "select": "select",
  "radio": "radio",
  "checkbox": "checkbox",
  "calculation": "text",
  "group": "text",
};

export function FormField({ item, value, onChange, disabled, hasError, dnCount }: FormFieldProps) {
  const type = typeMap[item.dataType?.toLowerCase()] ?? "text";
  const isRequired = item.required;

  const commonProps = {
    disabled,
    placeholder: item.description ?? item.name,
    onChange,
    value,
  };

  const renderField = () => {
    if (item.responseType === "select" || item.responseType === "radio") {
      if (item.responseType === "radio") {
        return (
          <Radio.Group {...commonProps}>
            {item.options?.map((opt) => (
              <Radio key={opt.value} value={opt.value}>{opt.label}</Radio>
            ))}
          </Radio.Group>
        );
      }
      return (
        <Select {...commonProps} style={{ width: "100%" }} allowClear>
          {item.options?.map((opt) => (
            <Select.Option key={opt.value} value={opt.value}>{opt.label}</Select.Option>
          ))}
        </Select>
      );
    }

    switch (type) {
      case "number": return <InputNumber {...commonProps} style={{ width: "100%" }} />;
      case "date": return <DatePicker {...commonProps} style={{ width: "100%" }} />;
      case "textarea": return <TextArea {...commonProps} rows={3} />;
      case "checkbox": return <Checkbox {...commonProps} checked={value}>{item.name}</Checkbox>;
      default: return <Input {...commonProps} />;
    }
  };

  return (
    <Form.Item
      label={<Space>
        {dnCount != null && dnCount > 0 && (
          <Badge count={dnCount} size="small" style={{ backgroundColor: "#faad14" }} />
        )}
        {item.name}
        {item.units && <Typography.Text type="secondary">({item.units})</Typography.Text>}
      </Space>}
      required={isRequired}
      validateTrigger="onBlur"
      validateStatus={hasError ? "error" : undefined}
      rules={[
        { required: isRequired, message: `${item.name} is required` },
        item.regexp ? {
          pattern: new RegExp(item.regexp),
          message: item.regexpErrorMsg ?? `Invalid format`,
        } : {},
      ]}
    >
      {renderField()}
    </Form.Item>
  );
}
