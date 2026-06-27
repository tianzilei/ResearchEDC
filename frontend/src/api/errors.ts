import type { ApiError } from "@/api/client";

type ApiErrorObject = ApiError & Error;

export function isApiError(error: unknown): error is ApiErrorObject {
  return error instanceof Error
    && typeof (error as Partial<ApiError>).status === "number";
}

export function formatApiError(error: unknown, fallback: string): string {
  const message = error instanceof Error && error.message ? error.message : fallback;
  const requestId = isApiError(error) ? error.requestId : undefined;
  return requestId ? `${message} (Request ID: ${requestId})` : message;
}
