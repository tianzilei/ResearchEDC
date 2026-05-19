package org.akaza.openclinica.module.event.infrastructure;

import org.springframework.stereotype.Component;

@Component
public class LegacyEventAdapter {

    public Integer findLegacyStudyEventId(Integer moduleEventId) {
        throw new UnsupportedOperationException(
            "Legacy bridge not yet implemented for study event: " + moduleEventId);
    }

    public void syncEventStatus(Integer legacyEventId, Integer statusId) {
        throw new UnsupportedOperationException(
            "Legacy bridge not yet implemented for status sync: " + legacyEventId);
    }

    public String findLegacySubjectLabel(Integer studySubjectId) {
        throw new UnsupportedOperationException(
            "Legacy bridge not yet implemented for subject label: " + studySubjectId);
    }
}
