package org.researchedc.module.analytics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.analytics.dto.AnalyticsDashboardDTO;
import org.researchedc.module.analytics.dto.AnalyticsMetricDTO;
import org.researchedc.module.dashboard.dto.TasksResponse;
import org.researchedc.module.dashboard.service.DashboardService;
import org.researchedc.module.ecoa.dto.EcoaAdherenceSummaryDTO;
import org.researchedc.module.ecoa.service.EcoaService;
import org.researchedc.module.recruit.dto.CandidateDTO;
import org.researchedc.module.recruit.enums.CandidateStatus;
import org.researchedc.module.recruit.service.RecruitService;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final int STATUS_AVAILABLE = 1;

    private final StudySubjectRepository studySubjectRepository;
    private final RecruitService recruitService;
    private final EcoaService ecoaService;
    private final DashboardService dashboardService;
    private final CurrentStudyAccessService currentStudyAccessService;

    public AnalyticsService(StudySubjectRepository studySubjectRepository,
                            RecruitService recruitService,
                            EcoaService ecoaService,
                            DashboardService dashboardService,
                            CurrentStudyAccessService currentStudyAccessService) {
        this.studySubjectRepository = studySubjectRepository;
        this.recruitService = recruitService;
        this.ecoaService = ecoaService;
        this.dashboardService = dashboardService;
        this.currentStudyAccessService = currentStudyAccessService;
    }

    public AnalyticsDashboardDTO dashboard(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        AnalyticsDashboardDTO dto = new AnalyticsDashboardDTO();
        dto.setStudyId(studyId);
        dto.setEnrollment(enrollmentMetrics(studyId));
        dto.setParticipantWork(participantWorkMetrics(studyId, currentUserId));
        dto.setOperations(operationMetrics());
        return dto;
    }

    private List<AnalyticsMetricDTO> enrollmentMetrics(Integer studyId) {
        long enrolled = studySubjectRepository.countByStudyId(studyId);
        long active = studySubjectRepository.countByStudyIdAndStatusId(studyId, STATUS_AVAILABLE);
        long recent = studySubjectRepository.countByStudyIdAndDateCreatedAfter(studyId, LocalDateTime.now().minusDays(30));
        return List.of(
                new AnalyticsMetricDTO("enrolled", "Enrolled", enrolled, "subjects"),
                new AnalyticsMetricDTO("active", "Active", active, "subjects"),
                new AnalyticsMetricDTO("recent30d", "New 30d", recent, "subjects")
        );
    }

    private List<AnalyticsMetricDTO> participantWorkMetrics(Integer studyId, Integer currentUserId) {
        EcoaAdherenceSummaryDTO adherence = ecoaService.summarizeAdherence(studyId, currentUserId);
        List<CandidateDTO> candidates = recruitService.listCandidates(studyId, null, currentUserId);
        long eligible = candidates.stream().filter(candidate -> candidate.getStatus() == CandidateStatus.ELIGIBLE).count();
        long converted = candidates.stream().filter(candidate -> candidate.getStatus() == CandidateStatus.CONVERTED).count();
        return List.of(
                new AnalyticsMetricDTO("ecoaTotal", "eCOA Total", adherence.getTotal(), "tasks"),
                new AnalyticsMetricDTO("ecoaCompleted", "eCOA Completed", adherence.getCompleted(), "tasks"),
                new AnalyticsMetricDTO("ecoaOverdue", "eCOA Overdue", adherence.getOverdue(), "tasks"),
                new AnalyticsMetricDTO("candidates", "Candidates", candidates.size(), "records"),
                new AnalyticsMetricDTO("eligibleCandidates", "Eligible", eligible, "records"),
                new AnalyticsMetricDTO("convertedCandidates", "Converted", converted, "records")
        );
    }

    private List<AnalyticsMetricDTO> operationMetrics() {
        TasksResponse tasks = dashboardService.getTasks();
        return List.of(
                new AnalyticsMetricDTO("pendingCrfs", "Pending CRFs", tasks.getPendingCrfs(), "items"),
                new AnalyticsMetricDTO("openQueries", "Open Queries", tasks.getPendingQueries(), "items"),
                new AnalyticsMetricDTO("pendingReviews", "Pending Reviews", tasks.getPendingReviews(), "items")
        );
    }

    private void requireReadAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canReadStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have read access to this study");
        }
    }
}
