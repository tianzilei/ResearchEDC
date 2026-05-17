# core/ - Domain Logic & Data Access

**Module:** Core business logic, DAOs, services, and Hibernate entities  
**Files:** ~736 Java files  

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
- **Entities:** Hibernate 3.5 with annotations in `domain/datamap/`
- **Beans:** Plain DTOs in `bean/` with getters/setters
- **Services:** Interface + Impl pattern in `service/`

## TESTING

**Base classes:** `org.akaza.openclinica.templates.HibernateOcDbTestCase` and `OcDbTestCase` (both extend DBUnit's `DataSourceBasedDBTestCase`)

**DAO tests** (e.g., `RuleSetDaoTest`, `ItemDataDAOTest`):
- Load full Spring context via `ClassPathXmlApplicationContext` in static initializer
- Use DBUnit `FlatXmlDataSetBuilder` to load test data from `{package}/testdata/{ClassName}.xml`
- Test data simulates full clinical trial state (studies, subjects, events, CRFs, items)
- Teardown rolls back transactions and closes DS connection for isolation

**Service tests** (e.g., `RuleSetServiceTest`):
- Same `HibernateOcDbTestCase` base as DAO tests
- Test business logic like `filterByStatusEqualsAvailable`, `getRuleSetsByCrfStudyAndStudyEventDefinition`
- Use Spring beans via `getContext().getBean("serviceName")`

**Pure unit tests** (e.g., `ExpressionServiceTest`):
- Extend `junit.framework.TestCase` (no Spring, no DB)
- Test expression syntax validation, date format parsing

**Test data files:** 12 XML files under `src/test/resources/org/akaza/openclinica/{dao,service}/testdata/`
**Config:** `src/test/resources/test.properties` (DB connection) and `applicationContext-core-spring.xml`

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
