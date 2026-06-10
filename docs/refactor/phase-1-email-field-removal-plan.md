# Phase 1 Follow-up: Email Field Removal

**Created:** 2026-06-09
**Status:** Product-facing request/contact and email-field paths retired on 2026-06-10; compatibility mappings remain.
**Scope:** Remove email-dependent user account and study-contact fields after mail delivery and Enterprise surfaces are removed.

## Goal

ResearchEDC no longer sends email and no longer exposes Enterprise support/contact workflows. The next removal slice should remove email as a product data requirement from user accounts, account request flows, study contact metadata, frontend forms, and legacy JSP job forms.

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

## Exit Gates

1. Email/contact-field references are either removed or explicitly documented as compatibility references.
2. mvn -pl app -am compile -DskipTests passes.
3. mvn test -pl app -am passes.
4. cd frontend && pnpm typecheck && pnpm test --run passes.
5. Legacy guardrails pass.
