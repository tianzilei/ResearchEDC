package org.researchedc.module.dashboard.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.audit.dto.AuditLogDTO;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.dashboard.dto.BootstrapResponse;
import org.researchedc.module.dashboard.dto.BootstrapResponse.ModuleInfo;
import org.researchedc.module.dashboard.dto.BootstrapResponse.StudyInfo;
import org.researchedc.module.dashboard.dto.BootstrapResponse.UserInfo;
import org.researchedc.module.dashboard.dto.RecentActivityItem;
import org.researchedc.module.dashboard.dto.StatusResponse;
import org.researchedc.module.dashboard.dto.TasksResponse;
import org.researchedc.module.discrepancynote.service.DiscrepancyNoteService;
import org.researchedc.module.event.service.EventService;
import org.researchedc.module.identity.dto.RoleDTO;
import org.researchedc.module.identity.dto.UserDTO;
import org.researchedc.module.identity.service.IdentityService;
import org.researchedc.module.study.dto.StudySummaryDTO;
import org.researchedc.module.study.service.StudyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final CurrentUserUtils currentUserUtils;
    private final IdentityService identityService;
    private final StudyService studyService;
    private final EventService eventService;
    private final DiscrepancyNoteService discrepancyNoteService;
    private final AuditService auditService;

    public DashboardService(CurrentUserUtils currentUserUtils,
                            IdentityService identityService,
                            StudyService studyService,
                            EventService eventService,
                            DiscrepancyNoteService discrepancyNoteService,
                            AuditService auditService) {
        this.currentUserUtils = currentUserUtils;
        this.identityService = identityService;
        this.studyService = studyService;
        this.eventService = eventService;
        this.discrepancyNoteService = discrepancyNoteService;
        this.auditService = auditService;
    }

    public BootstrapResponse getBootstrap() {
        Integer userId = currentUserUtils.getCurrentUserId();
        UserDTO user = identityService.getUser(userId);
        List<RoleDTO> roles = identityService.getUserRoles(user.getUserName());

        Set<String> distinctRoleNames = new HashSet<>();
        for (RoleDTO r : roles) {
            if (r.getRoleName() != null) {
                distinctRoleNames.add(r.getRoleName());
            }
        }
        List<String> userRoles = new ArrayList<>(distinctRoleNames);

        List<StudyInfo> studies = new ArrayList<>();
        StudyInfo defaultStudy = null;

        List<StudySummaryDTO> allStudies = studyService.listStudies();
        for (StudySummaryDTO study : allStudies) {
            String matchedRole = findRoleForStudy(roles, study.getStudyId());
            if (matchedRole == null) continue;
            studies.add(new StudyInfo(study.getStudyId(), study.getName(),
                false, null, matchedRole));
            if (defaultStudy == null) {
                defaultStudy = new StudyInfo(study.getStudyId(), study.getName(),
                    false, null, matchedRole);
            }
        }

        List<ModuleInfo> modules = buildModules(userRoles);

        UserInfo userInfo = new UserInfo(
            user.getUserId(), user.getUserName(),
            user.getFirstName(), user.getLastName(), userRoles);

        return new BootstrapResponse(userInfo, studies, defaultStudy, modules);
    }

    public TasksResponse getTasks() {
        long pendingCrfs = eventService.countPendingCrfs();
        long pendingQueries = discrepancyNoteService.countOpenNotes();
        return new TasksResponse((int) pendingCrfs, (int) pendingQueries, 0, 0);
    }

    public StatusResponse getStatus() {
        return new StatusResponse("normal", "normal", null);
    }

    public List<RecentActivityItem> getRecent() {
        Page<AuditLogDTO> logs = auditService.listAuditLogs(
            null, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "performedDate")));
        return logs.getContent().stream()
            .map(this::toRecentItem)
            .toList();
    }

    private RecentActivityItem toRecentItem(AuditLogDTO log) {
        String type = log.getEventType() != null
            ? log.getEventType().name().toLowerCase() : "other";
        String label = log.getEntityLabel() != null ? log.getEntityLabel()
            : (log.getEntityType() != null ? log.getEntityType() : "");
        String desc = (log.getEntityType() != null ? log.getEntityType() : "")
            + (label.isEmpty() ? "" : ": " + label);
        if (log.getDetails() != null && !log.getDetails().isEmpty()) {
            desc = log.getDetails();
        }
        return new RecentActivityItem(
            type, desc, log.getPerformedDate(), null);
    }

    private List<ModuleInfo> buildModules(List<String> userRoles) {
        Set<String> allowed = new HashSet<>();
        for (String role : userRoles) {
            List<String> mods = ROLE_MODULES.get(role.toLowerCase());
            if (mods != null) allowed.addAll(mods);
        }

        List<ModuleInfo> result = new ArrayList<>();
        for (ModuleDefinition def : ALL_MODULES) {
            if (allowed.contains(def.key)) {
                result.add(new ModuleInfo(def.key, def.name, def.description,
                    def.path, def.priority));
            }
        }
        result.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
        return result;
    }

    private static String findRoleForStudy(List<RoleDTO> roles, Integer studyId) {
        for (RoleDTO role : roles) {
            if (studyId.equals(role.getStudyId())) {
                return role.getRoleName();
            }
        }
        return null;
    }

    private record ModuleDefinition(String key, String name, String description,
                                     String path, int priority) {}

    private static final List<ModuleDefinition> ALL_MODULES = List.of(
        new ModuleDefinition("subjects", "受试者管理", "入组、筛选、查看受试者状态", "/app/subjects", 1),
        new ModuleDefinition("crf", "CRF填写", "填写、保存、审核CRF", "/app/crfs", 2),
        new ModuleDefinition("randomization", "随机化", "受试者随机分配与随机化记录", "/app/randomization", 3),
        new ModuleDefinition("events", "访视管理", "计划访视、实际访视、访视窗口", "/app/events", 4),
        new ModuleDefinition("queries", "Query管理", "创建、回复、关闭Query", "/app/queries", 5),
        new ModuleDefinition("quality-checks", "数据质控", "缺失值、逻辑错误、异常值检查", "/app/quality-checks", 6),
        new ModuleDefinition("surveys", "问卷系统", "问卷模板、分发、填写、评分", "/app/questionnaires/templates", 7),
        new ModuleDefinition("exports", "数据导出", "CSV、Excel、审计、脱敏导出", "/app/data-export", 8),
        new ModuleDefinition("reports", "报表统计", "入组进度、CRF完成率、Query统计", "/app/reports", 9),
        new ModuleDefinition("audit-logs", "审计日志", "登录、修改、随机化、权限变更日志", "/app/audit-log", 10),
        new ModuleDefinition("study-mgmt", "研究管理", "研究配置、访视计划、CRF绑定", "/app/studies", 11),
        new ModuleDefinition("site-mgmt", "站点管理", "站点人员、权限、站点状态", "/app/admin", 12),
        new ModuleDefinition("users", "用户与权限", "用户、角色、账户修改审核", "/app/admin/users", 13),
        new ModuleDefinition("settings", "系统设置", "系统参数、邮件、备份、部署信息", "/app/admin/system", 14)
    );

    private static final Map<String, List<String>> ROLE_MODULES = Map.ofEntries(
        Map.entry("admin", List.of("subjects", "crf", "randomization", "events", "queries",
            "quality-checks", "surveys", "exports", "reports", "audit-logs",
            "study-mgmt", "site-mgmt", "users", "settings")),
        Map.entry("coordinator", List.of("subjects", "crf", "randomization", "events", "queries",
            "quality-checks", "surveys", "exports", "reports", "audit-logs", "study-mgmt")),
        Map.entry("investigator", List.of("subjects", "crf", "events", "queries", "surveys")),
        Map.entry("monitor", List.of("queries", "quality-checks", "surveys", "reports", "audit-logs")),
        Map.entry("dataManager", List.of("subjects", "crf", "events", "queries", "exports", "audit-logs")),
        Map.entry("dataEntry", List.of("subjects", "crf")),
        Map.entry("studyDirector", List.of("subjects", "crf", "study-mgmt", "exports", "audit-logs")),
        Map.entry("principalInvestigator", List.of("subjects", "crf", "events", "queries", "surveys", "reports", "audit-logs")),
        Map.entry("study_director", List.of("subjects", "crf", "randomization", "events", "queries",
            "quality-checks", "surveys", "exports", "reports", "audit-logs",
            "study-mgmt", "site-mgmt", "users", "settings"))
    );
}
