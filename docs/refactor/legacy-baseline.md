# Legacy Code Baseline

> Generated: 2026-05-18  
> Branch: master  
> Version: 3.18-SNAPSHOT

## Overview

This document captures the current legacy code metrics for OpenClinica as a baseline for the modernisation project. All counts are derived from actual source file analysis on the master branch. Future reports will compare against these numbers to track modernisation progress.

The project has three major legacy layers (legacy-core, web, ws) and a growing set of modern Spring Modulith modules under `app/src/.../module/`.

---

## Legacy Core (legacy-core/)

| Category | Files | Notes |
|---|---:|---|
| **Total Java files** | 754 | 737 main + 17 test |
| DAO files | 140 | `dao/` package — JDBC-based data access |
| Bean files | 250 | `bean/` package — legacy DTOs |
| Domain entities | 166 | `domain/` package — Hibernate entities |
| Service files | 60 | `service/` package |
| Logic files | 57 | `logic/` package — rule runners, ODM export |
| Job files | 9 | `job/` package — Quartz jobs |
| Validator files | 6 | `validator/` package |
| Exception files | 7 | `exception/` package |
| i18n files | 4 | `i18n/` package — resource bundles |
| Pattern files | 9 | `patterns/` package |
| Core utilities | 13 | `core/` package — SessionManager, helpers |
| Logging | 13 | `log/` package |
| Web filters | 3 | `web/filter/` — security filters |

### Service/Logic/Job Combined

| Category | Files |
|---|---:|
| Service + Logic + Job | **126** |

---

## Web Layer (web/)

| Category | Files | Notes |
|---|---:|---|
| **Total Java files** | 487 | Controllers, servlets, helpers, filters |
| JSP files | 838 | Under `webapp/WEB-INF/jsp/` and includes |
| SecureController subclasses | 187 | Servlet-based page controllers |

### Web Sub-Package Breakdown

| Package | Purpose |
|---|---|
| `control/admin/` | User, CRF, study, job administration |
| `control/submit/` | Data entry, subject, event operations |
| `control/managestudy/` | Study management, event definitions |
| `control/extract/` | Dataset creation, export |
| `control/login/` | Authentication, password management |
| `control/rule/` | Rule execution |
| `controller/` | Spring MVC REST controllers |
| `web/pform/` | OpenRosa/Pform integration |
| `web/crfdata/` | CRF data import services |
| `web/restful/` | ODM REST resources |

---

## SOAP Layer (ws/)

| Category | Files | Notes |
|---|---:|---|
| **Total Java files** | 173 | Including generated JAXB classes |
| Endpoint classes | 7 | Spring WS endpoints |
| Validators | 5 | SOAP request validators |

### SOAP Endpoint Inventory

| Endpoint | Business Capability |
|---|---|
| `DataEndpoint` | Clinical data import/export |
| `CrfEndpoint` | CRF operations |
| `StudyEndpoint` | Study CRUD |
| `StudySubjectEndpoint` | Study-subject enrollment |
| `StudyEventDefinitionEndpoint` | Event definition management |
| `EventEndpoint` | Study event operations |
| `CctsSubjectEndpoint` | CCTS subject transfer |

---

## Modern Modules (app/.../module/)

| Module | Java Files | Status | API Base Path |
|---|---:|---|---|
| randomization | 37 | ✅ Complete | `/api/v1/randomization` |
| event | 14 | ✅ Extracted | `/api/v1/events` |
| datacapture | 14 | ✅ Extracted | `/api/v1/data-capture` |
| subject | 11 | ✅ Extracted | `/api/v1/subjects` |
| identity | 11 | ✅ Built | `/api/v1/identity` |
| study | 10 | ✅ Extracted | `/api/v1/studies` |
| export | 9 | ✅ Complete | `/api/v1/exports` |
| audit | 9 | ✅ Extracted | `/api/v1/audit` |
| crf | 8 | ✅ Complete | `/api/v1/crfs` |
| notification | 5 | ✅ Complete | event-driven |
| legacy | 5 | ✅ Built | `/api/v1/legacy/*` |
| **Total** | **134** | | |

### app/ Module Summary

| Metric | Count |
|---|---:|
| Total app/src Java files | 150 |
| Module files (src/main) | 134 |
| Config/other app files | 16 |

