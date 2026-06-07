# Legacy Workflow Inventory

Generated: 2026-06-07 16:10:10 UTC

Full CSV inventory: `legacy-workflow-inventory.csv`

## Summary By Type

| Artifact type | Count |
|---|---:|
| `dao-implementation` | 114 |
| `dao-spi` | 66 |
| `dao-support` | 6 |
| `jsp-view` | 419 |
| `legacy-servlet` | 189 |
| `scheduled-job` | 6 |
| `shared-service` | 60 |
| `soap-endpoint` | 7 |
| `spring-mvc-route` | 92 |

## Summary By Classification

| Classification | Count |
|---|---:|
| `keep compatibility` | 125 |
| `replace` | 764 |
| `unknown` | 70 |

## Summary By Phase Slice

| Phase slice | Count |
|---|---:|
| `phase-0-inventory-and-gates` | 62 |
| `phase-1-admin-read-only` | 50 |
| `phase-1-admin-write` | 6 |
| `phase-1-crf-metadata` | 105 |
| `phase-1-data-entry-discrepancy` | 100 |
| `phase-1-export-dataset-filter` | 73 |
| `phase-1-import-export-compatibility` | 52 |
| `phase-1-layout-fragments` | 1 |
| `phase-1-login-profile` | 35 |
| `phase-1-study-subject-event` | 216 |
| `phase-2-soap-retirement` | 7 |
| `phase-3-dao-implementation-deletion` | 186 |
| `phase-4-shared-service-deletion` | 66 |

## First Phase 1 Candidate Slice

Recommended slice: `phase-1-admin-read-only`.

Deletion proof required before removing any candidate artifact:

- SPA/API route is the default navigation path.
- Legacy route either redirects to SPA or has no runtime registration.
- Permissions match the relevant `SecureController.mayProceed()` behavior.
- Audit/status/log output parity is captured by tests or explicit verification.
- Servlet/JSP/helper references are gone before file deletion.

Candidate artifacts:

