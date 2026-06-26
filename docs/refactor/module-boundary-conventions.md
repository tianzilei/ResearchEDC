# Module Boundary Conventions

**Created:** 2026-06-26 (Phase 12E)
**Status:** Active

## Module Dependency Map

All 17 modules declare `allowedDependencies` in their `package-info.java` via `@ApplicationModule`. The `ModulithVerificationTest` enforces these boundaries at build time.

### Dependency Graph

```
audit           → dataimport::event
crf             → dataimport::service, dataimport::dto
dashboard       → identity::{service,dto}, study::{service,dto}, event::service, audit::{service,dto,enums}, discrepancynote::service
datacapture     → audit::{service,enums}, dataimport::{service,dto}, event::{repository,entity}, subject::{repository,entity}, crf::{repository,entity}, rule::{repository,entity}
dataimport      → dataimport::dto (self)
dataset         → (none)
discrepancynote → (none)
event           → audit::{service,enums}, dataimport::{service,dto}
export          → study::{repository,entity}, subject::{repository,entity}, event::{repository,entity}, datacapture::{repository,entity}, crf::{repository,entity}
filter          → (none)
identity        → audit::{service,enums}
openrosa        → audit::{service,enums}, crf::{entity,repository}, study::{entity,repository}, subject::{entity,repository}, event::{entity,repository}, datacapture::{entity,repository}, identity::{entity,repository}
randomization   → (none)
rule            → (none)
study           → audit::{service,enums}, dataimport::{service,dto}
subjectgroup    → audit::{service,enums}
subject         → audit::{service,enums}, event::{service,dto}, dataimport::{service,dto}
```

### Module Categories

| Category | Modules | Pattern |
|----------|---------|---------|
| **Leaf (no deps)** | dataset, filter, randomization, rule, discrepancynote | Pure domain, no cross-module access |
| **Audit-aware** | identity, subjectgroup, study, subject, event | Depend on audit for event logging |
| **Data pipeline** | datacapture, dataimport, crf | Depend on multiple modules for entity/repository access |
| **Aggregation** | dashboard, export, openrosa | Read from many modules, write to few |

### Convention: Package Slices in `allowedDependencies`

Modules declare dependencies at the **package slice** level, not the whole module:
- `study::service` — public service API
- `study::dto` — data transfer objects
- `study::repository` — Spring Data repositories (read access)
- `study::entity` — JPA entities (data model access)

This enforces that a module can depend on another module's **data model** without depending on its **business logic**.

## Anti-Corruption Adapter Pattern

Modules that need to access legacy or cross-module data do so through `internal/adapter/` classes:

| Module | Adapter | Purpose |
|--------|---------|---------|
| `audit` | `AuditStudySubjectEventAdapter`, `AuditUserEventAdapter`, `AuditUserLoginAdapter`, `DatabaseChangeLogDaoAdapter` | Bridge to audit event infrastructure |
| `crf` | `CrfVersionDaoAdapter`, `ItemDaoAdapter`, `ItemFormMetadataDaoAdapter`, `SCDItemMetadataDaoAdapter` | Bridge to CRF metadata repositories |
| `datacapture` | `ItemDataDaoAdapter`, `ItemGroupDaoAdapter`, `ResponseSetDaoAdapter`, `AttachmentStorageAdapter` | Bridge to data capture repositories |
| `dataimport` | `ImportCrfDataAdapter` | Bridge to CRF data for import validation |
| `event` | `StudyEventDaoAdapter`, `StudyEventDefinitionDaoAdapter`, `EventCrfDaoAdapter` | Bridge to event repositories |
| `export` | `ExportDataProviderAdapter` | Bridge to multiple repositories for ODM export |
| `openrosa` | `OpenRosaCrfAdapter` | Bridge to CRF metadata for OpenRosa XML |
| `study` | `StudyDaoAdapter` | Bridge to study repositories |
| `subject` | `StudySubjectDaoAdapter` | Bridge to subject repositories |

### Adapter Rules
1. Adapters live under `module/<name>/internal/adapter/`
2. Adapters are `@Component` classes (not interfaces)
3. Adapters use module-owned repositories — never bypass to shared DAOs
4. Adapters are `@Primary` when implementing a shared SPI (legacy bridge pattern)
5. Public module APIs must not expose adapter types

## Null-Safety Conventions

### Current `@NonNullApi` Adoption

| Module | Package | `@NonNullApi` |
|--------|---------|---------------|
| `dashboard` | `service/`, `dto/`, `controller/` | ✅ |
| `dataimport` | `event/` | ✅ |
| All others | — | ❌ |

### Recommendation

- **New module code**: Add `@NonNullApi` to `package-info.java` at the module root
- **Existing modules**: Adopt incrementally — add `@NonNullApi` when modifying a module's service/controller layer
- **Entity packages**: Do NOT add `@NonNullApi` — JPA entities have nullable columns by nature
- **DTO packages**: Safe to add — DTOs are constructed in controlled code paths

### Convention for New Modules

```java
@org.springframework.lang.NonNullApi
package org.researchedc.module.<name>;
```

## Guardrails

1. **`ModulithVerificationTest`** — Authoritative boundary gate. Runs on every build.
2. **`allowedDependencies`** — Declared in `package-info.java` per module. Verified by Spring Modulith.
3. **`internal/adapter/`** — Anti-corruption layer convention. Enforced by code review.
4. **No `shared.*` imports** — Module public classes must not import `shared/` Java packages (enforced by convention, 0 Java files in shared/).
5. **No circular dependencies** — Enforced by `ModulithVerificationTest`.
