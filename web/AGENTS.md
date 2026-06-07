# web/ - Web UI Layer

**Module:** Web interface, JSP pages, servlets, REST controllers  
**Files:** 484 Java files, 419 JSP files  

> **前端共存:** 新 React SPA 运行于 `/app/*` 路径，旧 JSP 继续运行于 `/legacy/*`（通过 Nginx 路由）。
> 新前端代码位于 `frontend/src/`，与 web 模块无关。REST 控制器仍在此模块中供新旧前端共用。
> **Legacy removal status:** this module is still legacy-heavy. Current scan shows 189 `SecureController`/`CoreSecureController` subclasses and 419 JSP files. Do not delete servlets/JSPs until the matching SPA route, module API, redirect, and permission behavior are verified.

## STRUCTURE

```
web/src/main/java/org/researchedc/
├── control/          # Servlets (MVC controllers)
│   ├── admin/        # Admin functions
│   ├── extract/      # Data extraction
│   ├── login/        # Authentication
│   ├── managestudy/  # Study management
│   ├── submit/       # Data entry/submission
│   └── techadmin/    # Technical admin
├── controller/       # Spring MVC REST controllers
├── view/             # JSP view helpers
└── web/              # Filters, beans, REST resources

web/src/main/webapp/
├── WEB-INF/jsp/      # JSP pages
└── images/           # Static assets
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Study management UI | `control/managestudy/` | Servlets for study operations |
| Data entry | `control/submit/` | CRF data entry servlets |
| Admin panel | `control/admin/` | System administration |
| REST API | `controller/` | Spring @Controller classes |
| JSP pages | `webapp/WEB-INF/jsp/` | View templates |
| Legacy deletion plan | `../docs/refactor/remove-legacy-code-plan.md` | Workflow migration and deletion gates |

## CONVENTIONS

- **Servlets:** Extend `SecureController` for auth/security
- **REST Controllers:** Use `@Controller` + `@RequestMapping`
- **JSPs:** Use `include/footer.jsp`, `include/header.jsp`

## TESTING

**Base class:** `junit.framework.TestCase` (no Spring context, no database)

**Tests in web module** (2 test files: `SubmitDataServletTest`, `ListDiscNotesForCRFServletTest`):
- Pure JUnit unit tests — no DB, no Spring context
- Use **Mockito** (`import static org.mockito.Mockito.*`) for mocking role/permission objects
- Test authorization logic like `mayViewData()`, `maySubmitData()` with different roles
- Configure locale via `ResourceBundleProvider.updateLocale()`
- ✅ 3 tests pass (JAVA_HOME=21 required for Mockito/ByteBuddy compatibility)

## ANTI-PATTERNS

- **NEVER** bypass `SecureController` - always check session
- **NEVER** put SQL in servlets - delegate to core services
- **ALWAYS** use `forwardPage()` pattern for navigation
- **DO NOT** hardcode URLs - use `Page.*` constants

## KEY PATTERNS

### SecureController Pattern
```java
public class MyServlet extends SecureController {
    @Override
    protected void processRequest() throws Exception {
        // Session/auth already validated
        // ...
        forwardPage(Page.MY_PAGE);
    }
}
```
