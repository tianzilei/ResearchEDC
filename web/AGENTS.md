# web/ - Web UI Layer (Gutted)

**Module:** Remaining web compatibility Java only
**Files:** 102 Java files, 0 JSP files, 0 static assets

> **Run 96 (2026-06-12):** All JSP views (29), static assets (~1400), GWT remnants, TLDs, tags, and pages-servlet.xml deleted. web.xml reduced from 310→40 lines (dead servlets/listeners/filters removed). 102 Java files remain: 6 secure servlet subclasses, servlet bases, data-entry/import validation helpers, form builders, table/view helpers, and SDV/scheduled-job support.

## REMAINING FILES

```
web/src/main/java/org/researchedc/
├── control/
│   ├── core/                  # SecureController, CoreSecureController (base classes)
│   ├── form/                  # Validator, DiscrepancyValidator, FormProcessor, etc.
│   ├── managestudy/           # BeanFactory, ViewNotesServlet
│   ├── SpringServletAccess.java
│   └── submit/                # DataEntryServlet, DownloadAttachedFileServlet, ImportCRFInfoContainer
├── view/                      # Link, Page, Table, form/Rule
└── web/
    ├── crfdata/               # ImportCRFDataService, ImportHelper
    └── filter/                # LocaleFilter
```

## DEPENDENCY CHAIN

The high-risk data-entry/import cluster is kept because app/ module imports or still depends on it:

- `app/.../module/dataimport/` → `control.form.{Validator, DiscrepancyValidator, FormDiscrepancyNotes}` + `web.crfdata.ImportCRFDataService`
- `app/.../OpenClinicaApplication.java` → component-scans `web.filter`

## DELETION BLOCKER

Removing these Java files requires:
1. Moving validation/import logic into `app/module/dataimport` (preferred)
2. Accepting them as compatibility shim code

## WEBAPP

```
web/src/main/webapp/
└── WEB-INF/
    └── web.xml               # 40 lines (EncodingFilter + listeners only)
```
