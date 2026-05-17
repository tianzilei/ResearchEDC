# OpenClinica - PROJECT KNOWLEDGE BASE

**Generated:** 2026-05-17  
**Branch:** master  

## OVERVIEW

OpenClinica is an open-source Electronic Data Capture (EDC) and Clinical Data Management (CDM) platform for clinical trials. Built on Java 7 with Spring Framework 3.2, Hibernate ORM, and Liquibase migrations. Multi-module Maven project supporting Oracle and PostgreSQL.

## STRUCTURE

```
./
├── core/           # Domain logic, DAOs, services, Hibernate entities
├── web/            # Web UI (JSP), servlets, controllers, REST endpoints  
├── ws/             # SOAP web services, study/subject/event endpoints
├── pom.xml         # Maven parent - Spring 3.2, Hibernate 3.5, Java 7
└── README.md
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Database entities | `core/src/main/java/org/akaza/openclinica/domain/datamap/` | Hibernate annotations |
| Data access (SQL) | `core/src/main/java/org/akaza/openclinica/dao/**/*.java` | EntityDAO pattern |
| Web controllers | `web/src/main/java/org/akaza/openclinica/control/**/*.java` | Extends SecureController |
| REST endpoints | `web/src/main/java/org/akaza/openclinica/controller/*.java` | Spring @Controller |
| SOAP services | `ws/src/main/java/org/akaza/openclinica/ws/*Endpoint.java` | Spring WS |
| JSP pages | `web/src/main/webapp/WEB-INF/jsp/**/*.jsp` | Legacy UI layer |
| Migrations | `core/src/main/resources/migration/` | Liquibase changelog |
| i18n strings | `core/src/main/resources/org/akaza/openclinica/i18n/*.properties` | 6 languages |
| Properties config | `*/src/main/filters/datainfo.properties` | DB connection, paths |

## CONVENTIONS

- **Package:** `org.akaza.openclinica.*`
- **Beans:** `*Bean` suffix for DTOs (e.g., `StudyBean`)
- **DAOs:** `*DAO` suffix, extend `EntityDAO<K extends EntityBean>`
- **Servlets:** `*Servlet` suffix, extend `SecureController` or `CoreSecureController`
- **Tests:** 3-tier: JUnit (`TestCase`), DAO integration (`HibernateOcDbTestCase` + DBUnit), Service integration
- **Logging:** SLF4J + Logback (configured in `logback.xml`)

## ANTI-PATTERNS (THIS PROJECT)

- **NEVER** bypass `SecureController` for session/auth checks
- **DO NOT** write SQL directly in servlets - use DAO layer
- **AVOID** modifying migration files after they've been released
- **NEVER** hardcode file paths - use `CoreResources.getField()`
- **DO NOT** ignore transaction boundaries - use `@Transactional` or `TransactionTemplate`
- **AVOID** Java 7+ features (project targets Java 7 compatibility)

## TESTING ARCHITECTURE

Tests live in `core/src/test` (17 files) and `web/src/test` (2 files). Three tiers:

| Tier | Base Class | DB Needed | Use Case |
|------|-----------|-----------|----------|
| **Unit** | `junit.framework.TestCase` | ❌ | Pure logic, format conversion, expression parsing |
| **DAO** | `HibernateOcDbTestCase` (extends DBUnit `DataSourceBasedDBTestCase`) | ✅ | CRUD operations, query correctness |
| **Service** | `HibernateOcDbTestCase` | ✅ | Business service integration, rule filtering |

**Key patterns:**
- DAO/Service tests load full Spring context: all `applicationContext-*.xml` via `ClassPathXmlApplicationContext`
- Test data follows convention: `{package}/testdata/{ClassName}.xml` — DBUnit FlatXmlDataSet auto-loaded per test class
- Pure unit tests (like `SubmitDataServletTest`) use Mockito for role/permission mocking
- `test.properties` configures DB connection (Oracle/PostgreSQL)
- Base classes `HibernateOcDbTestCase` and `OcDbTestCase` in `core/.../templates/`

## UNIQUE STYLES

- **DAO Pattern:** All DB access through EntityDAO subclasses with `execute()`/`executeFind()`
- **Bean Pattern:** All data transfer objects extend `EntityBean` with audit fields
- **Servlet Security:** All web controllers extend `SecureController` which handles authz
- **ODM Export:** Clinical data exports use CDISC ODM XML format
- **Rules Engine:** Expression-based edit checks in `core/.../rule/expression/`

## COMMANDS

```bash
# Build all modules
mvn clean install -DskipTests

# Build with specific DB profile (default, integration, dev-*)
mvn clean install -Dconfig.id=integration

# Run tests
mvn test

# Skip tests (common for initial build)
mvn clean install -DskipTests=true

# Generate database migration (requires datainfo.properties)
mvn liquibase:update -pl core

# Build distribution
mvn clean package assembly:single
```

## NOTES

- **Database:** Supports Oracle and PostgreSQL - test queries exist in `core/src/main/resources/queries/`
- **Security:** Spring Security 3.2 with LDAP support, role-based access control
- **Scheduling:** Quartz jobs for data export and imports
- **Legacy:** Mix of older patterns (JSP, Servlet) with newer Spring MVC controllers
- **Profiles:** Maven filters in `*/src/main/filters/` for environment-specific config
- **Version:** Currently 3.18-SNAPSHOT (pom.xml)

## SUBMODULE REFERENCES

- [core/AGENTS.md](./core/AGENTS.md) - Domain logic and data access
- [web/AGENTS.md](./web/AGENTS.md) - Web UI and controllers
- [ws/AGENTS.md](./ws/AGENTS.md) - SOAP web services
