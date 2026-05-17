import type { ThemeConfig } from "antd";

const theme: ThemeConfig = {
  token: {
    colorPrimary: "#099A87",
    colorPrimaryHover: "#07B89A",
    colorPrimaryActive: "#087A6C",
    colorBgContainer: "#F8F5F0",
    colorBgLayout: "#EFEBE4",
    colorBgElevated: "#FFFFFF",
    colorText: "#1A1D23",
    colorTextSecondary: "#6B7280",
    colorBorder: "#E5E0D8",
    colorBorderSecondary: "#EDE8E0",
    borderRadius: 8,
    borderRadiusLG: 12,
    fontFamily:
      "'DM Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
    fontSize: 14,
    fontSizeHeading1: 28,
    fontSizeHeading2: 22,
    fontSizeHeading3: 18,
    fontSizeHeading4: 16,
    fontWeightStrong: 600,
    boxShadow:
      "0 1px 3px rgba(15,26,46,0.08), 0 1px 2px rgba(15,26,46,0.06)",
    boxShadowSecondary: "0 4px 12px rgba(15,26,46,0.10)",
  },
  components: {
    Layout: {
      headerBg: "#0F1A2E",
      headerHeight: 60,
      bodyBg: "#EFEBE4",
      siderBg: "#F8F5F0",
    },
    Menu: {
      itemBg: "transparent",
      itemBorderRadius: 8,
      itemColor: "#6B7280",
      itemHoverColor: "#099A87",
      itemHoverBg: "rgba(9,154,135,0.06)",
      itemSelectedColor: "#099A87",
      itemSelectedBg: "rgba(9,154,135,0.10)",
      itemActiveBg: "rgba(9,154,135,0.12)",
      itemMarginInline: 8,
      itemHeight: 42,
    },
    Card: {
      paddingLG: 24,
      borderRadiusLG: 12,
      boxShadow: "0 1px 3px rgba(15,26,46,0.06)",
      boxShadowSecondary: "0 4px 12px rgba(15,26,46,0.08)",
    },
    Table: {
      headerBg: "#F8F5F0",
      headerBorderRadius: 8,
      rowHoverBg: "rgba(9,154,135,0.04)",
      borderColor: "#EDE8E0",
    },
    Button: {
      primaryShadow: "0 2px 6px rgba(9,154,135,0.25)",
      borderRadiusLG: 10,
      borderRadiusSM: 6,
      borderRadius: 8,
      fontWeight: 500,
    },
    Tag: {
      borderRadius: 6,
    },
    Modal: {
      borderRadiusLG: 12,
      boxShadowSecondary: "0 8px 24px rgba(15,26,46,0.12)",
    },
    Input: {
      borderRadius: 8,
      activeBorderColor: "#099A87",
      hoverBorderColor: "#07B89A",
    },
    Select: {
      borderRadius: 8,
      optionSelectedBg: "rgba(9,154,135,0.10)",
    },
    Progress: {
      borderRadius: 6,
    },
    Alert: {
      borderRadiusLG: 10,
    },
    Dropdown: {
      borderRadius: 10,
    },
    Notification: {
      borderRadiusLG: 10,
    },
  },
};

export default theme;
