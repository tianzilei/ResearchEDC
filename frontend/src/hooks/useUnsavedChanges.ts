import { useEffect } from "react";

/**
 * Warns users before navigating away from a page with unsaved changes via
 * the browser's native beforeunload event (tab close, refresh, external navigation).
 * React Router in-app navigation blocking will be added in Phase 4.
 * When `isDirty` is false, no blocking occurs and all listeners are cleaned up.
 */
export function useUnsavedChanges(isDirty: boolean): void {
  useEffect(() => {
    if (!isDirty) return;

    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();
      event.returnValue = "";
    };

    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [isDirty]);
}
