package org.researchedc.module.analytics.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.dashboard.dto.TasksResponse;
import org.researchedc.module.dashboard.service.DashboardService;
import org.researchedc.module.ecoa.dto.EcoaAdherenceSummaryDTO;
import org.researchedc.module.ecoa.service.EcoaService;
import org.researchedc.module.recruit.dto.CandidateDTO;
import org.researchedc.module.recruit.enums.CandidateStatus;
import org.researchedc.module.recruit.service.RecruitService;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private StudySubjectRepository studySubjectRepository;
    @Mock private RecruitService recruitService;
    @Mock private EcoaService ecoaService;
    @Mock private DashboardService dashboardService;
    @Mock private CurrentStudyAccessService currentStudyAccessService;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(studySubjectRepository, recruitService, ecoaService,
                dashboardService, currentStudyAccessService);
    }

    @Test
    void dashboard_aggregatesStudyMetrics() {
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(true);
        when(studySubjectRepository.countByStudyId(10)).thenReturn(12L);
        when(studySubjectRepository.countByStudyIdAndStatusId(10, 1)).thenReturn(10L);
        when(studySubjectRepository.countByStudyIdAndDateCreatedAfter(eq(10), any())).thenReturn(3L);
        EcoaAdherenceSummaryDTO adherence = new EcoaAdherenceSummaryDTO();
        adherence.setTotal(8);
        adherence.setCompleted(5);
        adherence.setOverdue(1);
        when(ecoaService.summarizeAdherence(10, 42)).thenReturn(adherence);
        when(recruitService.listCandidates(10, null, 42)).thenReturn(List.of(
                candidate(CandidateStatus.ELIGIBLE),
                candidate(CandidateStatus.CONVERTED),
                candidate(CandidateStatus.NEW)));
        when(dashboardService.getTasks()).thenReturn(new TasksResponse(4, 2, 1, 0));

        var result = service.dashboard(10, 42);

        assertEquals(10, result.getStudyId());
        assertEquals(3, result.getEnrollment().size());
        assertEquals(12L, result.getEnrollment().get(0).getValue());
        assertEquals(6, result.getParticipantWork().size());
        assertEquals(3, result.getOperations().size());
    }

    @Test
    void dashboard_whenNoStudyReadAccess_denies() {
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.dashboard(10, 42));
        verifyNoInteractions(studySubjectRepository, recruitService, ecoaService, dashboardService);
    }

    private static CandidateDTO candidate(CandidateStatus status) {
        CandidateDTO candidate = new CandidateDTO();
        candidate.setStatus(status);
        return candidate;
    }
}
