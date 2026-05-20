# Subject Module Refactor

> Generated at: 2026-05-18
> Status: Phase 1 Complete
> Branch: master

## Overview

This document describes the Subject module refactoring from legacy bridge pattern to modern application service + domain model pattern. It serves as the template for future module migrations (Study, Event, DataCapture, etc.).

## Refactor Summary

- **Target module**: Subject (app/.../module/subject/)
- **Phase**: 1 (ApplicationService + Adapter + Events)
- **Status**: Complete

## Before Structure (Phase 0)

```
module/subject/
├── controller/SubjectController.java    # REST API
├── service/SubjectService.java          # Business logic + @Transactional
├── repository/
│   ├── SubjectRepository.java           # JPA repository
│   └── StudySubjectRepository.java      # JPA repository
├── entity/
│   ├── SubjectEntity.java               # JPA entity
│   └── StudySubjectEntity.java          # JPA entity
├── dto/
│   ├── SubjectDTO.java                  # Read DTO
│   ├── StudySubjectDTO.java             # Read DTO
│   ├── CreateSubjectRequest.java        # HTTP request DTO
│   └── EnrollSubjectRequest.java        # HTTP request DTO
└── package-info.java
```

## After Structure (Phase 1)

```
module/subject/
├── api/
│   ├── SubjectController.java          ← REST API (unchanged)
│   └── dto/                            ← HTTP DTOs (unchanged)
├── application/
│   ├── SubjectApplicationService.java  ← NEW: orchestrator, validates, publishes events
│   └── command/
│       ├── CreateSubjectCommand.java   ← NEW: write command (decoupled from HTTP)
│       └── EnrollSubjectCommand.java   ← NEW: write command
├── domain/
│   ├── SubjectPolicy.java              ← NEW: business rules & validation
│   └── SubjectDomainService.java       ← NEW: domain logic (label generation, etc.)
├── infrastructure/
│   ├── SubjectRepository.java          ← JPA repository (unchanged)
│   ├── StudySubjectRepository.java     ← JPA repository (unchanged)
│   └── LegacySubjectAdapter.java       ← NEW: placeholder for future legacy DAO bridge
├── event/
│   └── SubjectEnrolledEvent.java       ← NEW: ApplicationEvent for cross-module comms
├── service/
│   └── SubjectService.java             ← Existing service (unchanged, wrapped by ApplicationService)
└── package-info.java
```

## Architecture

### Layered Architecture

```
SubjectController (REST API)
    ↓ HTTP DTOs (CreateSubjectRequest, etc.)
SubjectApplicationService (application orchestrator)
    ↓ Commands (CreateSubjectCommand, etc.)
    ↓ Validation → SubjectPolicy
    ↓ Domain logic → SubjectDomainService
    ↓ Events → SubjectEnrolledEvent (published to Spring ApplicationEvents)
    ↓ Delegation
SubjectService (existing business logic, @Transactional)
    ↓
SubjectRepository / StudySubjectRepository (JPA repositories)
    ↓
Database (subject / study_subject tables)
```

### Key Design Decisions

1. **Controller → ApplicationService delegation**: Controller continues to accept HTTP-specific DTOs (CreateSubjectRequest). ApplicationService accepts Commands. This provides clean separation between HTTP concerns and application concerns.

2. **Existing SubjectService preserved**: No need to rewrite working code. SubjectApplicationService wraps and delegates to SubjectService. Future refactors can incrementally move logic from SubjectService into the domain layer.

3. **LegacySubjectAdapter as placeholder**: Currently throws UnsupportedOperationException. When legacy DAO access is needed (e.g., SubjectDAO integration), the adapter provides a single injection point rather than scattering legacy imports across the module.

4. **Domain events for cross-module communication**: SubjectEnrolledEvent is published to Spring's ApplicationEvents mechanism, allowing other modules (Randomization, Event, DataCapture) to react without direct coupling.

5. **Policy objects for validation**: SubjectPolicy contains static validation methods, keeping business rules testable outside the Spring container.

### Dependency Flow

```
module/subject/     → depends on → module/subject/entity (internal)
                      depends on → module/subject/repository (internal)
                      depends on → Spring Modulith ApplicationEvents

module/subject/     → NO dependency on → legacy-core DAO/Bean (isolated via adapter)
```

## DTO Strategy

| Layer | DTO Type | Request | Response |
|-------|----------|---------|----------|
| HTTP (Controller) | Request/Response DTO | CreateSubjectRequest, EnrollSubjectRequest | SubjectDTO, StudySubjectDTO |
| Application | Command | CreateSubjectCommand, EnrollSubjectCommand | Same as above |
| Domain | Domain Model | SubjectEntity | SubjectEntity |

All modern DTOs are decoupled from legacy Bean classes. Legacy Beans (SubjectBean) are NOT exposed from this module.

## Events

| Event | Published When | Fields |
|-------|---------------|--------|
| SubjectEnrolledEvent | After enrollSubject() completes | studySubjectId, subjectId, studyId, enrolledAt (LocalDateTime) |

## Migration Guide

### For New Modules

Follow this sequence when refactoring a new module:

1. Copy the `api/` structure from the existing module
2. Create `application/` with ApplicationService + Commands
3. Create `domain/` with Policy + DomainService
4. Create `infrastructure/Legacy<Module>Adapter.java`
5. Create `event/<Module>ChangedEvent.java`
6. Ensure Controller `@Autowired` → ApplicationService (not Service directly)
7. Run `mvn compile` to verify
8. Run ModulithVerificationTest to verify boundaries

### Steps to Keep Backward Compatible

1. Keep existing REST API paths unchanged
2. Keep existing DTO shapes unchanged
3. Keep existing Service class as delegate target
4. Add new files only (don't modify existing)

## Verification

| Check | Command | Status |
|-------|---------|--------|
| Maven compile | `mvn -B clean compile -DskipTests` | ✅ Pass |
| Modulith boundary | `mvn test -pl app -am -Dtest=ModulithVerificationTest` | ✅ Pass (1/0/0) |
| Existing files | `git diff -- app/src/.../module/subject/` | ✅ No existing files modified |

## Remaining Legacy Dependencies

| Dependency | File | Plan |
|------------|------|------|
| None | — | Subject module is fully decoupled from legacy-core |

## Next Steps

1. Refactor Study module to same pattern (Phase 1 complete)
2. Refactor Event module to same pattern (Phase 1 complete)
3. Connect ApplicationServices to real data flows (Phase 2)
4. Move domain logic from Service classes to DomainService classes incrementally

## Revision History

| Date | Author | Change |
|------|--------|--------|
| 2026-05-18 | AI-Assisted | Initial creation as Phase 1 deliverable |
