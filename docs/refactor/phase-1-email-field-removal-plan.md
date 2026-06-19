# Phase 1 Follow-up: Email Field Removal

**Created:** 2026-06-09
**Updated:** 2026-06-19
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

- `app/module/identity/entity/UserAccountEntity.email`
- `shared/domain/user/UserAccount.email`
- `app/module/study/entity/StudyEntity.facilityContactEmail`
- `shared/domain/datamap/Study.facilityContactEmail`

These are compatibility/schema mappings, not active product requirements.

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
- New product-side user creation writes an empty compatibility `email` value.
- New product-side study creation writes an empty compatibility `facilityContactEmail` value.
- Questionnaire service auth principal no longer stores or surfaces an `email` claim.
- Shared legacy user/study entities now default compatibility email fields to empty strings, and `StudyDaoAdapter` neutralizes the legacy study-contact email column on ingress.
- Module-owned user/study entity setters now neutralize attempted compatibility email writes at the write boundary.
- Shared legacy user/study setters now neutralize attempted compatibility email writes, and the shared `Study` constructor no longer preserves a legacy facility-contact email payload.

### Slice E3: Contract And Schema Cleanup

**Goal:** remove remaining email-specific storage/contract surface only after downstream compatibility review.

- Review ODM/export reliance on `FacilityContactEmail`.
- Review rule schema/docs that still mention `EmailAction`.
- Review migration-era structures such as `dc_send_email_event` and `rule_action.email_subject`.
- Only then version or remove schema/contract surface.

Current progress:
- Rule import template guidance now marks `EmailAction` as compatibility-only, not an active ResearchEDC feature.
- Unreferenced legacy runtime i18n email labels and retired Enterprise wording have been deleted or neutralized in runtime bundles.
- ODM and rule XSD contracts now annotate `FacilityContactEmail` and `EmailAction` as compatibility-only legacy surface.

Current blockers:
- ODM/XSD contracts still structurally define `OpenClinica:FacilityContactEmail`.
- Rule XSD contracts still structurally define `EmailAction`.
- Historical Liquibase structures still contain `dc_send_email_event` and `rule_action.email_subject`.

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
