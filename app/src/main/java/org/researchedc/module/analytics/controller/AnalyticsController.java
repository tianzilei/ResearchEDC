package org.researchedc.module.analytics.controller;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.analytics.dto.AnalyticsDashboardDTO;
import org.researchedc.module.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUserUtils currentUserUtils;

    public AnalyticsController(AnalyticsService analyticsService, CurrentUserUtils currentUserUtils) {
        this.analyticsService = analyticsService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/dashboard")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<AnalyticsDashboardDTO> dashboard(@RequestParam Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.dashboard(studyId, currentUserId));
    }
}
