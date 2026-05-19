package org.akaza.openclinica.module.subject.domain;

import java.time.LocalDateTime;

public final class SubjectDomainService {

    private SubjectDomainService() {
    }

    public static String generateLabel(Integer studyId, Integer subjectId) {
        return "SS" + studyId + "-" + subjectId;
    }

    public static void validateSubjectEnrollmentConditions(Boolean subjectExists, Integer studyId) {
        if (!Boolean.TRUE.equals(subjectExists)) {
            throw new IllegalArgumentException("Subject does not exist and cannot be enrolled");
        }
        if (studyId == null) {
            throw new IllegalArgumentException("Study ID is required for enrollment");
        }
    }
}
