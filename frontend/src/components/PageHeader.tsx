import type { ReactNode } from "react";

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  actions?: ReactNode;
  className?: string;
}

export default function PageHeader({ title, subtitle, actions, className }: PageHeaderProps) {
  return (
    <div className={`page-header${className ? ` ${className}` : ""}`}>
      <div className="page-header-left">
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <div className="accent-bar" />
          <h1 className="page-header-title">{title}</h1>
        </div>
        {subtitle && <p className="page-header-subtitle">{subtitle}</p>}
      </div>
      {actions && <div className="page-header-actions">{actions}</div>}
    </div>
  );
}

export function PageHeaderStat({ label, value }: { label: string; value: string | number }) {
  return (
    <div style={{ textAlign: "center" }}>
      <div className="number-display" style={{ fontSize: 20, fontWeight: 600, color: "var(--text)" }}>{value}</div>
      <div style={{ fontSize: 12, color: "var(--text-secondary)" }}>{label}</div>
    </div>
  );
}