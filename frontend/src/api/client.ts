import APP_CONFIG from "@/config";

type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

interface RequestConfig {
  method: HttpMethod;
  path: string;
  body?: unknown;
  params?: Record<string, string | number | boolean | undefined>;
  headers?: Record<string, string>;
}

interface ApiError {
  status: number;
  message: string;
  details?: unknown;
}

const API_AUTH_FAILURE_EVENT = "researchedc:api-auth-failure";

interface ApiAuthFailureDetail {
  status: 401 | 403;
  path: string;
}

interface DownloadResult {
  blob: Blob;
  filename?: string;
}

function emitApiAuthFailure(status: 401 | 403, path: string) {
  window.dispatchEvent(new CustomEvent<ApiAuthFailureDetail>(API_AUTH_FAILURE_EVENT, {
    detail: { status, path },
  }));
}

function getCsrfToken(): string | null {
  for (const cookie of document.cookie.split(";")) {
    const [name, value] = cookie.trim().split("=");
    if (name === "XSRF-TOKEN") return value ?? null;
  }
  return null;
}

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl.replace(/\/+$/, "");
  }

  private buildUrl(path: string, params?: RequestConfig["params"]) {
    const url = new URL(`${this.baseUrl}${path}`, window.location.origin);

    if (params) {
      for (const [key, value] of Object.entries(params)) {
        if (value !== undefined) url.searchParams.set(key, String(value));
      }
    }

    return url;
  }

  private async handleErrorResponse(response: Response, path: string): Promise<never> {
    const errorBody = await response.text().catch(() => "");
    if (response.status === 401 || response.status === 403) {
      emitApiAuthFailure(response.status, path);
    }
    const error: ApiError & Error = Object.assign(new Error(errorBody || response.statusText), {
      status: response.status,
      message: errorBody || response.statusText,
    });
    throw error;
  }

  private async request<T>(config: RequestConfig): Promise<T> {
    const url = this.buildUrl(config.path, config.params);

    const headers: Record<string, string> = {
      Accept: "application/json",
      ...config.headers,
    };

    if (config.body !== undefined && !(config.body instanceof FormData)) {
      headers["Content-Type"] = "application/json";
    }

    if (config.method === "POST" || config.method === "PUT" || config.method === "PATCH" || config.method === "DELETE") {
      const csrfToken = getCsrfToken();
      if (csrfToken) headers["X-XSRF-TOKEN"] = csrfToken;
    }

    const response = await fetch(url.toString(), {
      method: config.method,
      headers,
      credentials: "same-origin",
      body: config.body instanceof FormData ? config.body : config.body !== undefined ? JSON.stringify(config.body) : undefined,
    });

    if (!response.ok) {
      return this.handleErrorResponse(response, config.path);
    }

    if (response.status === 204) return undefined as T;

    const contentType = response.headers.get("Content-Type") ?? "";
    if (!contentType.includes("application/json")) {
      return undefined as T;
    }
    return response.json() as Promise<T>;
  }

  async download(path: string, params?: RequestConfig["params"]): Promise<DownloadResult> {
    const url = this.buildUrl(path, params);
    const response = await fetch(url.toString(), {
      method: "GET",
      headers: { Accept: "*/*" },
      credentials: "same-origin",
    });

    if (!response.ok) {
      return this.handleErrorResponse(response, path);
    }

    const disposition = response.headers.get("Content-Disposition") ?? "";
    const filenameRe = /filename="?([^";\n]+)"?/;
    const match = filenameRe.exec(disposition);
    return { blob: await response.blob(), filename: match?.[1] };
  }

  get<T>(path: string, params?: RequestConfig["params"]) { return this.request<T>({ method: "GET", path, params }); }
  post<T>(path: string, body?: unknown, params?: RequestConfig["params"]) { return this.request<T>({ method: "POST", path, body, params }); }
  put<T>(path: string, body?: unknown, params?: RequestConfig["params"]) { return this.request<T>({ method: "PUT", path, body, params }); }
  patch<T>(path: string, body?: unknown, params?: RequestConfig["params"]) { return this.request<T>({ method: "PATCH", path, body, params }); }
  delete<T>(path: string, params?: RequestConfig["params"]) { return this.request<T>({ method: "DELETE", path, params }); }
}

export const apiClient = new ApiClient(APP_CONFIG.apiBaseUrl);
export { API_AUTH_FAILURE_EVENT, emitApiAuthFailure };
export type { ApiAuthFailureDetail, ApiError, DownloadResult, RequestConfig };
