# Phase 1 Follow-up: Email Field Removal

**Created:** 2026-06-09
**Updated:** 2026-06-24
**Status:** Product-facing request/contact and email-field paths retired on 2026-06-10; compatibility mappings and legacy content residue remain.
**Scope:** Remove email-dependent user account and study-contact fields after mail delivery and Enterprise surfaces are removed.

## Goal

ResearchEDC no longer sends email and no longer exposes Enterprise support/contact workflows. The next removal slice should remove email as a product data requirement from user accounts, account request flows, study contact metadata, frontend forms, and legacy JSP job forms.

## Current Audit

### Already Removed From Active Product Flows

- No frontend route or form now collects user email, request-account email, request-study email, or contact-form email.
- No active app-side mail-delivery workflow remains.
- Identity DTOs and profile/admin product APIs no longer expose email as an input or output field.
- Study create/update product flows no longer expose `facilityContactEmail` in SPA forms or REST request DTOs.

### Remaining Active Compatibility References

- Historical database columns `user_account.email` and `module_user_account.email`
- Historical database columns `study.facility_contact_email` and `module_study.facility_contact_email`
- ODM/XSD `OpenClinica:FacilityContactEmail`

These are schema/contract compatibility surfaces, not active Java product requirements. The app/shared Java entity APIs no longer expose the retired email fields.

### Remaining Content / Historical Residue

- i18n bundle text still contains retired Enterprise branding and email-request wording
- licensing/footer text still mentions Enterprise support
- historical notes still describe Enterprise service packages
- migration XML, trigger SQL, ODM/XSD schemas, and rule schemas still contain email-era columns/elements

## Planned Slices

### Slice E1: Content Cleanup

**Goal:** remove still-visible Enterprise and email-support wording without touching schema compatibility.

- Replace Enterprise marketing text in i18n bundles with neutral retired wording.
- Remove stale mail/Enterprise wording from licensing/footer text.
- Keep the same resource keys for compatibility, but stop advertising retired capabilities.

Current progress:
- Runtime i18n bundles no longer expose the unreferenced `confirm_email`, `contact_email`, or `your_email` labels.
- Rule-management notes and footer/licensing tooltips now describe supported in-app capabilities and neutral vendor-service retirement wording instead of advertising Enterprise or email-action workflows.

### Slice E2: Compatibility Write Neutralization

**Goal:** ensure compatibility fields remain non-product, non-required, and inert.

- Verify user creation/update paths never require or surface email.
- Verify study creation/update paths never require or surface `facilityContactEmail`.
- Where helpful, explicitly write neutral compatibility values (`NULL` or empty string by contract) instead of accidental user-supplied content.

Current progress:
- New product-side user creation no longer maps the retired compatibility `email` value in Java.
- New product-side study creation no longer maps the retired compatibility `facilityContactEmail` value in Java.
- Questionnaire service auth principal no longer stores or surfaces an `email` claim.
- Shared legacy user/study entities now default compatibility email fields to empty strings, and `StudyDaoAdapter` neutralizes the legacy study-contact email column on ingress.
- Module-owned and shared user/study entities no longer expose retired email setters/getters.
- The shared `Study` constructor accepts the historical positional argument for binary/source compatibility but no longer preserves a legacy facility-contact email payload.
- PostgreSQL write-boundary triggers now force retained `user_account.email`, `module_user_account.email`, `study.facility_contact_email`, and `module_study.facility_contact_email` compatibility columns to empty strings before Phase B sync propagates writes.
- App module and shared Hibernate entities no longer map or expose retired user/study email compatibility fields; neutralization is now handled at the database boundary.

### Slice E3: Contract And Schema Cleanup

**Goal:** remove remaining email-specific storage/contract surface only after downstream compatibility review.

- Review ODM/export reliance on `FacilityContactEmail`.
- Verify rule schema/docs no longer expose `EmailAction`.
- Review migration-era structures such as `dc_send_email_event` and `rule_action.email_subject`.
- Only then version or remove schema/contract surface.

Current progress:
- Rule import template guidance first marked `EmailAction` as compatibility-only, and the runtime rule XSD now removes that action from the accepted contract.
- Unreferenced legacy runtime i18n email labels and retired Enterprise wording have been deleted or neutralized in runtime bundles.
- ODM XSD contracts now annotate `FacilityContactEmail` as deprecated legacy surface with explicit removal intent and write-neutralization documentation.
- New 3.18 forward migration retires the historical `dc_send_email_event` table, its legacy Oracle sequence, and the old `rule_action.email_to` / `rule_action.email_subject` storage columns after source scans confirmed no active Java/runtime mappings.
- Runtime rule XSD no longer defines `EmailAction` or `EmailActionType`; source scans confirmed no active app/shared Java callers or rule import wiring still depend on that legacy action structure.
- `RetiredEmailSchemaCleanupTest` now locks the ODM XSD deprecation annotation, preventing accidental softening of the compatibility boundary.

Current blockers:
- ODM/XSD contracts still structurally define `OpenClinica:FacilityContactEmail` (deprecated, annotated, will be removed in a future version).
- Historical pre-3.18 Liquibase files still document the original email-era table/columns, but forward migrations now retire the runtime storage surface.

## In Scope

- User account/profile DTOs and frontend forms: remove email input/display from profile and admin user management where it is no longer required.
- Request/contact SPA pages: remove or retire request-account, request-study, and contact flows if they only collect email-backed requests.
- Study contact metadata: remove facility contact email from create/update study DTOs and React study wizard/editor.
- Export/import job forms: remove contactEmail fields and JobDataMap entries; report job status in-app only.
- ODM/export compatibility: remove OpenClinica:FacilityContactEmail only after downstream contract review, or version the export contract.
- Database/schema: stop writing email-only columns before dropping legacy columns.

## Out of Scope For Current Slice

- Dropping database columns immediately.
- Removing generic email strings from i18n bundles before code references are gone.
- Removing identity fields still used by authentication, audit, or external API contracts.
- Rewriting historical Liquibase migrations.
- Removing ODM/XSD or rule-schema email elements before contract review.

## Exit Gates

1. Email/contact-field references are either removed or explicitly documented as compatibility references.
2. mvn -pl app -am compile -DskipTests passes.
3. mvn test -pl app -am passes.
4. cd frontend && pnpm typecheck && pnpm test --run passes.
5. Legacy guardrails pass.