---

## Cross-Cutting Legacy Indicators

| Indicator | Count | Details |
|---|---:|---|
| Ehcache (`net.sf.ehcache`) imports | 8 | 3 files in legacy-core (EhCacheWrapper, SQLFactory, RandomizationRegistrar) |
| `javax.*` residual imports | 303 | 147 in legacy-core (115 files), 83 in web (48 files), 73 in ws (36 files) |
| Legacy DAO imports from modern modules | 35 | 9 files in `app/src` |
| Legacy Bean imports from modern modules | 8 | 4 files in `app/src` |
| Legacy domain imports from modern modules | 1 | 1 file in `app/src` |
| **Total legacy imports from modern modules** | **44** | 14 files across app/src |

### Legacy Import Hotspots in Modern Modules

| File | Legacy Imports |
|---|---|
| `ServiceConfig.java` | 18 DAO imports |
| `SecurityConfig.java` | 5 DAO + 1 domain |
| `HibernateConfig.java` | 3 DAO imports |
| `LegacyCrfAdapter.java` | 5 DAO + 5 Bean imports |
| `LegacyStudyController.java` | 1 DAO + 1 Bean |
| `LegacySubjectController.java` | 1 DAO + 1 Bean |
| `OidcSessionBridgeSuccessHandler.java` | 1 DAO + 1 Bean |
| `DbConfig.java` | 1 DAO import |
| `CoreResourcesConfig.java` | 1 DAO import |

---

## Codebase Size Summary

| Layer | Java Files | JSP Files | Total |
|---|---:|---:|---:|
| legacy-core | 754 | — | 754 |
| web | 487 | 838 | 1,325 |
| ws | 173 | — | 173 |
| app (modern) | 150 | — | 150 |
| **Legacy total** | **1,414** | **838** | **2,252** |
| **Modern total** | **150** | **—** | **150** |
| **Grand total** | **1,564** | **838** | **2,402** |

### Legacy vs Modern Ratio

| Metric | Count | % |
|---|---:|---:|
| Legacy Java files | 1,414 | 90.4% |
| Modern Java files | 150 | 9.6% |
| Legacy JSP files | 838 | (separate) |

---

## Trend Targets

| Metric | Current | 3-Month Target | 6-Month Target | Notes |
|---|---:|---:|---:|---|
| Legacy DAO files | 140 | 112 (-20%) | 70 (-50%) | Migrate to module repositories |
| JSP files | 838 | 754 (-10%) | 587 (-30%) | Replace with React pages |
| SOAP Java files | 173 | 173 (baseline) | 138 (-20%) | Inventory first, then retire |
| Ehcache imports | 8 | 7 (-12%) | 5 (-37%) | Migrate to Spring Cache/Caffeine |
| `javax.*` residuals | 303 | 273 (-10%) | 182 (-40%) | Jakarta EE migration |
| Modern module files | 134 | 161 (+20%) | 201 (+50%) | New modules and expansion |
| SecureController subclasses | 187 | 169 (-10%) | 131 (-30%) | Replace with React routes |
| Legacy imports from modern | 44 | 35 (-20%) | 22 (-50%) | Expand adapter isolation |

---

## Methodology

All counts were generated using `find` and `grep` against the master branch source tree on 2026-05-18:

- **Java file counts**: `find <path> -name '*.java' | wc -l` (excluding `target/` and generated sources)
- **JSP counts**: `find web -name '*.jsp' | wc -l`
- **SecureController**: `grep "extends SecureController" web/` (unique files)
- **SOAP endpoints**: Files matching `*Endpoint*.java` in `ws/src/main/java/org/akaza/openclinica/ws/`
- **Ehcache**: `grep "import net.sf.ehcache"` across all Java sources
- **javax residuals**: `grep "^import javax\."` across all Java sources
- **Modern module files**: `find app/src -path '*/module/<name>/*' -name '*.java'` per module
- **Legacy imports from modern**: `grep "import org.akaza.openclinica.(dao|bean|domain)\."` in `app/src/`

---

## Revision History

| Date | Author | Change |
|---|---|---|
| 2026-05-18 | Modernisation AI | Initial baseline generated |
