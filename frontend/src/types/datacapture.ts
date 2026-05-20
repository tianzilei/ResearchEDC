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
  label: string;
  ordinal: number;
  items: number[];
}
