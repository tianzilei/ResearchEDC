package org.researchedc.module.rule.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.researchedc.module.rule.dto.RuleCurrentDateResponse;

class RuleScheduleServiceTest {

    @Test
    void shouldRun_matchesLegacyTimezoneAdjustment() {
        RuleScheduleService service = new RuleScheduleService(fixedClock());

        assertTrue(service.shouldRun("UTC", "America/New_York", 20, 0));
        assertFalse(service.shouldRun("UTC", "America/New_York", 20, 1));
    }

    @Test
    void shouldRun_wrapsAcrossMidnight() {
        RuleScheduleService service = new RuleScheduleService(fixedClock());

        assertTrue(service.shouldRun("America/New_York", "UTC", 2, 22));
    }

    @Test
    void shouldRun_defaultsBlankSubjectZoneToJvmDefault() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            RuleScheduleService service = new RuleScheduleService(fixedClock());

            assertTrue(service.shouldRun("UTC", "", 20, 20));
        } finally {
            TimeZone.setDefault(original);
        }
    }

    @Test
    void defaultRunTime_matchesLegacyDefault() {
        RuleScheduleService service = new RuleScheduleService(fixedClock());

        assertEquals(20, service.defaultRunTime());
    }

    @Test
    void currentDates_returnsSubjectAndServerDates() {
        RuleScheduleService service = new RuleScheduleService(fixedClock());

        RuleCurrentDateResponse response = service.currentDates("UTC", "America/New_York");

        assertEquals("2026-06-07", response.serverDate());
        assertEquals("UTC", response.serverZoneId());
        assertEquals("2026-06-06", response.ssDate());
    }

    @Test
    void invalidHour_throwsException() {
        RuleScheduleService service = new RuleScheduleService(fixedClock());

        assertThrows(IllegalArgumentException.class,
                () -> service.shouldRun("UTC", "UTC", 24, 20));
    }

    @Test
    void missingServerZone_throwsException() {
        RuleScheduleService service = new RuleScheduleService(fixedClock());

        assertThrows(IllegalArgumentException.class,
                () -> service.currentDates(null, "UTC"));
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-06-07T01:30:00Z"), ZoneOffset.UTC);
    }
}
