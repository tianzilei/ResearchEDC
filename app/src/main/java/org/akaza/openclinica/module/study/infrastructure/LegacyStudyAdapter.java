package org.akaza.openclinica.module.study.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Placeholder adapter for legacy Study DAO access.
 * All methods throw {@link UnsupportedOperationException} until the legacy bridge is implemented.
 */
@Component
public class LegacyStudyAdapter {

    private static final Logger log = LoggerFactory.getLogger(LegacyStudyAdapter.class);

    public Object findLegacyStudy(Integer legacyStudyId) {
        log.warn("LegacyStudyAdapter.findLegacyStudy({}) called — legacy bridge not yet implemented", legacyStudyId);
        throw new UnsupportedOperationException(
                "Legacy study bridge not yet implemented. studyId=" + legacyStudyId);
    }

    public void syncToLegacy(Integer legacyStudyId, String changeType, Integer changedBy) {
        log.warn("LegacyStudyAdapter.syncToLegacy({}, {}, {}) called — legacy bridge not yet implemented",
                legacyStudyId, changeType, changedBy);
        throw new UnsupportedOperationException(
                "Legacy study sync not yet implemented. studyId=" + legacyStudyId);
    }

    public boolean existsInLegacy(Integer legacyStudyId) {
        log.warn("LegacyStudyAdapter.existsInLegacy({}) called — legacy bridge not yet implemented", legacyStudyId);
        throw new UnsupportedOperationException(
                "Legacy study existence check not yet implemented. studyId=" + legacyStudyId);
    }
}
