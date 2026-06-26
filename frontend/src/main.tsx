import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { AppProviders } from "@/providers/AppProviders";
import "@/styles/global.css";
import "@/i18n";

const rootElement = document.getElementById("root");
if (!rootElement) {
  throw new Error("Root element #root not found");
}

createRoot(rootElement).render(
  <StrictMode>
    <AppProviders />
  </StrictMode>,
);
