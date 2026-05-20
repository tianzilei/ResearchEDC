import {
  createContext,
  useContext,
  useState,
  useCallback,
  type ReactNode,
} from "react";
import APP_CONFIG from "@/config";

interface UserInfo {
  sub: string;
  name: string;
  email: string;
  roles: string[];
}

interface AuthContextValue {
  user: UserInfo | null;
  isAuthenticated: boolean;
  isInitialized: boolean;
  login: () => void;
  logout: () => void;
  getAccessToken: () => Promise<string | null>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * Keycloak / OIDC authentication provider.
 *
 * In its baseline form this provider checks for an existing session token
 * on mount. Full Keycloak JS Adapter integration (redirect login, token
 * refresh) can be plugged in once the Keycloak realm is configured.
 *
 * For now, the provider exposes a login() redirect and a placeholder token
 * flow that can be replaced with @react-keycloak/web or oidc-client-ts.
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

  useState(() => {
    const token = sessionStorage.getItem("oc_access_token");
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split(".")[1] ?? ""));
        setUser({
          sub: payload.sub ?? "",
          name: payload.name ?? payload.preferred_username ?? "",
          email: payload.email ?? "",
          roles: payload.realm_access?.roles ?? [],
        });
      } catch {
        sessionStorage.removeItem("oc_access_token");
      }
    }
    setIsInitialized(true);
  });

  const login = useCallback(() => {
    const { url, realm, clientId } = APP_CONFIG.keycloak;
    const redirectUri = `${window.location.origin}/app/dashboard`;
    const keycloakUrl = `${url}/realms/${realm}/protocol/openid-connect/auth?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=openid`;
    window.location.href = keycloakUrl;
  }, []);

  const logout = useCallback(() => {
    sessionStorage.removeItem("oc_access_token");
    setUser(null);
  }, []);

  const getAccessToken = useCallback((): Promise<string | null> => {
    return Promise.resolve(sessionStorage.getItem("oc_access_token"));
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: user !== null,
        isInitialized,
        login,
        logout,
        getAccessToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
