import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

import enTranslation from "@/locales/en/translation.json";
import zhTranslation from "@/locales/zh/translation.json";

export const SUPPORTED_LANGUAGES = [
  { key: "en", label: "English" },
  { key: "zh", label: "中文" },
] as const;

export type SupportedLocale = (typeof SUPPORTED_LANGUAGES)[number]["key"];

void i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: enTranslation },
      zh: { translation: zhTranslation },
    },
    fallbackLng: "en",
    detection: {
      order: ["localStorage", "navigator"],
      lookupLocalStorage: "oc_i18n_lang",
      caches: ["localStorage"],
    },
    interpolation: {
      escapeValue: false,
    },
    returnObjects: false,
  });

export default i18n;
