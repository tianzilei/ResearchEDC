import type { FormRecordStatus } from "@/components/form-engine/FormStatus";

export const STATUS_MAP: Record<number, FormRecordStatus> = {
  1: "INITIAL",
  2: "DRAFT",
  3: "SUBMITTED",
  4: "LOCKED",
  5: "FROZEN",
  6: "SIGNED",
};

export const STATUS_CLASSES: Record<string, string> = {
  INITIAL: "status-default",
  DRAFT: "status-info",
  SUBMITTED: "status-success",
  LOCKED: "status-warning",
  FROZEN: "status-default",
  SIGNED: "status-success",
};

export function deriveRecordStatus(statusId: number): FormRecordStatus {
  return STATUS_MAP[statusId] ?? "INITIAL";
}

export function statusClassName(recordStatus: FormRecordStatus): string {
  return STATUS_CLASSES[recordStatus] ?? "status-default";
}
