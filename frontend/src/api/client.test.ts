import { afterEach, describe, expect, it, vi } from "vitest";

import { API_AUTH_FAILURE_EVENT, apiClient } from "./client";

function response(body: string, init: ResponseInit) {
  return new Response(body, init);
}

describe("ApiClient error handling", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("preserves JSON error envelope fields and request id", async () => {
    const listener = vi.fn();
    window.addEventListener(API_AUTH_FAILURE_EVENT, listener);
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(response(JSON.stringify({
      timestamp: "2026-06-28T00:00:00Z",
      status: 403,
      error: "Forbidden",
      message: "No access",
      path: "/api/v1/studies",
      requestId: "req-123",
    }), {
      status: 403,
      statusText: "Forbidden",
      headers: { "Content-Type": "application/json" },
    })));

    await expect(apiClient.get("/api/v1/studies")).rejects.toMatchObject({
      status: 403,
      message: "No access",
      requestId: "req-123",
      path: "/api/v1/studies",
    });
    expect(listener).toHaveBeenCalledWith(expect.objectContaining({
      detail: { status: 403, path: "/api/v1/studies" },
    }));
    window.removeEventListener(API_AUTH_FAILURE_EVENT, listener);
  });

  it("keeps text error responses readable", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(response("Export artifact is not available", {
      status: 404,
      statusText: "Not Found",
      headers: { "Content-Type": "text/plain" },
    })));

    await expect(apiClient.download("/api/v1/exports/7/download")).rejects.toMatchObject({
      status: 404,
      message: "Export artifact is not available",
      details: "Export artifact is not available",
    });
  });
});
