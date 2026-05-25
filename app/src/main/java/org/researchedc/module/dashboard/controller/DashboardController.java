package org.researchedc.module.dashboard.controller;

import java.util.List;
import org.researchedc.module.dashboard.dto.BootstrapResponse;
import org.researchedc.module.dashboard.dto.RecentActivityItem;
import org.researchedc.module.dashboard.dto.StatusResponse;
import org.researchedc.module.dashboard.dto.TasksResponse;
import org.researchedc.module.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/bootstrap")
    public ResponseEntity<BootstrapResponse> getBootstrap() {
        return ResponseEntity.ok(dashboardService.getBootstrap());
    }

    @GetMapping("/tasks")
    public ResponseEntity<TasksResponse> getTasks() {
        return ResponseEntity.ok(dashboardService.getTasks());
    }

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus() {
        return ResponseEntity.ok(dashboardService.getStatus());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentActivityItem>> getRecent() {
        return ResponseEntity.ok(dashboardService.getRecent());
    }
}
