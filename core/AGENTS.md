# core/ - Domain Logic & Data Access

**Module:** Core business logic, DAOs, services, and Hibernate entities  
**Files:** ~736 Java files  

> **Modulith 迁移状态:** core 模块的包 (`bean`, `dao`, `domain`, `service`, `logic` 等) 是遗留代码。新模块应创建在 `app/` 模块的 `org.akaza.openclinica.module.<name>` 包下，逐步从 core 提取到对应的 Modulith 模块中。提取顺序: notification ✅ → export → audit → randomization → subject → study/site → crf/data-capture。

## STRUCTURE

```
core/src/main/java/org/akaza/openclinica/
├── bean/          # DTOs/Beans - StudyBean, SubjectBean, etc.
├── dao/           # Data Access Objects - SQL queries
├── domain/        # Hibernate entities (JPA annotations)
├── service/       # Business services
├── logic/         # Business logic (rules, expressions)
├── job/           # Quartz scheduled jobs
├── validator/     # Input validation
├── exception/     # Custom exceptions
└── i18n/          # Internationalization utilities
```

## WHERE TO LOOK

| Task | Location | Pattern |
|------|----------|---------|
| Entity definitions | `domain/datamap/` | Hibernate `@Entity` classes |
| Database queries | `dao/*/` | `*DAO extends EntityDAO` |
| Business logic | `service/*/` | `*Service` or `*ServiceImpl` |
| Rules engine | `logic/rule/` | Expression processing |
| Data imports | `logic/importdata/` | Import processors |

## CONVENTIONS

- **DAOs:** Extend `EntityDAO<K>` with `execute()`/`executeFind()` methods
- **Entities:** Hibernate 6.4.4 with JPA annotations in `domain/datamap/` (migrated from Hibernate 3.5)
- **Beans:** Plain DTOs in `bean/` with getters/setters
- **Services:** Interface + Impl pattern in `service/`

## KNOWN ISSUES

### Hibernate 6 兼容性
- **Physical naming strategy**: `CamelCaseToUnderscoresNamingStrategy` 将 Java 属性名转换为下划线列名
  - 约 50+ 实体使用隐式命名策略（无 `@Column`），约 50+ 使用显式 `@Column(name = "...")`
  - 同名表对应多个 Entity 时需添加 `@Entity(name = "...")` 区分
  - 原始 `Set` 类型（无泛型）在 Hibernate 6 中需添加类型参数
- **Liquibase 4.26**: `defaultValueComputed` 属性在 column 元素中不支持，需使用 `<constraints nullable="false"/>`
- **单元测试**: 需 JDK 21 运行（JDK 25 下 Mockito/ByteBuddy 不兼容）

## TESTING

**Base classes:** `org.akaza.openclinica.templates.HibernateOcDbTestCase` and `OcDbTestCase` (both extend DBUnit's `DataSourceBasedDBTestCase`)

**DAO tests** (e.g., `RuleSetDaoTest`, `ItemDataDAOTest`):
- Load full Spring context via `ClassPathXmlApplicationContext` in static initializer
- Use DBUnit `FlatXmlDataSetBuilder` to load test data from `{package}/testdata/{ClassName}.xml`
- Test data simulates full clinical trial state (studies, subjects, events, CRFs, items)
- Teardown rolls back transactions and closes DS connection for isolation
- ⚠️ 测试方法多数被注释，需准备完整 PostgreSQL 数据库

**Service tests** (e.g., `RuleSetServiceTest`):
- Same `HibernateOcDbTestCase` base as DAO tests
- Test business logic like `filterByStatusEqualsAvailable`, `getRuleSetsByCrfStudyAndStudyEventDefinition`
- Use Spring beans via `getContext().getBean("serviceName")`

**Pure unit tests** (e.g., `ExpressionServiceTest`):
- Extend `junit.framework.TestCase` (no Spring, no DB)
- Test expression syntax validation, date format parsing

**Test data files:** 12 XML files under `src/test/resources/org/akaza/openclinica/{dao,service}/testdata/`
**Config:** `src/test/resources/test.properties` (DB connection) and `applicationContext-core-spring.xml`

### 运行测试
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
docker run -d --name oc-test-pg -e POSTGRES_USER=clinica \
  -e POSTGRES_PASSWORD=clinica \
  -e POSTGRES_DB=openclinica-TEST-3.12 \
  -p 5432:5432 postgres:17-alpine
mvn test -pl core
```

## ANTI-PATTERNS

- **NEVER** modify Liquibase migration files after release
- **NEVER** use raw SQL outside DAO layer
- **ALWAYS** use `EntityDAO` pattern for DB access
- **DO NOT** create circular dependencies between services

## KEY PATTERNS

### EntityDAO Pattern
```java
public class StudyDAO extends EntityDAO<StudyBean> {
    public StudyBean findByPK(int id) {
        HashMap variables = new HashMap();
        variables.put(1, id);
        return (StudyBean) executeFind("findByPK", variables).get(0);
    }
}
```

### Service Pattern
```java
@Service
@Transactional
public class StudyServiceImpl implements StudyService {
    @Autowired
    private StudyDAO studyDao;
    // ...
}
```
