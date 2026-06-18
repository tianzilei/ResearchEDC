# Phase 1 CRF Metadata Slice

**Status:** closed in the generated active inventory; 0 active `phase-1-crf-metadata` artifacts remain.
**Inventory source:** `docs/refactor/legacy-workflow-inventory.csv` (`phase_slice=phase-1-crf-metadata`).
**Slice ledger:** `docs/refactor/phase-1-crf-metadata-ledger.csv`.

## Scope

This slice records the CRF metadata boundary rows that were previously grouped together with active data-entry rendering fragments. The current generated active inventory is closed; this file is now a historical slice ledger.

Initial candidate groups:

| Group | Rows | Result |
|---|---:|---|
| CRF section viewer | 2 | Closed in generated active inventory after web/JSP/servlet deletion. |
| Active data-entry rendering | 9 | Closed in generated active inventory after web/JSP/servlet deletion. |
| Orphaned fragments | 2 | Already absent from the filesystem and dropped from regenerated active inventory. |

## Deletion Gate

No remaining file in this slice may be deleted until all checks are true for that file's workflow:

1. SPA/API CRF preview or data-entry route is the default navigation path.
2. Legacy route either redirects to SPA or is no longer registered in `web.xml`, Spring MVC config, or any Boot servlet initializer.
3. Permissions match the relevant `SecureController.mayProceed()` behavior.
4. CRF section, repeating group, item rendering, print, lock/unlock, and discrepancy-note behavior has parity proof.
5. JSP include/tag/helper references are gone.
6. Targeted backend tests and relevant frontend checks pass.

## Ledger Result

The ledger started with 13 rows from the stale active inventory. After regenerating the inventory against the current tree, two orphaned JSP rows are no longer active: `eventCrfLayer.jsp` and `showFixedItemInput.jsp`.

| Status | Count | Meaning |
|---|---:|---|
| `deleted` | 2 | Files are absent and no longer appear in generated active inventory. |
| `closed` | 11 | Former live CRF/data-entry dependencies no longer appear in generated active inventory. |

## Former Blockers

- `viewSectionDataEntry.jsp` and `viewSectionDataEntryHtml.jsp` were tied to legacy section viewing and print behavior.
- `showItemInput*`, `showGroupItemInput*`, `generate*`, and `showSection.jsp` were item/repeating-group rendering dependencies.
- `CheckCRFLocked` was registered in `web.xml` and called by `interviewer.jsp`.

## Result

This slice is closed in the generated active inventory. The current inventory records 0 active `phase-1-crf-metadata` artifacts and 0 active artifacts overall.
