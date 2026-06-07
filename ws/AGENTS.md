# ws/ - Web Services

**Module:** SOAP web services for study/subject/event management  
**Files:** 75 Java files  

> **Legacy removal status:** SOAP remains part of the legacy surface. Endpoint adapters reduce DAO coupling, but the module is not deleted and should be retired only after each external SOAP contract has an owner-approved replacement or deprecation path.

## STRUCTURE

```
ws/src/main/java/org/researchedc/
├── ws/              # SOAP endpoints
│   ├── StudyEndpoint.java
│   ├── StudySubjectEndpoint.java
│   ├── EventEndpoint.java
│   └── CrfEndpoint.java
├── controller/      # Service controllers
├── view/            # View helpers (shared with web)
└── web/             # Web service utilities
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Study operations | `ws/StudyEndpoint.java` | SOAP study endpoints |
| Subject operations | `ws/StudySubjectEndpoint.java` | Subject CRUD |
| Event operations | `ws/EventEndpoint.java` | Study event endpoints |
| CRF operations | `ws/CrfEndpoint.java` | Case report form endpoints |
| Legacy deletion plan | `../docs/refactor/remove-legacy-code-plan.md` | SOAP retirement gates |

## CONVENTIONS

- **Endpoints:** Spring WS `@Endpoint` annotated classes
- **Services:** Use core services for business logic
- **Validation:** Use `BaseVSValidatorImplementation`

## ANTI-PATTERNS

- **NEVER** expose internal IDs in SOAP responses
- **ALWAYS** validate input with XSD schemas
- **DO NOT** bypass service layer - use core services

## KEY PATTERNS

### SOAP Endpoint Pattern
```java
@Endpoint
public class StudyEndpoint {
    @PayloadRoot(localPart = "createStudyRequest")
    @ResponsePayload
    public CreateStudyResponse createStudy(@RequestPayload CreateStudyRequest request) {
        // Delegate to core service
    }
}
```
