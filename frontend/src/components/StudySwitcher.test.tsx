import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import StudySwitcher from "./StudySwitcher";
import type { Study } from "@/types/study";

const mockStudy: Study = {
  id: 1,
  name: "Clinical Trial A",
  identifier: "CTA-001",
  oid: "S_CTA001",
  type: "study",
  status: "available",
};

const mockSite: Study = {
  id: 2,
  name: "Site X",
  identifier: "SITE-X",
  oid: "S_SITEX",
  type: "site",
  status: "available",
};

const mockStudies = [
  {
    study: mockStudy,
    sites: [mockSite],
  },
];

vi.mock("@/hooks/useStudies", () => ({
  useStudies: () => ({ data: mockStudies }),
  useCurrentStudy: () => ({
    currentStudy: null,
    setCurrentStudy: vi.fn(),
  }),
}));

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const map: Record<string, string> = {
        "layout.selectStudy": "Select a study",
      };
      return map[key] ?? key;
    },
    i18n: { language: "en" },
  }),
}));

describe("StudySwitcher", () => {
  it("renders placeholder text when no study is selected", () => {
    render(<StudySwitcher />);
    expect(screen.getByText("Select a study")).toBeInTheDocument();
  });

  it("opens dropdown and shows study names on click", () => {
    render(<StudySwitcher />);
    const selector = document.querySelector(".ant-select-selector");
    expect(selector).not.toBeNull();
    if (!selector) return;
    fireEvent.mouseDown(selector);
    expect(screen.getByText("Clinical Trial A")).toBeInTheDocument();
  });
});
