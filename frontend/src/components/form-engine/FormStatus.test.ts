import { describe, it, expect } from "vitest";
import { isFieldDisabled } from "./FormStatus";

describe("FormStatus - isFieldDisabled", () => {
  it("disables field when lockedByOther is true", () => {
    expect(isFieldDisabled({ status: "DRAFT", lockedByOther: true })).toBe(true);
  });

  it("disables field when status is LOCKED", () => {
    expect(isFieldDisabled({ status: "LOCKED" })).toBe(true);
  });

  it("disables field when status is FROZEN", () => {
    expect(isFieldDisabled({ status: "FROZEN" })).toBe(true);
  });

  it("disables field when status is SIGNED", () => {
    expect(isFieldDisabled({ status: "SIGNED" })).toBe(true);
  });

  it("disables field when SUBMITTED and not initial entry", () => {
    expect(isFieldDisabled({ status: "SUBMITTED", isInitialEntry: false })).toBe(true);
  });

  it("enables field when SUBMITTED and is initial entry", () => {
    expect(isFieldDisabled({ status: "SUBMITTED", isInitialEntry: true })).toBe(false);
  });

  it("enables field when status is DRAFT", () => {
    expect(isFieldDisabled({ status: "DRAFT" })).toBe(false);
  });

  it("enables field when status is INITIAL", () => {
    expect(isFieldDisabled({ status: "INITIAL" })).toBe(false);
  });

  it("lockedByOther takes precedence over any status", () => {
    expect(isFieldDisabled({ status: "INITIAL", lockedByOther: true })).toBe(true);
    expect(isFieldDisabled({ status: "DRAFT", lockedByOther: true })).toBe(true);
  });

  it("defaults to enabled for unknown status", () => {
    expect(isFieldDisabled({ status: "UNKNOWN" as never })).toBe(false);
  });
});
