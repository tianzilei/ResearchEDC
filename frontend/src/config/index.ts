/**
 * Application configuration.
 *
 * All runtime-configurable values are read from environment variables
 * or Vite import.meta.env at build time.
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/OpenClinica";

const KEYCLOAK_CONFIG = {
  url: import.meta.env.VITE_KEYCLOAK_URL ?? "http://localhost:8080/auth",
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? "openclinica",
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? "openclinica-frontend",
} as const;

const APP_CONFIG = {
  appName: "OpenClinica",
  apiBaseUrl: API_BASE_URL,
  keycloak: KEYCLOAK_CONFIG,
} as const;

export default APP_CONFIG;
