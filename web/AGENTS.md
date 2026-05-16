# web/ - Web UI Layer

**Module:** Web interface, JSP pages, servlets, REST controllers  
**Files:** ~486 Java files, ~300 JSP files  

## STRUCTURE

```
web/src/main/java/org/akaza/openclinica/
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

## CONVENTIONS

- **Servlets:** Extend `SecureController` for auth/security
- **REST Controllers:** Use `@Controller` + `@RequestMapping`
- **JSPs:** Use `include/footer.jsp`, `include/header.jsp`

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
