# Phase 1 CRF Metadata Slice

**Status:** partially reconciled; generated inventory still has 11 active `phase-1-crf-metadata` artifacts.
**Inventory source:** `docs/refactor/legacy-workflow-inventory.csv` (`phase_slice=phase-1-crf-metadata`).
**Slice ledger:** `docs/refactor/phase-1-crf-metadata-ledger.csv`.

## Scope

This slice covers the CRF metadata boundary rows that were previously grouped together with active data-entry rendering fragments. The purpose of this pass was to separate true CRF section viewing, active data-entry dependencies, and compatibility-sensitive print/import fragments before further deletion.

Initial candidate groups:

| Group | Rows | Result |
|---|---:|---|
| CRF section viewer | 2 | Blocked by `ViewSectionDataEntryServlet`, monitor input fragments, and print compatibility dependencies. |
| Active data-entry rendering | 9 | Blocked by active JSP includes from `initialDataEntry.jsp`, `initialDataEntryNw.jsp`, `administrativeEditing.jsp`, `doubleDataEntry.jsp`, `viewSectionDataEntry.jsp`, and `interviewer.jsp`. |
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
| `blocked` | 11 | Live CRF/data-entry dependencies remain referenced by JSPs, `web.xml`, or legacy servlets. |

## Remaining Blockers

- `viewSectionDataEntry.jsp` and `viewSectionDataEntryHtml.jsp` remain tied to legacy section viewing and print behavior.
- `showItemInput*`, `showGroupItemInput*`, `generate*`, and `showSection.jsp` remain active item/repeating-group rendering dependencies.
- `CheckCRFLocked` is still registered in `web.xml` and called by `interviewer.jsp`.

## Result

This slice is open but narrowed. The generated inventory now records 11 active `phase-1-crf-metadata` artifacts, down from the stale 13-row candidate list. Further deletion is blocked on SPA/module CRF rendering, print, and lock/unlock parity.
