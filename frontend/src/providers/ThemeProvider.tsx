import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from "react";
import { ConfigProvider } from "antd";
import { getTheme, type ThemeMode } from "@/styles/theme";

interface ThemeContextValue {
  mode: ThemeMode;
  toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextValue | null>(null);

function getInitialMode(): ThemeMode {
  const stored = localStorage.getItem("theme");
  if (stored === "daylight" || stored === "night") return stored;
  return "daylight";
}

interface ThemeProviderProps {
  children: ReactNode;
}

export function ThemeProvider({ children }: ThemeProviderProps) {
  const [mode, setMode] = useState<ThemeMode>(getInitialMode);

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", mode);
    localStorage.setItem("theme", mode);
  }, [mode]);

  const toggleTheme = useCallback(() => {
    setMode((prev) => (prev === "daylight" ? "night" : "daylight"));
  }, []);

  return (
    <ThemeContext.Provider value={{ mode, toggleTheme }}>
      <ConfigProvider theme={getTheme(mode)} wave={{ disabled: true }}>
        {children}
      </ConfigProvider>
    </ThemeContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useTheme(): ThemeContextValue {
  const ctx = useContext(ThemeContext);
  if (!ctx) {
    throw new Error("useTheme must be used within a ThemeProvider");
  }
  return ctx;
}
