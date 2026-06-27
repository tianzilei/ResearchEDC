import { describe, expect, it } from "vitest";

import { formatApiError, isApiError } from "./errors";

describe("API error formatting", () => {
  it("includes request id when present", () => {
    const error = Object.assign(new Error("No access"), {
      status: 403,
      requestId: "req-123",
    });

    expect(isApiError(error)).toBe(true);
    expect(formatApiError(error, "Fallback")).toBe("No access (Request ID: req-123)");
  });

  it("uses fallback for non-error values", () => {
    expect(isApiError("failed")).toBe(false);
    expect(formatApiError("failed", "Fallback")).toBe("Fallback");
  });
});
