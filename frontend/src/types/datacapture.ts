/**
 * Data Capture types matching backend DTOs:
 * - ItemDataDTO
 * - SaveItemDataRequest
 * - BatchSaveItemsRequest
 * - ResponseSetDTO
 * - ItemGroupDTO
 */

export interface ItemDataDTO {
  itemDataId: number | null;
  itemId: number;
  eventCrfId: number;
  value: string;
  ordinal: number | null;
  statusId: number | null;
  deleted: boolean | null;
  dateCreated: string | null;
  dateUpdated: string | null;
}

export interface SaveItemDataRequest {
  eventCrfId: number;
  itemId: number;
  value: string;
  statusId?: number;
  ordinal?: number;
}

export interface BatchSaveItemsRequest {
  eventCrfId: number;
  items: SaveItemDataRequest[];
}

export interface ResponseSetOption {
  text: string;
  value: string;
}

export interface ResponseSetDTO {
  responseSetId: number;
  responseTypeId: number;
  label: string;
  options: ResponseSetOption[];
}

export interface ItemGroupDTO {
  itemGroupId: number;
  crfId: number;
  name: string;
  ocOid: string;
  items: number[];
}

export interface ScdRule {
  targetItemId: number;
  controlItemId: number;
  controlItemName: string;
  optionValue: string;
  message: string;
}

export interface RuleInfo {
  ruleName: string;
  ruleDescription: string;
  expressionValue: string;
  enabled: boolean;
}

export interface RuleEvalResponse {
  eventCrfId: number;
  ruleSetCount: number;
  rules: RuleInfo[];
}
