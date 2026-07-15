# ResearchEDC Final R&D Plan

**Created:** 2026-06-26  
**Status:** Module 10 complete
**Planning horizon:** 12 months (`2026-07` to `2027-06`)  
**Document role:** single retained R&D planning document

## 1. Purpose

This document is the single retained engineering plan for ResearchEDC.

It keeps only the R&D portion of the earlier planning work and removes:

- market analysis
- competitor summaries
- business packaging
- commercial positioning

It defines:

1. the engineering target architecture
2. the module families and dependency order
3. the implementation sequence
4. the detailed 12-month R&D plan
5. the definition of done and verification gates for each module

## 2. R&D Goal

Within the next 12 months, ResearchEDC should evolve from the current modular EDC baseline into a broader but still disciplined open-source platform by:

1. preserving the existing Core clinical-data backbone
2. adding shared platform foundations once
3. implementing new modules one by one in dependency order
4. keeping each module independently usable and testable
5. maintaining strict module boundaries during expansion

## 3. Current Technical Baseline

The current codebase already includes these stable foundations:

- Spring Boot modular monolith backend
- Spring Modulith boundaries in `org.researchedc.module.*`
- React SPA frontend
- questionnaire service for templates, assignments, responses, scoring, public fill, and export
- randomization module
- audit module
- export module
- identity, study, subject, event, datacapture, rule, dataset, filter, and discrepancy-note modules
- OpenRosa support

The current codebase also already has strong architectural constraints:

- module-level `allowedDependencies`
- anti-corruption adapters under `internal/adapter/`
- module-owned DTO and repository patterns
- no shared Java surface remaining in `shared/src/main/java`

## 4. Engineering Principles

### 4.1 Module-first implementation

Each module must become a complete workflow, not only a partial API surface.

### 4.2 Composition over coupling

New modules should collaborate through:

- explicit service APIs
- package-slice dependencies
- application events where appropriate
- narrow integration contracts

They should not bypass boundaries through convenience imports.

### 4.3 Shared foundations built once

The following concerns must not be re-implemented inside feature modules:

- task scheduling
- reminders
- token lifecycle
- participant access control
- integration sync state
- analytics event capture

### 4.4 Open-source maintainability

Each module should remain understandable and independently maintainable by open-source contributors.

### 4.5 Verification-first delivery

No module is considered complete without tests, docs, and activation/deactivation behavior.

## 5. Module Families

ResearchEDC should be developed through six module families.

### 5.1 Family A: Platform Foundations

- identity and authorization
- study/site/tenant context
- audit and compliance
- export and artifacts
- notification and task engine
- integration framework
- analytics event model

### 5.2 Family B: Core Clinical Data

- study management
- subject management
- event / visit management
- CRF metadata and form logic
- data capture
- discrepancy / query backbone
- randomization
- dataset / filter / reporting setup

### 5.3 Family C: Participant Modules

- participant identity and access
- eConsent
- eCOA / ePRO
- participant portal / task inbox

### 5.4 Family D: Site Operations Modules

- recruit / prescreen
- SDV / remote monitoring
- site performance analytics
- readiness / activation toolkit

### 5.5 Family E: Integration Modules

- FHIR / EHR connector
- external connector pack
- API / webhook automation toolkit

### 5.6 Family F: Enablement Modules

- study build toolkit
- implementation toolkit
- training/admin toolkit

## 6. Dependency Stack

Development must follow this dependency stack.

### Tier 0: existing baseline

- identity
- study
- subject
- event
- datacapture
- crf
- audit
- export
- randomization
- rule
- dataset/filter

### Tier 1: shared foundations

- notification and task engine
- participant identity and access foundation
- analytics event model
- integration framework

### Tier 2: participant workflows

- eCOA/ePRO
- eConsent
- participant portal

### Tier 3: site operations

- Recruit
- analytics control plane
- SDV

### Tier 4: strategic integration

- FHIR / EHR connector
- connector extensions

### Tier 5: later adjacencies

- coding
- supply / RTSM expansion
- deeper CTMS-adjacent modules