| Type | Path | Symbol | Route/mapping |
|---|---|---|---|
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/auditDatabase.jsp | auditDatabase.jsp | /WEB-INF/jsp/admin/auditDatabase.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/auditItem.jsp | auditItem.jsp | /WEB-INF/jsp/admin/auditItem.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/auditLogStudy.jsp | auditLogStudy.jsp | /WEB-INF/jsp/admin/auditLogStudy.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/auditLogStudyOld.jsp | auditLogStudyOld.jsp | /WEB-INF/jsp/admin/auditLogStudyOld.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/auditLogUser.jsp | auditLogUser.jsp | /WEB-INF/jsp/admin/auditLogUser.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/auditUserActivity.jsp | auditUserActivity.jsp | /WEB-INF/jsp/admin/auditUserActivity.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/showAuditEventJobRow.jsp | showAuditEventJobRow.jsp | /WEB-INF/jsp/admin/showAuditEventJobRow.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/showAuditEventRow.jsp | showAuditEventRow.jsp | /WEB-INF/jsp/admin/showAuditEventRow.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/showAuditEventStudyRow.jsp | showAuditEventStudyRow.jsp | /WEB-INF/jsp/admin/showAuditEventStudyRow.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/studyAuditLog.jsp | studyAuditLog.jsp | /WEB-INF/jsp/admin/studyAuditLog.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/systemStatus.jsp | systemStatus.jsp | /WEB-INF/jsp/admin/systemStatus.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/viewAllJobs.jsp | viewAllJobs.jsp | /WEB-INF/jsp/admin/viewAllJobs.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/viewJobs.jsp | viewJobs.jsp | /WEB-INF/jsp/admin/viewJobs.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/viewLogMessage.jsp | viewLogMessage.jsp | /WEB-INF/jsp/admin/viewLogMessage.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/viewScheduler.jsp | viewScheduler.jsp | /WEB-INF/jsp/admin/viewScheduler.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/viewSingleJob.jsp | viewSingleJob.jsp | /WEB-INF/jsp/admin/viewSingleJob.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showAuditEntityLink.jsp | showAuditEntityLink.jsp | /WEB-INF/jsp/include/showAuditEntityLink.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/managestudy/viewStudySubjectAudit.jsp | viewStudySubjectAudit.jsp | /WEB-INF/jsp/managestudy/viewStudySubjectAudit.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/submit/viewRuleSetAudits.jsp | viewRuleSetAudits.jsp | /WEB-INF/jsp/submit/viewRuleSetAudits.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/techadmin/index.jsp | index.jsp | /WEB-INF/jsp/techadmin/index.jsp |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/AuditDatabaseServlet.java | org.researchedc.control.admin.AuditDatabaseServlet | /AuditDatabase |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/AuditLogStudyServlet.java | org.researchedc.control.admin.AuditLogStudyServlet | /AuditLogStudy |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/AuditLogUserServlet.java | org.researchedc.control.admin.AuditLogUserServlet | /AuditLogUser |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/AuditUserActivityServlet.java | org.researchedc.control.admin.AuditUserActivityServlet | /AuditUserActivity |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/SystemStatusServlet.java | org.researchedc.control.admin.SystemStatusServlet | /SystemStatus |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/ViewAllJobsServlet.java | org.researchedc.control.admin.ViewAllJobsServlet | /ViewAllJobs |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/ViewJobServlet.java | org.researchedc.control.admin.ViewJobServlet | /ViewJob |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/ViewLogMessageServlet.java | org.researchedc.control.admin.ViewLogMessageServlet | /ViewLogMessage |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/ViewSingleJobServlet.java | org.researchedc.control.admin.ViewSingleJobServlet | /ViewSingleJob |
| legacy-servlet | web/src/main/java/org/researchedc/control/managestudy/ExportExcelStudySubjectAuditLogServlet.java | org.researchedc.control.managestudy.ExportExcelStudySubjectAuditLogServlet | /ExportExcelStudySubjectAuditLog |
| legacy-servlet | web/src/main/java/org/researchedc/control/managestudy/StudyAuditLogServlet.java | org.researchedc.control.managestudy.StudyAuditLogServlet | /StudyAuditLog |
| legacy-servlet | web/src/main/java/org/researchedc/control/managestudy/ViewItemAuditLogServlet.java | org.researchedc.control.managestudy.ViewItemAuditLogServlet | /ViewItemAuditLog |
| legacy-servlet | web/src/main/java/org/researchedc/control/managestudy/ViewStudySubjectAuditLogServlet.java | org.researchedc.control.managestudy.ViewStudySubjectAuditLogServlet | /ViewStudySubjectAuditLog |
| legacy-servlet | web/src/main/java/org/researchedc/control/submit/ViewRuleSetAuditServlet.java | org.researchedc.control.submit.ViewRuleSetAuditServlet | /ViewRuleSetAudit |
| legacy-servlet | web/src/main/java/org/researchedc/control/techadmin/TechAdminServlet.java | org.researchedc.control.techadmin.TechAdminServlet | /TechAdmin |
| legacy-servlet | web/src/main/java/org/researchedc/control/techadmin/ViewSchedulerServlet.java | org.researchedc.control.techadmin.ViewSchedulerServlet | /ViewScheduler |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/AccountController.java | org.researchedc.controller.AccountController:1018 | /auditcrc |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:161 | /config |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:310 | /extract |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:402 | /modules |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:466 | /modules/participate |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:505 | /modules/randomize |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:548 | /modules/webservices |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:592 | /modules/ruledesigner |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:61 | /auth/api/v1/system |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:646 | /modules/datamart |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:691 | /modules/auth |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:754 | /filesystem |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:799 | /database |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/SystemController.java | org.researchedc.controller.SystemController:87 | /systemstatus |

## Unknown Items

These require manual owner/category assignment before deletion work:

