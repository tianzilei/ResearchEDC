# Legacy Workflow Inventory

Generated: 2026-06-09 16:25:58 UTC

Full CSV inventory: `legacy-workflow-inventory.csv`

## Summary By Type

| Artifact type | Count |
|---|---:|
| `dao-implementation` | 60 |
| `dao-spi` | 60 |
| `dao-support` | 6 |
| `jsp-view` | 121 |
| `legacy-servlet` | 68 |
| `scheduled-job` | 3 |
| `shared-service` | 50 |
| `spring-mvc-route` | 24 |

## Summary By Classification

| Classification | Count |
|---|---:|
| `keep compatibility` | 86 |
| `replace` | 276 |
| `unknown` | 30 |

## Summary By Phase Slice

| Phase slice | Count |
|---|---:|
| `phase-0-inventory-and-gates` | 26 |
| `phase-1-admin-write` | 6 |
| `phase-1-crf-metadata` | 31 |
| `phase-1-data-entry-discrepancy` | 36 |
| `phase-1-export-dataset-filter` | 19 |
| `phase-1-import-export-compatibility` | 26 |
| `phase-1-login-profile` | 5 |
| `phase-1-study-subject-event` | 64 |
| `phase-3-dao-implementation-deletion` | 126 |
| `phase-4-shared-service-deletion` | 53 |

## First Phase 1 Candidate Slice

Recommended slice: `phase-1-login-profile`.

Deletion proof required before removing any candidate artifact:

- SPA/API route is the default navigation path.
- Legacy route either redirects to SPA or has no runtime registration.
- Permissions match the relevant `SecureController.mayProceed()` behavior.
- Audit/status/log output parity is captured by tests or explicit verification.
- Servlet/JSP/helper references are gone before file deletion.

Candidate artifacts:

| Type | Path | Symbol | Route/mapping |
|---|---|---|---|
| jsp-view | web/src/main/webapp/WEB-INF/jsp/login-include/footer.jsp | footer.jsp | /WEB-INF/jsp/login-include/footer.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/login-include/login-alertbox.jsp | login-alertbox.jsp | /WEB-INF/jsp/login-include/login-alertbox.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/login-include/login-footer.jsp | login-footer.jsp | /WEB-INF/jsp/login-include/login-footer.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/login/login.jsp | login.jsp | /WEB-INF/jsp/login/login.jsp |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/AccountController.java | org.researchedc.controller.AccountController:115 | /login |

## Unknown Items

These require manual owner/category assignment before deletion work:

| Type | Path | Symbol | Route/mapping |
|---|---|---|---|
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/configuration.jsp | configuration.jsp | /WEB-INF/jsp/admin/configuration.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/index.jsp | index.jsp | /WEB-INF/jsp/admin/index.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/alertbox.jsp | alertbox.jsp | /WEB-INF/jsp/include/alertbox.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showDateTimeInput.jsp | showDateTimeInput.jsp | /WEB-INF/jsp/include/showDateTimeInput.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showHiddenInput.jsp | showHiddenInput.jsp | /WEB-INF/jsp/include/showHiddenInput.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showPageMessages.jsp | showPageMessages.jsp | /WEB-INF/jsp/include/showPageMessages.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showPopUp.jsp | showPopUp.jsp | /WEB-INF/jsp/include/showPopUp.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showPopUp2.jsp | showPopUp2.jsp | /WEB-INF/jsp/include/showPopUp2.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showSideMessage.jsp | showSideMessage.jsp | /WEB-INF/jsp/include/showSideMessage.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showTable.jsp | showTable.jsp | /WEB-INF/jsp/include/showTable.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showTableWithTab.jsp | showTableWithTab.jsp | /WEB-INF/jsp/include/showTableWithTab.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/sideAlert.jsp | sideAlert.jsp | /WEB-INF/jsp/include/sideAlert.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/sideIcons.jsp | sideIcons.jsp | /WEB-INF/jsp/include/sideIcons.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/sideInfo.jsp | sideInfo.jsp | /WEB-INF/jsp/include/sideInfo.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/tech-admin-header.jsp | tech-admin-header.jsp | /WEB-INF/jsp/include/tech-admin-header.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/workflow.jsp | workflow.jsp | /WEB-INF/jsp/include/workflow.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/menu.jsp | menu.jsp | /WEB-INF/jsp/menu.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/showInfo.jsp | showInfo.jsp | /WEB-INF/jsp/showInfo.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/showMessage.jsp | showMessage.jsp | /WEB-INF/jsp/showMessage.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/taglibs.jsp | taglibs.jsp | /WEB-INF/jsp/taglibs.jsp |
| legacy-servlet | web/src/main/java/org/researchedc/control/MainMenuServlet.java | org.researchedc.control.MainMenuServlet | /MainMenu |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/AdminSystemServlet.java | org.researchedc.control.admin.AdminSystemServlet | /AdminSystem |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/DeleteUserServlet.java | org.researchedc.control.admin.DeleteUserServlet | /DeleteUser |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/UnLockUserServlet.java | org.researchedc.control.admin.UnLockUserServlet | /UnLockUser |
| legacy-servlet | web/src/main/java/org/researchedc/control/rule/ExecuteCrossEditCheckServlet.java | org.researchedc.control.rule.ExecuteCrossEditCheckServlet | /ExecuteCrossEditCheck |
| scheduled-job | shared/src/main/java/org/researchedc/job/XsltTransformJob.java | org.researchedc.job.XsltTransformJob | (quartz/job class) |
| scheduled-job | web/src/main/java/org/researchedc/web/job/ExampleSpringJob.java | org.researchedc.web.job.ExampleSpringJob | (quartz/job class) |
| scheduled-job | web/src/main/java/org/researchedc/web/job/ImportSpringJob.java | org.researchedc.web.job.ImportSpringJob | (quartz/job class) |
| shared-service | shared/src/main/java/org/researchedc/service/JobTriggerService.java | org.researchedc.service.JobTriggerService | (shared service) |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/RuleController.java | org.researchedc.controller.RuleController:71 | /rule |

_Generated by `scripts/ci/generate-legacy-inventory.py`._
