export type FormRecordStatus =
  | "INITIAL"
  | "DRAFT"
  | "SUBMITTED"
  | "LOCKED"
  | "FROZEN"
  | "SIGNED";

export interface FormStatusConfig {
  status: FormRecordStatus;
  /**
   * Whether the form is locked by another user.
   * When true, all fields are disabled regardless of status.
   */
  lockedByOther?: boolean;
  /**
   * Whether this is an initial data entry (vs. review/edit).
   */
  isInitialEntry?: boolean;
}

/**
 * Derives the disabled state for a form field based on the record status.
 *
 * Fields are disabled when:
 * - The record is locked by another user
 * - The record status is LOCKED, FROZEN, or SIGNED (read-only)
 * - The record is SUBMITTED and this is not an initial entry
 */
export function isFieldDisabled(config: FormStatusConfig): boolean {
  if (config.lockedByOther) return true;

  switch (config.status) {
    case "LOCKED":
    case "FROZEN":
    case "SIGNED":
      return true;
    case "SUBMITTED":
      return !config.isInitialEntry;
    case "DRAFT":
    case "INITIAL":
      return false;
    default:
      return false;
  }
}
