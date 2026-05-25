import {
  createContext,
  useContext,
  useState,
  useCallback,
  useEffect,
  type ReactNode,
} from "react";

export interface UserInfo {
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  enabled: boolean;
  roles: string[];
}

interface MeResponse {
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  enabled: boolean;
  roles: string[];
  studyRoles: Array<{ studyId: number; roleName: string }>;
}

interface AuthContextValue {
  user: UserInfo | null;
  isAuthenticated: boolean;
  isInitialized: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  loginError: string | null;
  loginLoading: boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

function getCsrfToken(): string | null {
  for (const cookie of document.cookie.split(";")) {
    const [name, value] = cookie.trim().split("=");
    if (name === "XSRF-TOKEN") return value ?? null;
  }
  return null;
}

async function fetchCurrentUser(): Promise<UserInfo | null> {
  try {
    const res = await fetch("/api/v1/auth/me", {
      credentials: "same-origin",
    });
    if (!res.ok) return null;
    const data: MeResponse = await res.json();
    return {
      userId: data.userId,
      username: data.username,
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
      enabled: data.enabled,
      roles: data.roles,
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

  useEffect(() => {
    fetchCurrentUser()
      .then((userInfo) => {
        setUser(userInfo);
      })
      .catch(() => {
        setUser(null);
      })
      .finally(() => {
        setIsInitialized(true);
      });
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    setLoginError(null);
    setLoginLoading(true);
    try {
      const response = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
        credentials: "same-origin",
      });

      if (!response.ok) {
        setLoginLoading(false);
        throw new Error("Invalid username or password, or account temporarily unavailable.");
      }

      const userInfo = await fetchCurrentUser();
      if (userInfo) {
        setUser(userInfo);
      } else {
        throw new Error("Failed to retrieve user information.");
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : "Login failed";
      setLoginError(message);
      throw err;
    } finally {
      setLoginLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      const csrfToken = getCsrfToken();
      await fetch("/api/v1/auth/logout", {
        method: "POST",
        credentials: "same-origin",
        headers: csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {},
      });
    } catch {
      // Continue with local state cleanup even if server logout fails
    }
    setUser(null);
    setLoginError(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: user !== null,
        isInitialized,
        login,
        logout,
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
