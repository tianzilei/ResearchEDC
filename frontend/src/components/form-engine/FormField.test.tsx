import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { Form } from "antd";
import { FormField, type FormItemConfig } from "./FormField";

function renderWithForm(item: FormItemConfig, value?: string) {
  return render(
    <Form>
      <FormField item={item} value={value} />
    </Form>,
  );
}

describe("FormField", () => {
  it("renders a text input by default", () => {
    const item: FormItemConfig = {
      itemId: 1,
      name: "subject_id",
      dataType: "text",
      required: false,
      ordinal: 1,
    };
    renderWithForm(item);
    const input = screen.getByRole("textbox");
    expect(input).toBeInTheDocument();
  });

  it("renders a number input for number dataType", () => {
    const item: FormItemConfig = {
      itemId: 2,
      name: "age",
      dataType: "number",
      required: false,
      ordinal: 2,
    };
    renderWithForm(item);
    const input = screen.getByRole("spinbutton");
    expect(input).toBeInTheDocument();
  });

  it("renders a select field when responseType is select", () => {
    const item: FormItemConfig = {
      itemId: 3,
      name: "gender",
      dataType: "text",
      responseType: "select",
      required: false,
      ordinal: 3,
      options: [
        { label: "Male", value: "m" },
        { label: "Female", value: "f" },
      ],
    };
    const { container } = renderWithForm(item);
    const select = container.querySelector(".ant-select");
    expect(select).toBeInTheDocument();
  });

  it("renders radio buttons when responseType is radio", () => {
    const item: FormItemConfig = {
      itemId: 4,
      name: "consent",
      dataType: "text",
      responseType: "radio",
      required: false,
      ordinal: 4,
      options: [
        { label: "Yes", value: "y" },
        { label: "No", value: "n" },
      ],
    };
    renderWithForm(item);
    expect(screen.getByText("Yes")).toBeInTheDocument();
    expect(screen.getByText("No")).toBeInTheDocument();
  });

  it("shows units in the label when provided", () => {
    const item: FormItemConfig = {
      itemId: 5,
      name: "weight",
      dataType: "number",
      required: false,
      ordinal: 5,
      units: "kg",
    };
    renderWithForm(item);
    expect(screen.getByText("(kg)")).toBeInTheDocument();
  });

  it("disables the field when disabled prop is true", () => {
    const item: FormItemConfig = {
      itemId: 6,
      name: "locked_field",
      dataType: "text",
      required: false,
      ordinal: 6,
    };
    render(<Form><FormField item={item} disabled={true} /></Form>);
    const input = screen.getByRole("textbox");
    expect(input).toBeDisabled();
  });

  it("renders textarea for dataType textarea", () => {
    const item: FormItemConfig = {
      itemId: 7,
      name: "notes",
      dataType: "textarea",
      required: false,
      ordinal: 7,
    };
    renderWithForm(item);
    const textarea = screen.getByRole("textbox");
    expect(textarea.tagName).toBe("TEXTAREA");
  });
});
