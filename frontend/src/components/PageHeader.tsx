import type { ReactNode } from "react";
import { Typography } from "antd";

const { Text } = Typography;

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
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
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
      <div className="number-display" style={{ fontSize: 22, color: "#1A1D23" }}>{value}</div>
      <Text style={{ fontSize: 12, color: "#9CA3AF", letterSpacing: "0.02em" }}>{label}</Text>
    </div>
  );
}