| Type | Path | Symbol | Route/mapping |
|---|---|---|---|
| jsp-view | web/src/main/webapp/WEB-INF/jsp/403.jsp | 403.jsp | /WEB-INF/jsp/403.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/404.jsp | 404.jsp | /WEB-INF/jsp/404.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/access_confirmation.jsp | access_confirmation.jsp | /WEB-INF/jsp/access_confirmation.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/configuration.jsp | configuration.jsp | /WEB-INF/jsp/admin/configuration.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/index.jsp | index.jsp | /WEB-INF/jsp/admin/index.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/admin/showJobRow.jsp | showJobRow.jsp | /WEB-INF/jsp/admin/showJobRow.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/error.jsp | error.jsp | /WEB-INF/jsp/error.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/admin-head-prev.jsp | admin-head-prev.jsp | /WEB-INF/jsp/include/admin-head-prev.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/admin-header.jsp | admin-header.jsp | /WEB-INF/jsp/include/admin-header.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/alertbox.jsp | alertbox.jsp | /WEB-INF/jsp/include/alertbox.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/breadcrumb.jsp | breadcrumb.jsp | /WEB-INF/jsp/include/breadcrumb.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/footer-inactive.jsp | footer-inactive.jsp | /WEB-INF/jsp/include/footer-inactive.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/footer.jsp | footer.jsp | /WEB-INF/jsp/include/footer.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/home-header.jsp | home-header.jsp | /WEB-INF/jsp/include/home-header.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/navBar.jsp | navBar.jsp | /WEB-INF/jsp/include/navBar.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showDateTimeInput.jsp | showDateTimeInput.jsp | /WEB-INF/jsp/include/showDateTimeInput.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showEntities.jsp | showEntities.jsp | /WEB-INF/jsp/include/showEntities.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showHiddenInput.jsp | showHiddenInput.jsp | /WEB-INF/jsp/include/showHiddenInput.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showMessages.jsp | showMessages.jsp | /WEB-INF/jsp/include/showMessages.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showPageMessages.jsp | showPageMessages.jsp | /WEB-INF/jsp/include/showPageMessages.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showPanel.jsp | showPanel.jsp | /WEB-INF/jsp/include/showPanel.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showPopUp.jsp | showPopUp.jsp | /WEB-INF/jsp/include/showPopUp.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showPopUp2.jsp | showPopUp2.jsp | /WEB-INF/jsp/include/showPopUp2.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showPresetValueText.jsp | showPresetValueText.jsp | /WEB-INF/jsp/include/showPresetValueText.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showSideMessage.jsp | showSideMessage.jsp | /WEB-INF/jsp/include/showSideMessage.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showTable.jsp | showTable.jsp | /WEB-INF/jsp/include/showTable.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showTableNewDomain.jsp | showTableNewDomain.jsp | /WEB-INF/jsp/include/showTableNewDomain.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showTableWithTab.jsp | showTableWithTab.jsp | /WEB-INF/jsp/include/showTableWithTab.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showTerms.jsp | showTerms.jsp | /WEB-INF/jsp/include/showTerms.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/showTextInput.jsp | showTextInput.jsp | /WEB-INF/jsp/include/showTextInput.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/sideAlert.jsp | sideAlert.jsp | /WEB-INF/jsp/include/sideAlert.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/sideIcons.jsp | sideIcons.jsp | /WEB-INF/jsp/include/sideIcons.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/sideInfo.jsp | sideInfo.jsp | /WEB-INF/jsp/include/sideInfo.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/sideInfo_prev.jsp | sideInfo_prev.jsp | /WEB-INF/jsp/include/sideInfo_prev.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/sidebar.jsp | sidebar.jsp | /WEB-INF/jsp/include/sidebar.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/tech-admin-header.jsp | tech-admin-header.jsp | /WEB-INF/jsp/include/tech-admin-header.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/userbox-inactive.jsp | userbox-inactive.jsp | /WEB-INF/jsp/include/userbox-inactive.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/userbox.jsp | userbox.jsp | /WEB-INF/jsp/include/userbox.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/viewRuleAssignmentSide.jsp | viewRuleAssignmentSide.jsp | /WEB-INF/jsp/include/viewRuleAssignmentSide.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/viewRuleAssignmentSideInfo.jsp | viewRuleAssignmentSideInfo.jsp | /WEB-INF/jsp/include/viewRuleAssignmentSideInfo.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/include/workflow.jsp | workflow.jsp | /WEB-INF/jsp/include/workflow.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/listCurrentScheduledJobs.jsp | listCurrentScheduledJobs.jsp | /WEB-INF/jsp/listCurrentScheduledJobs.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/listLdapUsers.jsp | listLdapUsers.jsp | /WEB-INF/jsp/listLdapUsers.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/menu.jsp | menu.jsp | /WEB-INF/jsp/menu.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/showInfo.jsp | showInfo.jsp | /WEB-INF/jsp/showInfo.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/showMessage.jsp | showMessage.jsp | /WEB-INF/jsp/showMessage.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/taglibs.jsp | taglibs.jsp | /WEB-INF/jsp/taglibs.jsp |
| jsp-view | web/src/main/webapp/WEB-INF/jsp/user.jsp | user.jsp | /WEB-INF/jsp/user.jsp |
| jsp-view | web/src/main/webapp/decorator.jsp | decorator.jsp | /decorator.jsp |
| jsp-view | web/src/main/webapp/includes/allIcons.jsp | allIcons.jsp | /includes/allIcons.jsp |
| legacy-servlet | web/src/main/java/org/researchedc/control/MainMenuServlet.java | org.researchedc.control.MainMenuServlet | /MainMenu |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/AdminSystemServlet.java | org.researchedc.control.admin.AdminSystemServlet | /AdminSystem |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/DeleteUserServlet.java | org.researchedc.control.admin.DeleteUserServlet | /DeleteUser |
| legacy-servlet | web/src/main/java/org/researchedc/control/admin/UnLockUserServlet.java | org.researchedc.control.admin.UnLockUserServlet | /UnLockUser |
| legacy-servlet | web/src/main/java/org/researchedc/control/rule/ExecuteCrossEditCheckServlet.java | org.researchedc.control.rule.ExecuteCrossEditCheckServlet | /ExecuteCrossEditCheck |
| scheduled-job | shared/src/main/java/org/researchedc/job/XsltTransformJob.java | org.researchedc.job.XsltTransformJob | (quartz/job class) |
| scheduled-job | web/src/main/java/org/researchedc/web/job/ExampleSpringJob.java | org.researchedc.web.job.ExampleSpringJob | (quartz/job class) |
| scheduled-job | web/src/main/java/org/researchedc/web/job/ImportSpringJob.java | org.researchedc.web.job.ImportSpringJob | (quartz/job class) |
| scheduled-job | web/src/main/java/org/researchedc/web/job/XalanTransformJob.java | org.researchedc.web.job.XalanTransformJob | (quartz/job class) |
| scheduled-job | ws/src/main/java/org/researchedc/web/job/ExampleSpringJob.java | org.researchedc.web.job.ExampleSpringJob | (quartz/job class) |
| scheduled-job | ws/src/main/java/org/researchedc/web/job/ImportSpringJob.java | org.researchedc.web.job.ImportSpringJob | (quartz/job class) |
| shared-service | shared/src/main/java/org/researchedc/service/JobTriggerService.java | org.researchedc.service.JobTriggerService | (shared service) |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/IdtViewController.java | org.researchedc.controller.IdtViewController:148 | / |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/RuleController.java | org.researchedc.controller.RuleController:71 | /rule |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/ScheduledJobController.java | org.researchedc.controller.for:184 | /cancelScheduledJob |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/ScheduledJobController.java | org.researchedc.controller.for:64 | /listCurrentScheduledJobs |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/UserController.java | org.researchedc.controller.UserController:38 | /user |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/UserInfoController.java | org.researchedc.controller.UserInfoController:39 | /userinfo |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/user/LdapUserController.java | org.researchedc.controller.user.LdapUserController:34 | /listLdapUsers |
| spring-mvc-route | web/src/main/java/org/researchedc/controller/user/LdapUserController.java | org.researchedc.controller.user.LdapUserController:56 | /selectLdapUser |

_Generated by `scripts/ci/generate-legacy-inventory.py`._
