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
  /**
   * Whether this is a double data entry (second person entering same data to verify).
   */
  isDoubleEntry?: boolean;
  /**
   * Original values from the first data entry, used to detect discrepancies.
   * Keys should match FormItemConfig.name pattern.
   */
  originalValues?: Record<string, string>;
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

/**
 * Checks whether a field's current value differs from the original entry
 * in double-data-entry mode. Returns false if not in double-entry mode
 * or if the field has no original value recorded.
 */
export function hasDoubleEntryDiscrepancy(
  fieldName: string,
  currentValue: string,
  config: FormStatusConfig,
): boolean {
  if (!config.isDoubleEntry || !config.originalValues) return false;
  const original = config.originalValues[fieldName];
  return original !== undefined && original !== currentValue;
}