## 7. Final Implementation Order

The recommended implementation order is:

1. `Notification And Task Engine`
2. `Participant Identity And Access`
3. `eCOA/ePRO Productization`
4. `eConsent`
5. `Participant Portal / Task Inbox`
6. `Recruit / Prescreen`
7. `Analytics Control Plane`
8. `SDV / Remote Monitoring`
9. `FHIR / EHR Connector`
10. `Study Build Toolkit`

Reasoning:

- the first two unlock multiple later modules
- the next three create the first coherent new product surface
- the next three add operational depth
- the last two add strategic leverage and implementation speed

## 8. Detailed Module Plan

This section defines the engineering expectations for each planned module.

## 9. Module 1: Notification And Task Engine

**Status:** Complete as of 2026-07-07

### 9.1 Goal

Create a reusable platform service for scheduled work, reminders, and due-state tracking.

### 9.2 Responsibilities

- task definition model
- task instance model
- due/overdue/completed/expired states
- scheduler or trigger entry points
- reminder dispatch abstraction
- audit emission hooks

### 9.3 Backend scope

- new module package under `app/.../module/task` or equivalent
- entities for task templates and task instances
- service API for create, complete, cancel, expire, and query
- event hooks for study/subject/event-driven task generation
- reminder abstraction interface with no direct mail revival

### 9.4 Frontend scope

- minimal operator task list
- status rendering and filtering
- reusable task badge/count hook

### 9.5 Dependencies

- identity
- study/subject/event context
- audit

### 9.6 Exit gate

- tasks can be generated from a study or subject workflow
- due state transitions are correct
- at least one downstream module can consume it without local task reimplementation

### 9.7 Verification

Completed implementation:

- `task` Spring Modulith module with template and task-instance persistence
- task lifecycle states: `PENDING`, `DUE`, `OVERDUE`, `COMPLETED`,
  `CANCELLED`, and `EXPIRED`
- REST API under `/api/v1/tasks` for template listing/creation, task
  listing/creation, detail lookup, complete, cancel, overdue transition, and
  reminder dispatch
- service-level study read/write scoping through the current study access
  service
- audit hooks for template creation, task creation, completion, cancellation,
  and reminder dispatch
- reminder dispatch abstraction with a no-op provider, intentionally avoiding
  any retired mail-delivery path
- Liquibase schema for `task_template` and `task_instance`
- minimal SPA task inbox at `/app/tasks`, with study/mine filters, create,
  complete, cancel, and localized English/Chinese copy

Verification:

- `mvn test -pl app -am -Dtest=TaskServiceTest,CoreControllerAuthorizationTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  passed: 131 tests
- `corepack pnpm@11.1.2 -C frontend typecheck` passed

Remaining work moves to the next roadmap module or later task-engine expansion:
real reminder providers, scheduler wiring, and downstream automatic task
generation from participant-facing modules.

- unit tests for state transitions
- service tests for due/expire/complete logic
- controller tests for list and update endpoints
- frontend tests for task list rendering

## 10. Module 2: Participant Identity And Access

**Status:** Complete as of 2026-07-07

### 10.1 Goal

Create a reusable participant-facing access layer for future participant modules.

### 10.2 Responsibilities

- invitation issuance
- token lifecycle
- optional participant account model
- study-scoped participant permissions
- secure access to participant workflows

### 10.3 Backend scope

- participant identity entities
- token issuance and verification service
- study-subject linkage rules
- revocation/expiry support
- audit events for issue, use, revoke, expire

### 10.4 Frontend scope

- secure participant entry path
- token validation flow
- participant session bootstrap contract

### 10.5 Dependencies

- identity
- subject
- study
- audit

### 10.6 Exit gate

- participant can securely enter a scoped workflow
- expired/revoked access behaves safely
- downstream modules can rely on one shared participant access model

### 10.7 Verification

- token lifecycle tests
- permission tests
- controller tests for bootstrap/entry
- negative-path tests for expired and invalid links

Completed implementation:

- `participantaccess` Spring Modulith module with participant accounts linked to
  `module_study_subject`
- participant access-token persistence with hashed token storage, expiry,
  revocation, use tracking, and status lifecycle
- token issuance returns the raw token only once and stores only the hash
- public bootstrap endpoint under `/api/v1/participant-access/public/bootstrap`
  for participant entry validation
- secured operator endpoints under `/api/v1/participant-access` for account
  creation/listing, token issuance/listing, revocation, and expiry sweep
- service-level study read/write scoping through the current study access
  service
- audit hooks for account creation, token issuance, token use, token revocation,
  and token expiry
- Liquibase schema for `participant_account` and `participant_access_token`
- minimal SPA operator page at `/app/participants/access`
- participant entry route at `/participant/access/:token`

Verification:

- `mvn test -pl app -am -Dtest=ParticipantAccessServiceTest,CoreControllerAuthorizationTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  passed: 134 tests
- `corepack pnpm@11.1.2 -C frontend typecheck` passed

