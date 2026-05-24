import {
  createContext,
  useContext,
  useState,
  useCallback,
  type ReactNode,
} from "react";

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
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  getAccessToken: () => Promise<string | null>;
  loginError: string | null;
  loginLoading: boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

interface LoginResponse {
  token: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
}

function parseUserFromToken(token: string): UserInfo | null {
  try {
    const payload = JSON.parse(atob(token.split(".")[1] ?? ""));
    return {
      sub: payload.sub ?? "",
      name: payload.name ?? payload.preferred_username ?? payload.sub ?? "",
      email: payload.email ?? "",
      roles: payload.roles ?? [],
    };
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);
  const [loginLoading, setLoginLoading] = useState(false);

  useState(() => {
    const token = sessionStorage.getItem("oc_access_token");
    if (token) {
      const userInfo = parseUserFromToken(token);
      if (userInfo) {
        try {
          const payload = JSON.parse(atob(token.split(".")[1] ?? ""));
          const exp = payload.exp;
          if (exp && Date.now() > exp) {
            sessionStorage.removeItem("oc_access_token");
          } else {
            setUser(userInfo);
          }
        } catch {
          sessionStorage.removeItem("oc_access_token");
        }
      } else {
        sessionStorage.removeItem("oc_access_token");
      }
    }
    setIsInitialized(true);
  });

  const login = useCallback(async (username: string, password: string) => {
    setLoginError(null);
    setLoginLoading(true);
    try {
      const response = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("Invalid username or password");
        } else if (response.status === 403) {
          throw new Error("Account is disabled");
        } else {
          throw new Error("Login failed. Please try again.");
        }
      }

      const data: LoginResponse = await response.json();
      sessionStorage.setItem("oc_access_token", data.token);

      setUser({
        sub: data.username,
        name: `${data.firstName} ${data.lastName}`.trim() || data.username,
        email: data.email,
        roles: data.roles,
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : "Login failed";
      setLoginError(message);
      throw err;
    } finally {
      setLoginLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    sessionStorage.removeItem("oc_access_token");
    setUser(null);
    setLoginError(null);
  }, []);

  const getAccessToken = useCallback(async (): Promise<string | null> => {
    return sessionStorage.getItem("oc_access_token");
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
        loginError,
        loginLoading,
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
