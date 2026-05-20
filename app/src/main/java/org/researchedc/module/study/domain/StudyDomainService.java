package org.researchedc.module.study.domain;

import org.researchedc.module.study.entity.StudyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StudyDomainService {

    private static final Logger log = LoggerFactory.getLogger(StudyDomainService.class);

    public String determineStudyType(StudyEntity entity) {
        if (entity.isSite()) {
            return "SITE";
        }
        return "STUDY";
    }

    public boolean validateSiteAssignment(Integer siteId, Integer parentStudyId, boolean isSite) {
        if (parentStudyId == null || parentStudyId <= 0) {
            log.debug("Site assignment rejected: parentStudyId is null or non-positive for site {}", siteId);
            return false;
        }

        if (siteId != null && siteId.equals(parentStudyId)) {
            log.debug("Site assignment rejected: study {} cannot be its own parent", siteId);
            return false;
        }

        if (!isSite && parentStudyId > 0) {
            log.debug("Root study {} will become a site under parent {}", siteId, parentStudyId);
            return true;
        }

        return true;
    }

    public String generateOcOid(Integer studyId) {
        if (studyId == null) {
            throw new IllegalArgumentException("Study ID is required to generate an OID");
        }
        return String.format("S%06d", studyId);
    }
}
