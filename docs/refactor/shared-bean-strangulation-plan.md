# Shared Bean Strangulation Plan

**Created:** 2026-06-24
**Purpose:** Eliminate the 13 remaining `shared/bean` files by migrating adapters to module-owned DTOs.

## Current State

- **13 shared/bean files** remaining
- **12 adapter files** in `app/module/*/internal/adapter/` use these beans
- **93 methods** across adapters return legacy DTO types
- **Core beans** (3): `EntityBean`, `AuditableEntityBean`, `Status`
- **Manage study beans** (4): `StudyBean`, `StudySubjectBean`, `StudyEventBean`, `StudyEventDefinitionBean`
- **Submit beans** (6): `CRFVersionBean`, `EventCRFBean`, `ItemDataBean`, `ItemBean`, `ItemFormMetadataBean`, `ItemGroupBean`

## Completed Work

### Phase 1: Core Bean Migration ✅

Created module-owned base types in `app/src/main/java/org/researchedc/app/dto/`:
- `Status.java` - Enum with 12 status values (in `app.dto` package to satisfy Modulith boundaries)
- `Entity.java` - Base class with `id`, `name`, `active` fields
- `AuditableEntity.java` - Extends Entity with `createdDate`, `updatedDate`, `ownerId`, `updaterId`, `status`

### Phase 2: Adapter Migrations ✅ (5 of 11)

Migrated adapters to use module-owned DTOs:
- `ItemGroupDaoAdapter` → `ItemGroupDTO` (datacapture/dto/)
- `ItemDaoAdapter` → `ItemDTO` (crf/dto/)
- `ItemDataDaoAdapter` → `ItemDataDTO` (datacapture/dto/)
- `CrfVersionDaoAdapter` → `CrfVersionDTO` (crf/dto/)
- `AuditStudySubjectEventAdapter` → uses module-owned `Status` only
- Modulith verification test passes

## Remaining Work

### Phase 3: Remaining Adapter Migrations (6 adapters)

**Adapters to migrate:**
1. `StudyDaoAdapter` → `StudyDto` (StudyBean=800 lines, complex)
2. `StudySubjectDaoAdapter` → `StudySubjectDto` (depends on StudyBean)
3. `StudyEventDaoAdapter` → `StudyEventDto` (depends on StudyBean, StudyEventBean, StudyEventDefinitionBean)
4. `StudyEventDefinitionDaoAdapter` → `StudyEventDefinitionDto` (depends on StudyBean, StudyEventDefinitionBean)
5. `EventCrfDaoAdapter` → `EventCrfDto` (depends on StudyBean, StudyEventBean, StudySubjectBean, CRFVersionBean, EventCRFBean)
6. `ItemFormMetadataDaoAdapter` → `ItemFormMetadataDto` (complex nested ResponseSetBean)

### Phase 4: Caller Migration

Update all callers of adapter methods to use module-owned DTOs instead of legacy beans.

**Callers to update:**
- Module services that call adapter methods
- Controllers that expose adapter results
- Tests that assert on adapter results

### Phase 5: Shared Bean Deletion

After all callers are migrated, delete the 13 shared/bean files.

## Strategy

### Pattern for Adapter Migration

1. Create module-owned DTO in `app/module/<name>/dto/`
2. Update adapter to import module-owned DTO instead of shared bean
3. Update adapter methods to return module-owned DTO
4. Update adapter tests to use new DTO types
5. Verify compilation and tests pass

### Modulith Boundary Constraints

- `datacapture` module can depend on: `audit::service`, `audit::enums`, `dataimport::service`, `dataimport::dto`, `event::repository`, `event::entity`, `subject::repository`, `subject::entity`, `crf::repository`, `crf::entity`, `rule::repository`, `rule::entity`
- Core DTOs (`Entity`, `AuditableEntity`, `Status`) must live in `dataimport::dto` to be accessible by all modules

## Verification

After each adapter migration:
```bash
mvn clean compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest,<AdapterTest> -Dsurefire.failIfNoSpecifiedTests=false
cd frontend && pnpm typecheck
```

## Success Criteria

- Zero imports of `org.researchedc.bean.*` in `app/` module
- All adapter methods return module-owned DTOs
- All tests pass
- `shared/bean/` directory is empty or removed

## Current Progress

- **30 imports** of `org.researchedc.bean.*` remaining in `app/` module (down from 76)
- **6 adapters** still using shared beans (down from 11)
- **Base types** (`Status`, `Entity`, `AuditableEntity`) in `org.researchedc.app.dto` (outside Modulith scan path)
- **Compilation** ✅ | **Modulith verification** ✅ | **Adapter tests** ✅