Remaining work moves to downstream participant modules: real participant portal
session state, eCOA/ePRO task consumption, eConsent flows, and optional
participant account credentials.

## 11. Module 3: eCOA / ePRO Productization

### 11.1 Goal

Turn the current questionnaire capability into a scheduled, participant-facing workflow rather than a disconnected service surface.

### 11.2 Responsibilities

- scheduled assignments
- due windows
- reminders
- scoring
- adherence tracking
- operator review and completion visibility

### 11.3 Backend scope

- integrate questionnaire assignments with the task engine
- add schedule model tied to study/event/subject
- record due state and completion metrics
- expose operator-facing adherence queries

### 11.4 Frontend scope

- participant completion flow polish
- operator adherence dashboard
- assignment detail and schedule visibility
- overdue indicators

### 11.5 Service scope

- questionnaire-service contract updates where needed
- score and status payload extensions
- webhook/event hook for completion metrics

### 11.6 Dependencies

- task engine
- participant identity
- questionnaire-service
- audit
- study/subject/event

### 11.7 Exit gate

- one study can schedule assignments automatically
- participants can complete tasks through the shared access model
- operator can see due/completed/overdue status

### 11.8 Verification

- questionnaire-service API tests
- integration tests for assignment generation
- frontend tests for participant and operator flows
- metrics/adherence query tests

Completed implementation:

- `ecoa` Spring Modulith module with schedule and assignment persistence
- study-scoped schedule creation tied to `studyId`, `studySubjectId`, optional
  `studyEventId`, questionnaire version ID, due date, and optional due window
- schedule creation resolves or creates a participant account, issues a
  participant access token, creates an eCOA assignment, and creates a linked task
  engine task with `ECOA_ASSIGNMENT` target type
- operator REST endpoints under `/api/v1/ecoa` for schedules, assignments,
  adherence summaries, completion recording, cancellation, and overdue expiry
- participant REST endpoints under `/api/v1/ecoa/public/**` scoped by the raw
  participant access token for listing and completing active eCOA assignments
- public security permit rules for participant bootstrap and eCOA participant
  assignment endpoints
- adherence summary counters for total, pending, in-progress, completed,
  overdue, and completion rate
- Liquibase schema for `ecoa_schedule` and `ecoa_assignment`
- SPA operator dashboard at `/app/questionnaires/ecoa` with schedule creation,
  one-time participant-link display, adherence metrics, schedule visibility,
  overdue/status indicators, and operator complete/cancel actions
- participant entry page now displays active eCOA assignments for the shared
  access token and supports participant completion through the same token

Verification:

