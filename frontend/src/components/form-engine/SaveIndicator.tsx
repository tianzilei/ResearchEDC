import { Typography } from "antd";

const { Text } = Typography;

type SaveStatus = "idle" | "saving" | "saved" | "error";

interface SaveIndicatorProps {
  status: SaveStatus;
}

export function SaveIndicator({ status }: SaveIndicatorProps) {
  if (status === "idle") return null;

  const config = {
    saving: { text: "Saving...", color: "var(--text-muted)" },
    saved: { text: "Saved", color: "var(--success)" },
    error: { text: "Save failed", color: "var(--danger)" },
  }[status];

  return (
    <Text style={{ color: config.color, fontSize: 13 }}>
      {config.text}
    </Text>
  );
}
