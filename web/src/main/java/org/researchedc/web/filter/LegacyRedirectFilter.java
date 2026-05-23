package org.researchedc.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Servlet Filter intercepting known legacy servlet URLs and redirecting
 * to equivalent React SPA routes. Runs before all web servlets.
 * Each mapping here deactivates one legacy servlet for normal navigation.
 */
public final class LegacyRedirectFilter implements Filter {

    private static final Map<String, String> REDIRECT_MAP = new LinkedHashMap<>();
    static {
        REDIRECT_MAP.put("/MainMenu", "/app/dashboard");
        REDIRECT_MAP.put("/Login", "/login");
        REDIRECT_MAP.put("/Logout", "/login");
        REDIRECT_MAP.put("/ListStudy", "/app/studies");
        REDIRECT_MAP.put("/CreateStudy", "/app/studies/create");
        REDIRECT_MAP.put("/ManageStudy1", "/app/studies");
        REDIRECT_MAP.put("/UpdateStudy", "/app/studies");
        REDIRECT_MAP.put("/ListStudySubject", "/app/subjects");
        REDIRECT_MAP.put("/ListStudySubjects", "/app/subjects");
        REDIRECT_MAP.put("/AddNewSubject", "/app/subjects");
        REDIRECT_MAP.put("/SubmitData", "/app/subjects");
        REDIRECT_MAP.put("/ListCRF", "/app/crfs");
        REDIRECT_MAP.put("/ViewCRF", "/app/crfs");
        REDIRECT_MAP.put("/ListUserAccounts", "/app/admin/users");
        REDIRECT_MAP.put("/CreateUserAccount", "/app/admin/users");
        REDIRECT_MAP.put("/ViewUserAccount", "/app/admin/users");
        REDIRECT_MAP.put("/EditUserAccount", "/app/admin/users");
        REDIRECT_MAP.put("/AuditDatabase", "/app/admin/audit-log");
        REDIRECT_MAP.put("/AuditUserActivity", "/app/admin/audit-log");
        REDIRECT_MAP.put("/StudyAuditLog", "/app/admin/audit-log");
        REDIRECT_MAP.put("/SystemStatus", "/app/admin/system");
        REDIRECT_MAP.put("/Configure", "/app/admin/system");
        REDIRECT_MAP.put("/ConfigurePasswordRequirements", "/app/admin/system");
        REDIRECT_MAP.put("/AdminSystem", "/app/admin");
        REDIRECT_MAP.put("/CreateCRF", "/app/admin/crf-library");
        REDIRECT_MAP.put("/UpdateCRF", "/app/admin/crf-library");
        REDIRECT_MAP.put("/CreateCRFVersion", "/app/admin/crf-library");
        REDIRECT_MAP.put("/ViewCRFVersion", "/app/admin/crf-library");
        REDIRECT_MAP.put("/ViewScheduler", "/app/admin/jobs");
        REDIRECT_MAP.put("/ViewJobs", "/app/admin/jobs");
        REDIRECT_MAP.put("/ViewAllJobs", "/app/admin/jobs");
        REDIRECT_MAP.put("/ViewSingleJob", "/app/admin/jobs");
        REDIRECT_MAP.put("/CreateJobExport", "/app/admin/jobs");
        REDIRECT_MAP.put("/CreateJobImport", "/app/admin/jobs");
        REDIRECT_MAP.put("/ExtractDatasetsMain", "/app/data-export");
        REDIRECT_MAP.put("/ViewDatasets", "/app/data-export/datasets");
        REDIRECT_MAP.put("/CreateDataset", "/app/data-export/datasets");
        REDIRECT_MAP.put("/ExportDataset", "/app/data-export");
        REDIRECT_MAP.put("/ChangeStudy", "/app/profile");
        REDIRECT_MAP.put("/UpdateProfile", "/app/profile");
        REDIRECT_MAP.put("/Contact", "/app/profile");
        REDIRECT_MAP.put("/ListSite", "/app/studies");
        REDIRECT_MAP.put("/CreateSubStudy", "/app/studies");
        REDIRECT_MAP.put("/ListEventDefinition", "/app/studies");
        REDIRECT_MAP.put("/DefineStudyEvent", "/app/studies");
        REDIRECT_MAP.put("/ListSubjectGroupClass", "/app/studies");
        REDIRECT_MAP.put("/CreateSubjectGroupClass", "/app/studies");
        REDIRECT_MAP.put("/Enterprise", "/login");
        REDIRECT_MAP.put("/RequestPassword", "/login");
        REDIRECT_MAP.put("/RequestAccount", "/login");
        REDIRECT_MAP.put("/ResetPassword", "/login");
        REDIRECT_MAP.put("/AdministrativeEditing", "/app/admin");
        REDIRECT_MAP.put("/TableOfContents", "/app/subjects");
        REDIRECT_MAP.put("/RemoveStudy", "/app/studies");
        REDIRECT_MAP.put("/RestoreStudy", "/app/studies");
        REDIRECT_MAP.put("/ViewStudy", "/app/studies");
        REDIRECT_MAP.put("/ListSubject", "/app/subjects");
        REDIRECT_MAP.put("/ViewSubject", "/app/subjects");
        REDIRECT_MAP.put("/UpdateSubject", "/app/subjects");
        REDIRECT_MAP.put("/RemoveSubject", "/app/subjects");
        REDIRECT_MAP.put("/RestoreSubject", "/app/subjects");
        REDIRECT_MAP.put("/SignStudySubject", "/app/subjects");
        REDIRECT_MAP.put("/ReassignStudySubject", "/app/subjects");
        REDIRECT_MAP.put("/UpdateStudySubject", "/app/subjects");
        REDIRECT_MAP.put("/RemoveStudySubject", "/app/subjects");
        REDIRECT_MAP.put("/RestoreStudySubject", "/app/subjects");
        REDIRECT_MAP.put("/ViewStudySubject", "/app/subjects");
        REDIRECT_MAP.put("/ViewStudySubjectAuditLog", "/app/subjects");
        REDIRECT_MAP.put("/RemoveSite", "/app/studies");
        REDIRECT_MAP.put("/RestoreSite", "/app/studies");
        REDIRECT_MAP.put("/ViewSite", "/app/studies");
        REDIRECT_MAP.put("/RemoveEventDefinition", "/app/studies");
        REDIRECT_MAP.put("/RestoreEventDefinition", "/app/studies");
        REDIRECT_MAP.put("/LockEventDefinition", "/app/studies");
        REDIRECT_MAP.put("/UnlockEventDefinition", "/app/studies");
        REDIRECT_MAP.put("/ViewEventDefinition", "/app/studies");
        REDIRECT_MAP.put("/ViewEventDefinitionReadOnly", "/app/studies");
        REDIRECT_MAP.put("/ViewStudyEvents", "/app/studies");
        REDIRECT_MAP.put("/ListStudyUser", "/app/studies");
        REDIRECT_MAP.put("/ViewStudyUser", "/app/studies");
        REDIRECT_MAP.put("/SetStudyUserRole", "/app/studies");
        REDIRECT_MAP.put("/RemoveStudyUserRole", "/app/studies");
        REDIRECT_MAP.put("/RestoreStudyUserRole", "/app/studies");
        REDIRECT_MAP.put("/EditStudyUserRole", "/app/studies");
        REDIRECT_MAP.put("/ViewSubjectGroupClass", "/app/studies");
        REDIRECT_MAP.put("/RemoveSubjectGroupClass", "/app/studies");
        REDIRECT_MAP.put("/RestoreSubjectGroupClass", "/app/studies");
        REDIRECT_MAP.put("/UpdateSubjectGroupClass", "/app/studies");
        REDIRECT_MAP.put("/RemoveCRF", "/app/admin/crf-library");
        REDIRECT_MAP.put("/RestoreCRF", "/app/admin/crf-library");
        REDIRECT_MAP.put("/RemoveCRFVersion", "/app/admin/crf-library");
        REDIRECT_MAP.put("/RestoreCRFVersion", "/app/admin/crf-library");
        REDIRECT_MAP.put("/LockCRFVersion", "/app/admin/crf-library");
        REDIRECT_MAP.put("/UnlockCRFVersion", "/app/admin/crf-library");
        REDIRECT_MAP.put("/BatchCRFMigration", "/app/admin/crf-library");
        REDIRECT_MAP.put("/RemoveDataset", "/app/data-export/datasets");
        REDIRECT_MAP.put("/RestoreDataset", "/app/data-export/datasets");
        REDIRECT_MAP.put("/EditDataset", "/app/data-export/datasets");
        REDIRECT_MAP.put("/EditFilter", "/app/data-export");
        REDIRECT_MAP.put("/RemoveFilter", "/app/data-export");
        REDIRECT_MAP.put("/RestoreFilter", "/app/data-export");
        REDIRECT_MAP.put("/TechAdmin", "/app/admin");
        REDIRECT_MAP.put("/ViewLogMessage", "/app/admin");
        REDIRECT_MAP.put("/ViewTableOfContent", "/app/studies");
        REDIRECT_MAP.put("/ListEventsForSubjects", "/app/subjects");
        REDIRECT_MAP.put("/ViewNotes", "/app/subjects");
        REDIRECT_MAP.put("/ViewNote", "/app/subjects");
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Only redirect GET/HEAD — POST requests are form submissions
        // that need the servlet for processing
        String method = req.getMethod();
        if (!"GET".equals(method) && !"HEAD".equals(method)) {
            chain.doFilter(request, response);
            return;
        }

        String path = req.getRequestURI().substring(req.getContextPath().length());
        String target = REDIRECT_MAP.get(path);
        if (target != null) {
            resp.sendRedirect(req.getContextPath() + target);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}