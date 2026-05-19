package org.akaza.openclinica.module.subject.domain;

import org.akaza.openclinica.module.subject.application.command.CreateSubjectCommand;
import org.akaza.openclinica.module.subject.application.command.EnrollSubjectCommand;

public final class SubjectPolicy {

    private SubjectPolicy() {
    }

    public static void validateCreateSubject(CreateSubjectCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateSubjectCommand must not be null");
        }
        if (command.getUniqueIdentifier() == null || command.getUniqueIdentifier().isBlank()) {
            throw new IllegalArgumentException("Subject uniqueIdentifier is required");
        }
    }

    public static void validateEnrollSubject(EnrollSubjectCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("EnrollSubjectCommand must not be null");
        }
        if (command.getStudyId() == null) {
            throw new IllegalArgumentException("Study ID is required");
        }
        if (command.getSubjectId() == null) {
            throw new IllegalArgumentException("Subject ID is required");
        }
    }
}