- `mvn test -pl app -am -Dtest=EcoaServiceTest,ParticipantAccessServiceTest,TaskServiceTest,CoreControllerAuthorizationTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  passed: 160 tests
- `corepack pnpm@11.1.2 -C frontend typecheck` passed
- `corepack pnpm@11.1.2 -C frontend test --run` passed: 29 tests
- `xmllint --noout shared/src/main/resources/migration/3.18/2026-07-07-ecoa-tables.xml shared/src/main/resources/migration/3.18/release.xml`
  passed

Remaining work moves to later hardening: direct questionnaire-service callback
or webhook ingestion for scored payloads, richer participant questionnaire fill
embedding, and reminder dispatch beyond the task/reminder foundation.

## 12. Module 4: eConsent

### 12.1 Goal

Build a complete consent workflow with versioning, signing, countersigning, and signed artifact generation.

### 12.2 Responsibilities

- consent template/version model
- participant review/sign
- coordinator or investigator countersign
- re-consent support
- signed artifact storage/export
- audit trail

### 12.3 Backend scope

- consent entities and versioning rules
- signature evidence model
- countersign workflow
- signed PDF or artifact generation pipeline
- re-consent trigger service

### 12.4 Frontend scope

- consent viewer
- signature steps
- operator review/status view
- re-consent queue/status

### 12.5 Dependencies

- participant identity
- task engine
- audit
- export/artifact framework
- study/subject

### 12.6 Exit gate

- one subject can complete a consent end-to-end
- signed artifact is retrievable
- consent version and status are visible to operators

### 12.7 Verification

- versioning tests
- artifact generation tests
- signature workflow tests
- frontend tests for review/sign/status paths

Completed implementation:

- `econsent` Spring Modulith module with consent templates, versions, and
  assignments
- consent version lifecycle with draft and published states
- participant assignment workflow that resolves or creates a participant
  account, issues a participant access token, and creates a linked task engine
  task with `CONSENT_ASSIGNMENT` target type
- participant-token-scoped public endpoints under `/api/v1/econsent/public/**`
  for listing active consent tasks and submitting participant signatures
- operator endpoints under `/api/v1/econsent` for templates, versions,
  publishing, assignment, countersignature, assignment status, and artifact
  retrieval
- signature evidence capture for participant name, participant signature,
  browser evidence, signature timestamp, countersignature, countersigner, and
  countersign timestamp
- signed artifact generation as a retrievable UTF-8 text artifact stored with
  the consent assignment
- audit hooks for template creation, version creation/publish, assignment,
  participant signature, and countersignature/artifact generation
- Liquibase schema for `consent_template`, `consent_version`, and
  `consent_assignment`
- SPA operator page at `/app/questionnaires/econsent` with template/version
  management, publish, assignment, participant link display, status view,
  countersignature, and artifact view
- participant entry page now displays active consent tasks and supports
  participant review/sign through the shared access token

Verification:

- `mvn test -pl app -am -Dtest=EconsentServiceTest,ParticipantAccessServiceTest,TaskServiceTest,CoreControllerAuthorizationTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  passed: 171 tests
- `corepack pnpm@11.1.2 -C frontend typecheck` passed
- `corepack pnpm@11.1.2 -C frontend test --run` passed: 29 tests
- `xmllint --noout shared/src/main/resources/migration/3.18/2026-07-07-econsent-tables.xml shared/src/main/resources/migration/3.18/release.xml`
  passed

Remaining work moves to later hardening: PDF rendering/storage integration,
formal re-consent queue automation, richer identity-proofing evidence, and
artifact export bundling.

## 13. Module 5: Participant Portal / Task Inbox

**Status:** Complete as of 2026-07-15

### 13.1 Goal

Provide one coherent participant-facing shell for tasks and statuses.

### 13.2 Responsibilities

- unified participant landing page
- task list
- due status
- consent status
- questionnaire status

### 13.3 Backend scope

- participant bootstrap endpoint
- aggregated task/status DTOs
- module-aware task grouping

### 13.4 Frontend scope

- participant dashboard
- task detail routing
- status cards
- mobile-first layout

### 13.5 Dependencies

- participant identity
- task engine
- eCOA/ePRO
- eConsent

### 13.6 Exit gate

- participant can see all assigned work in one place
- navigation between participant modules is coherent

### 13.7 Verification

Completed implementation:

- `participantportal` Spring Modulith module with a public token-scoped
  bootstrap API under `/api/v1/participant-portal/public/bootstrap`
- aggregated participant summary DTO for total, actionable, questionnaire,
  consent, and overdue task counts
- module-aware task DTOs that group eCOA/ePRO assignments and eConsent
  assignments into one due-date ordered participant task list
- service composition through the shared participant access token contract and
  existing eCOA/eConsent public action endpoints
- public security permit rule for the participant portal bootstrap endpoint
- SPA participant entry page at `/participant/access/:token` now renders a
  unified participant portal shell with summary cards, a single task inbox,
  consent body preview/signing, and questionnaire completion actions
- localized English/Chinese copy for the participant portal task inbox

Verification:

- `mvn test -pl app -am -Dtest=ParticipantPortalServiceTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  passed: 3 tests
- `corepack pnpm@11.1.2 -C frontend typecheck` passed

- bootstrap aggregation tests
- frontend navigation tests
- mobile rendering regression checks

Remaining work moves to later participant hardening: richer participant session
state, deeper questionnaire-service fill embedding, formal accessibility/mobile
visual regression checks, and participant account credentials.

## 14. Module 6: Recruit / Prescreen

**Status:** Complete as of 2026-07-15

### 14.1 Goal

Create an early-stage candidate intake and conversion workflow.

### 14.2 Responsibilities

- public prescreen forms
- eligibility review
- candidate queue
- candidate-to-subject conversion

### 14.3 Backend scope

- candidate entities
- prescreen result model
- rule-based or checklist eligibility engine
- conversion service into subject workflows

### 14.4 Frontend scope

- public prescreen entry flow
- coordinator candidate queue
- conversion and rejection actions

### 14.5 Dependencies

- participant identity foundation where needed
- subject
- study
- rule
- audit

### 14.6 Exit gate

- coordinator can review candidate records
- eligible candidate can be converted into subject records through a supported path

### 14.7 Verification

Completed implementation:

- `recruit` Spring Modulith module with candidate and prescreen-result
  persistence
- secured REST API under `/api/v1/recruit` for candidate list/create/detail,
  prescreen recording, rejection, and conversion
- candidate lifecycle states: `NEW`, `PRESCREENED`, `ELIGIBLE`,
  `INELIGIBLE`, `CONVERTED`, and `REJECTED`
- prescreen decisions: `ELIGIBLE`, `INELIGIBLE`, and `NEEDS_REVIEW`, with score
  and criteria/review notes
- conversion service that turns an eligible candidate into the existing
  subject/enrollment workflow through the subject module service contract
- audit hooks for candidate creation, prescreen decision, rejection, and
  conversion
- Liquibase schema for `module_recruit_candidate` and
  `module_prescreen_result`
- SPA recruit queue at `/app/recruit` with candidate creation, status filter,
  prescreen, rejection, and conversion actions
- localized English/Chinese recruit queue copy

Verification:

- `mvn test -pl app -am -Dtest=RecruitServiceTest,ParticipantPortalServiceTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  passed: 9 tests
- `corepack pnpm@11.1.2 -C frontend typecheck` passed
- `xmllint --noout shared/src/main/resources/migration/3.18/2026-07-15-recruit-tables.xml shared/src/main/resources/migration/3.18/release.xml`
  passed

- conversion tests
- public form tests
- queue filtering tests

Remaining work moves to later recruit hardening: public prescreen form hosting,
rule/checklist-driven eligibility scoring, duplicate candidate detection, and
candidate communication hooks.

## 15. Module 7: Analytics Control Plane

**Status:** Complete as of 2026-07-15

### 15.1 Goal

Provide in-product operational visibility across participant and site workflows.

### 15.2 Responsibilities

- enrollment metrics
- adherence metrics
- backlog/issue metrics
- site performance metrics
- monitor queue metrics

### 15.3 Backend scope

- analytics event schema
- aggregation jobs or services
- permission-aware read APIs

### 15.4 Frontend scope

- dashboards for operators
- drill-down lists
- filters by study/site/status/time range

### 15.5 Dependencies

- analytics event model
- study/subject/event
- eCOA/ePRO
- Recruit
- SDV
- discrepancy/audit

### 15.6 Exit gate

- key operational metrics are visible without external export
- dashboards remain permission-scoped and performant enough for daily use

### 15.7 Verification

Completed implementation:

- `analytics` Spring Modulith module with a study-scoped read API under
  `/api/v1/analytics/dashboard`
- permission-aware dashboard aggregation through the current study access
  service
- enrollment metrics for total enrolled, active subjects, and subjects created
  in the last 30 days
- participant-work metrics from eCOA adherence and recruit candidate state
- operations metrics from the existing dashboard task summary
- SPA analytics dashboard at `/app/analytics` with grouped metric cards for
  enrollment, participant work, and operations
- localized English/Chinese analytics navigation and metric copy

Verification:

- `mvn test -pl app -am -Dtest=AnalyticsServiceTest,RecruitServiceTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  passed: 9 tests
- `corepack pnpm@11.1.2 -C frontend typecheck` passed

- aggregate calculation tests
- dashboard API tests
- frontend visualization tests

Remaining work moves to later analytics hardening: persistent analytics event
capture, trend windows, site-level drilldowns, charting, and performance-focused
read models for large studies.

## 16. Module 8: SDV / Remote Monitoring

**Status:** Complete as of 2026-07-15

### 16.1 Goal

Create a monitor-facing verification workflow.

### 16.2 Responsibilities

- SDV queue
- field/form/visit verification status
- review workspace
- unresolved issue tracking

### 16.3 Backend scope

- SDV state model
- per-entity verification service
- monitor query/read models
- discrepancy linkages

### 16.4 Frontend scope

- monitor queue page
- SDV detail/review views
- aging and status indicators

### 16.5 Dependencies

- event
- datacapture
- discrepancy backbone
- audit
- analytics

### 16.6 Exit gate

- monitor can review and update SDV state through supported flows
- unresolved work is visible and queryable

### 16.7 Verification

Completed implementation:

- `sdv` Spring Modulith module with event-CRF SDV review persistence
- secured REST API under `/api/v1/sdv` for study-scoped review queues,
  event-CRF review creation, and SDV status updates
- SDV lifecycle states: `PENDING`, `VERIFIED`, and `REQUIRES_CHANGES`
- event-CRF compatibility update that writes `sdv_status`, `sdv_update_id`, and
  `date_updated` when reviews are completed
- service-level study scoping through event CRF -> study subject -> study
  resolution
- audit hooks for SDV queue creation and status updates
- Liquibase schema for `module_sdv_review`
- SPA SDV queue at `/app/sdv` with status filter, manual event-CRF queueing,
  review notes, and verify/requires-changes actions
- localized English/Chinese SDV navigation and workflow copy

Verification:

- `mvn test -pl app -am -Dtest=SdvServiceTest,AnalyticsServiceTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  passed: 7 tests
- `corepack pnpm@11.1.2 -C frontend typecheck` passed
- `xmllint --noout shared/src/main/resources/migration/3.18/2026-07-15-sdv-tables.xml shared/src/main/resources/migration/3.18/release.xml`
  passed

- SDV state transition tests
- queue/filter tests
- frontend review workflow tests

Remaining work moves to later SDV hardening: field/item-level SDV flags,
monitor worklists generated automatically from completed CRFs, discrepancy
linkage, and richer remote review workspace.

## 17. Module 9: FHIR / EHR Connector

### 17.1 Goal

Build the first narrow interoperability module with safe provenance and reconciliation.

### 17.2 Responsibilities

- connector config
- resource ingestion
- mapping to subject/event/data structures
- provenance markers
- reconciliation workflow

### 17.3 Backend scope

- connector entities and auth config
- import pipeline for `Patient`, `Encounter`, and selected `Observation`
- mapping service
- error/retry state model

### 17.4 Frontend scope

- connector configuration UI
- import review queue
- reconciliation and error views

### 17.5 Dependencies

- integration framework
- subject
- event
- datacapture
- audit

### 17.6 Exit gate

- at least one narrow import path works end-to-end
- imported records carry provenance
- failures are visible and recoverable without DB intervention

### 17.7 Verification

Implemented on 2026-07-15:

- backend `fhir` Modulith module with connector configuration, FHIR `Patient`
  JSON ingestion, mapping to external ID / subject identifier / gender fields,
  import status tracking, reconciliation, and audit records
- Liquibase table migration for `module_fhir_connector` and
  `module_fhir_import_record`
- SPA FHIR dashboard at `/app/fhir` with connector creation, Patient JSON
  submission, status filtering, and reconciliation workflow
- localized English/Chinese navigation and workflow copy

Verification:

- `mvn test -pl app -am -Dtest=FhirServiceTest,SdvServiceTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  passed: 10 tests
- `corepack pnpm@11.1.2 -C frontend typecheck` passed
- `xmllint --noout shared/src/main/resources/migration/3.18/2026-07-15-fhir-tables.xml shared/src/main/resources/migration/3.18/release.xml`
  passed

Remaining work moves to later interoperability hardening: Encounter and
Observation ingestion, subject/event/data capture reconciliation, connector
retry semantics, richer provenance display, and external system authentication.

## 18. Module 10: Study Build Toolkit

### 18.1 Goal

Reduce configuration friction for repeated study setup.

### 18.2 Responsibilities

- study templates
- guided setup flows
- amendment-friendly configuration helpers
- reusable module defaults

### 18.3 Backend scope

- template model
- study bootstrap service
- versioned template application rules

### 18.4 Frontend scope

- guided creation wizard improvements
- template selection
- amendment assistance surfaces

### 18.5 Dependencies

- study
- crf
- rule
- randomization
- participant modules where applicable

### 18.6 Exit gate

- one new study can be initialized from reusable defaults with less manual setup than today

### 18.7 Verification

Implemented on 2026-07-15:

- backend `studybuild` Modulith module with reusable study templates,
  JSON-backed defaults, template listing/creation, and application through the
  existing `StudyService.createStudy` contract
- Liquibase table migration for `module_study_template`
- SPA Study Build Toolkit at `/app/study-build` with template listing,
  defaults preview, template creation, and create-study-from-template workflow
- localized English/Chinese navigation and workflow copy

Verification:

- `mvn test -pl app -am -Dtest=StudyBuildServiceTest,FhirServiceTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  passed: 10 tests
- `corepack pnpm@11.1.2 -C frontend typecheck` passed
- `xmllint --noout shared/src/main/resources/migration/3.18/2026-07-15-study-build-tables.xml shared/src/main/resources/migration/3.18/release.xml`
  passed

Remaining work moves to later study-startup hardening: versioned template
application rules, CRF/rule/randomization defaults, amendment assistance, and
wizard regression coverage.

## 19. 12-Month R&D Roadmap

The roadmap below keeps work strictly engineering-focused.

## 20. Quarter 1 (`2026-07` to `2026-09`)

### Objective

Finish the two shared foundations that unblock participant workflows and start eCOA/ePRO integration.

### Deliverables

- task engine baseline
- participant identity baseline
- questionnaire schedule model
- participant bootstrap contract
- initial operator task UI

### Detailed monthly plan

#### Month 1

- finalize module boundaries for task engine and participant identity
- define entities, DTOs, and public service contracts
- create schema plan and migration plan
- define audit events and permission rules

#### Month 2

- implement backend entities/repositories/services
- implement token lifecycle and task state transitions
- add minimal REST surfaces
- add unit and controller tests

#### Month 3

- wire questionnaire assignments to task engine
- build minimal participant bootstrap and operator task UI
- verify end-to-end scheduling for one study shape

### Exit gate

- scheduled participant tasks exist
- shared participant access exists
- downstream module work can start without redefining these concerns

## 21. Quarter 2 (`2026-10` to `2026-12`)

### Objective

Deliver the first complete participant workflows.

### Deliverables

- eCOA/ePRO v1
- eConsent v1
- participant portal MVP
- adherence/status visibility

### Detailed monthly plan

#### Month 4

- complete schedule-driven questionnaire logic
- implement operator adherence queries
- add participant completion status surfaces

#### Month 5

- implement consent templates, versioning, and signature workflow
- connect consent tasks to task engine
- implement signed artifact generation pipeline

#### Month 6

- build participant portal shell
- integrate eCOA and eConsent into one participant entry surface
- run end-to-end verification on both workflows

### Exit gate

- participant can enter one shell and complete assigned consent/questionnaire work
- operators can see study-scoped status for both workflows

## 22. Quarter 3 (`2027-01` to `2027-03`)

### Objective

Extend into coordinator and monitor operations.

### Deliverables

- Recruit MVP
- analytics control plane v1
- SDV workflow MVP

### Detailed monthly plan

#### Month 7

- implement candidate intake model and public prescreen flow
- define analytics event capture across participant workflows
- start dashboard read-model design

#### Month 8

- implement candidate review and conversion flow
- implement initial dashboards
- define SDV state model and review APIs

#### Month 9

- build SDV UI
- connect discrepancy/audit visibility
- finish operational dashboards for participant/site backlogs

### Exit gate

- coordinators can manage candidates
- monitors can review SDV work
- operations can see key workflow metrics in-product

## 23. Quarter 4 (`2027-04` to `2027-06`)

### Objective

Deliver the first narrow interoperability slice and improve study setup speed.

### Deliverables

- FHIR / EHR connector pilot
- reconciliation flow
- provenance visibility
- study build toolkit first slice

### Detailed monthly plan

#### Month 10

- implement integration framework baseline and connector config
- define exact mapping scope for `Patient`, `Encounter`, and selected `Observation`

#### Month 11

- implement import pipeline and provenance model
- build review and reconciliation UI
- add retry and error visibility

#### Month 12

- finish narrow end-to-end connector flow
- implement first study template / guided setup slice
- run full verification and document follow-up gaps

### Exit gate

- one narrow connector path is production-shaped
- one reusable study-setup accelerator exists

## 24. Definition Of Done

Every module must satisfy all of the following.

### 24.1 Architecture

- module purpose documented
- dependency placement valid under Modulith rules
- no boundary-breaking shortcuts introduced

### 24.2 Backend

- schema and entity design complete
- repository/service/controller path complete
- authorization defined
- audit behavior defined

### 24.3 Frontend or service surface

- coherent entry point exists
- happy path works
- errors and empty states exist

### 24.4 Verification

- unit tests
- service/controller tests
- frontend or service tests where relevant
- regression coverage for key state transitions

### 24.5 Operations

- enable/disable behavior documented
- migration impact documented
- pilot limitations documented

## 25. Engineering Guardrails

1. Respect `allowedDependencies` and existing module boundaries.
2. Keep anti-corruption adapters under `internal/adapter/`.
3. Do not revive legacy patterns from removed `shared` Java code.
4. Prefer module-owned DTOs and repositories.
5. Treat `task`, `participant identity`, `analytics`, and `integration` as shared platforms, not feature-local helpers.
6. New optional modules must fail soft when disabled.
7. No module is done until end-to-end workflow verification passes.

## 26. Verification Matrix

Each phase should update and run a relevant subset of the matrix below.

### Backend

- module unit tests
- controller tests
- Modulith boundary verification
- export/audit regressions when affected

### Frontend

- typecheck
- lint
- targeted Vitest coverage
- workflow rendering and state tests

### Questionnaire service

- API tests
- scoring tests
- assignment/response workflow tests

### Integration and contract

- OpenAPI or DTO contract verification where relevant
- end-to-end payload compatibility across module boundaries

## 27. Immediate Next Step

The next execution document after this final plan should be a focused backlog for only the first two modules:

1. `Notification And Task Engine`
2. `Participant Identity And Access`

That backlog should contain:

- file/module targets
- entity and API checklist
- migration checklist
- frontend checklist
- test checklist

## 28. Summary

The final R&D strategy for ResearchEDC is:

- keep the existing Core stable
- add shared foundations first
- implement modules one by one
- maintain strict boundaries
- require full workflow completion for every module

This keeps the roadmap realistic, technically disciplined, and compatible with the project's open-source modular architecture.
