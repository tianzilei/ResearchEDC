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

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl.replace(/\/+$/, "");
  }

  private async request<T>(config: RequestConfig): Promise<T> {
    const url = new URL(`${this.baseUrl}${config.path}`);

    if (config.params) {
      for (const [key, value] of Object.entries(config.params)) {
        if (value !== undefined) {
          url.searchParams.set(key, String(value));
        }
      }
    }

    const headers: Record<string, string> = {
      Accept: "application/json",
      ...config.headers,
    };

    if (config.body !== undefined && !(config.body instanceof FormData)) {
      headers["Content-Type"] = "application/json";
    }

    const response = await fetch(url.toString(), {
      method: config.method,
      headers,
      body:
        config.body instanceof FormData
          ? config.body
          : config.body !== undefined
            ? JSON.stringify(config.body)
            : undefined,
    });

    if (!response.ok) {
      const errorBody = await response.text().catch(() => "");
      const error: ApiError = {
        status: response.status,
        message: errorBody || response.statusText,
      };
      throw error;
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return response.json() as Promise<T>;
  }

  get<T>(path: string, params?: RequestConfig["params"]) {
    return this.request<T>({ method: "GET", path, params });
  }

  post<T>(path: string, body?: unknown) {
    return this.request<T>({ method: "POST", path, body });
  }

  put<T>(path: string, body?: unknown) {
    return this.request<T>({ method: "PUT", path, body });
  }

  patch<T>(path: string, body?: unknown) {
    return this.request<T>({ method: "PATCH", path, body });
  }

  delete<T>(path: string) {
    return this.request<T>({ method: "DELETE", path });
  }
}

export const apiClient = new ApiClient(APP_CONFIG.apiBaseUrl);
export type { ApiError, RequestConfig };
