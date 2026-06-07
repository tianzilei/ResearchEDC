package org.researchedc.module.rule.controller;

import org.researchedc.module.rule.dto.RuleCurrentDateRequest;
import org.researchedc.module.rule.dto.RuleCurrentDateResponse;
import org.researchedc.module.rule.dto.RuleDefaultRunTimeResponse;
import org.researchedc.module.rule.dto.RuleScheduleCheckRequest;
import org.researchedc.module.rule.dto.RuleScheduleCheckResponse;
import org.researchedc.module.rule.service.RuleScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rules/schedule")
public class RuleScheduleController {

    private final RuleScheduleService ruleScheduleService;

    public RuleScheduleController(RuleScheduleService ruleScheduleService) {
        this.ruleScheduleService = ruleScheduleService;
    }

    @PostMapping("/check")
    public ResponseEntity<RuleScheduleCheckResponse> check(@RequestBody RuleScheduleCheckRequest request) {
        boolean result = ruleScheduleService.shouldRun(
                request.getServerZoneId(),
                request.getSubjectZoneId(),
                requireHour("runTime", request.getRunTime()),
                requireHour("serverTime", request.getServerTime()));
        if (result) {
            return ResponseEntity.ok(new RuleScheduleCheckResponse(true));
        }
        return ResponseEntity.badRequest().body(new RuleScheduleCheckResponse(false));
    }

    @PostMapping("/current-date")
    public ResponseEntity<RuleCurrentDateResponse> currentDate(@RequestBody RuleCurrentDateRequest request) {
        return ResponseEntity.ok(ruleScheduleService.currentDates(
                request.getServerZoneId(), request.getSubjectZoneId()));
    }

    @GetMapping("/default-runtime")
    public ResponseEntity<RuleDefaultRunTimeResponse> defaultRunTime() {
        return ResponseEntity.ok(new RuleDefaultRunTimeResponse(ruleScheduleService.defaultRunTime()));
    }

    private static int requireHour(String field, Integer value) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
