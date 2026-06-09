import { useRef, useEffect, useCallback } from "react";

export interface AutoSaveState {
  status: "idle" | "saving" | "saved" | "error";
  lastSaved: Date | null;
  error: string | null;
}

interface UseAutoSaveOptions<T> {
  data: T;
  onSave: (data: T) => Promise<void>;
  delay?: number;
  enabled?: boolean;
}

export function useAutoSave<T>({
  data,
  onSave,
  delay = 2000,
  enabled = true,
}: UseAutoSaveOptions<T>) {
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const dataRef = useRef(data);
  const statusRef = useRef<AutoSaveState["status"]>("idle");
  const lastSavedRef = useRef<Date | null>(null);
  const errorRef = useRef<string | null>(null);
  const savedDataRef = useRef<T>(data);

  dataRef.current = data;

  const isDirty =
    JSON.stringify(data) !== JSON.stringify(savedDataRef.current);

  const save = useCallback(async () => {
    statusRef.current = "saving";
    const snapshot = dataRef.current;
    try {
      await onSave(snapshot);
      statusRef.current = "saved";
      lastSavedRef.current = new Date();
      errorRef.current = null;
      savedDataRef.current = snapshot;
    } catch (e: unknown) {
      statusRef.current = "error";
      errorRef.current = e instanceof Error ? e.message : "Save failed";
    }
  }, [onSave]);

  useEffect(() => {
    if (!enabled) return;

    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }

    if (isDirty) {
      timerRef.current = setTimeout(() => {
        void save();
      }, delay);
    }

    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, [data, delay, enabled, save, isDirty]);

  const flush = useCallback(async () => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
    await save();
  }, [save]);

  const getState = useCallback((): AutoSaveState => ({
    status: statusRef.current,
    lastSaved: lastSavedRef.current,
    error: errorRef.current,
  }), []);

  return { flush, getState, isDirty };
}
