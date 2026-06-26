package org.researchedc.module.rule.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.TimeZone;

import org.researchedc.module.rule.dto.RuleCurrentDateResponse;
import org.springframework.stereotype.Service;

@Service
public class RuleScheduleService {

    static final int DEFAULT_RUN_TIME = 20;

    private final Clock clock;

    public RuleScheduleService() {
        this(Clock.systemDefaultZone());
    }

    RuleScheduleService(Clock clock) {
        this.clock = clock;
    }

    public boolean shouldRun(String serverZoneId, String subjectZoneId, int runTime, int serverTime) {
        validateHour("runTime", runTime);
        validateHour("serverTime", serverTime);

        TimeZone serverZone = toTimeZone("serverZoneId", serverZoneId, false);
        TimeZone subjectZone = toTimeZone("subjectZoneId", subjectZoneId, true);

        int timeDifference = (serverZone.getRawOffset() + serverZone.getDSTSavings()
                - (subjectZone.getRawOffset() + subjectZone.getDSTSavings())) / (1000 * 60 * 60);
        int adjustedRunTime = runTime + timeDifference;
        if (adjustedRunTime > 23) {
            adjustedRunTime -= 24;
        }
        if (adjustedRunTime < 0) {
            adjustedRunTime += 24;
        }
        return serverTime == adjustedRunTime;
    }

    public int defaultRunTime() {
        return DEFAULT_RUN_TIME;
    }

    public RuleCurrentDateResponse currentDates(String serverZoneId, String subjectZoneId) {
        ZoneId subjectZone = toZoneId("subjectZoneId", subjectZoneId, true);
        ZoneId serverZone = toZoneId("serverZoneId", serverZoneId, false);
        String effectiveServerZoneId = normalizeZoneId("serverZoneId", serverZoneId, false);

        return new RuleCurrentDateResponse(
                LocalDate.now(clock.withZone(subjectZone)).toString(),
                effectiveServerZoneId,
                LocalDate.now(clock.withZone(serverZone)).toString());
    }

    private static void validateHour(String field, int value) {
        if (value < 0 || value > 23) {
            throw new IllegalArgumentException(field + " must be between 0 and 23");
        }
    }

    private static TimeZone toTimeZone(String field, String zoneId, boolean useDefaultWhenBlank) {
        return TimeZone.getTimeZone(toZoneId(field, zoneId, useDefaultWhenBlank));
    }

    private static ZoneId toZoneId(String field, String zoneId, boolean useDefaultWhenBlank) {
        String normalized = normalizeZoneId(field, zoneId, useDefaultWhenBlank);
        try {
            return ZoneId.of(normalized);
        } catch (ZoneRulesException e) {
            throw new IllegalArgumentException(field + " is not a valid time zone: " + normalized, e);
        }
    }

    private static String normalizeZoneId(String field, String zoneId, boolean useDefaultWhenBlank) {
        if (zoneId == null || zoneId.isBlank()) {
            if (useDefaultWhenBlank) {
                return TimeZone.getDefault().getID();
            }
            throw new IllegalArgumentException(field + " is required");
        }
        return zoneId;
    }
}
