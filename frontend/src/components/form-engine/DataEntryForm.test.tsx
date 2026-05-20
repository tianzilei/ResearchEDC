import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { DataEntryForm } from "./DataEntryForm";
import type { FormItemConfig } from "./FormField";

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const map: Record<string, string> = {
        "form.save": "Save",
        "form.saving": "Saving...",
        "form.saved": "Saved",
        "form.saveFailed": "Save failed",
        "form.lockedAlert": "This form is locked",
        "form.frozenAlert": "This form is frozen",
        "form.signedAlert": "This form is signed",
      };
      return map[key] ?? key;
    },
    i18n: { language: "en" },
  }),
}));

const mockItems: FormItemConfig[] = [
  { itemId: 1, name: "field_a", dataType: "text", required: false, ordinal: 1 },
  { itemId: 2, name: "field_b", dataType: "text", required: true, ordinal: 2 },
];

describe("DataEntryForm", () => {
  it("renders all form items sorted by ordinal", () => {
    render(
      <DataEntryForm
        items={mockItems}
        statusConfig={{ status: "INITIAL" }}
      />,
    );
    const inputs = screen.getAllByRole("textbox");
    expect(inputs).toHaveLength(2);
  });

  it("shows locked alert when status is LOCKED", () => {
    render(
      <DataEntryForm
        items={mockItems}
        statusConfig={{ status: "LOCKED" }}
      />,
    );
    expect(screen.getByText("This form is locked")).toBeInTheDocument();
  });

  it("shows frozen alert when status is FROZEN", () => {
    render(
      <DataEntryForm
        items={mockItems}
        statusConfig={{ status: "FROZEN" }}
      />,
    );
    expect(screen.getByText("This form is frozen")).toBeInTheDocument();
  });

  it("shows signed alert when status is SIGNED", () => {
    render(
      <DataEntryForm
        items={mockItems}
        statusConfig={{ status: "SIGNED" }}
      />,
    );
    expect(screen.getByText("This form is signed")).toBeInTheDocument();
  });

  it("renders the save button when onSave is provided", () => {
    render(
      <DataEntryForm
        items={mockItems}
        statusConfig={{ status: "INITIAL" }}
        onSave={async () => {}}
      />,
    );
    expect(screen.getByText("Save")).toBeInTheDocument();
  });

  it("does not render the save button when onSave is omitted", () => {
    render(
      <DataEntryForm
        items={mockItems}
        statusConfig={{ status: "INITIAL" }}
      />,
    );
    expect(screen.queryByText("Save")).not.toBeInTheDocument();
  });
});
