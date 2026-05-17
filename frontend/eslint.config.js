import globals from "globals";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import tseslint from "typescript-eslint";

export default tseslint.config(
  { ignores: ["dist", "vite.config.d.ts", "vitest.config.ts"] },
  {
    extends: [
      ...tseslint.configs.strictTypeChecked,
      ...tseslint.configs.stylisticTypeChecked,
    ],
    files: ["**/*.{ts,tsx}"],
    languageOptions: {
      ecmaVersion: 2022,
      globals: globals.browser,
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
      },
    },
    plugins: {
      "react-hooks": reactHooks,
      "react-refresh": reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      "react-refresh/only-export-components": ["warn", { allowConstantExport: true }],
      "@typescript-eslint/no-unused-vars": ["error", { argsIgnorePattern: "^_" }],
      "@typescript-eslint/restrict-template-expressions": ["error", { allowNumber: true }],
      // UI event handlers often return void from navigate() calls
      "@typescript-eslint/no-confusing-void-expression": "off",
      // antd message/notification APIs return promises used in void contexts
      "@typescript-eslint/no-floating-promises": "off",
      // React event handlers (onClick, etc.) accept async functions
      "@typescript-eslint/no-misused-promises": ["error", { checksVoidReturn: false }],
      // import.meta.env values are typed as any by Vite
      "@typescript-eslint/no-unsafe-assignment": "off",
      "@typescript-eslint/no-unsafe-member-access": "off",
      "@typescript-eslint/no-unsafe-call": "off",
      "@typescript-eslint/no-unsafe-argument": "off",
      "@typescript-eslint/no-unsafe-return": "off",
      // Optional chaining on required properties is a defensive pattern in React
      "@typescript-eslint/no-unnecessary-condition": "off",
      // Allow any in specific well-known patterns (JWT payload, JSON.parse)
      "@typescript-eslint/no-explicit-any": "warn",
      // void type arg is needed for some no-data mutations
      "@typescript-eslint/no-invalid-void-type": "off",
      // Ant Design 5 migration: allow deprecated props temporarily
      "@typescript-eslint/no-deprecated": "warn",
      // Non-null assertions after early returns are safe
      "@typescript-eslint/no-non-null-assertion": "warn",
    },
  },
);
