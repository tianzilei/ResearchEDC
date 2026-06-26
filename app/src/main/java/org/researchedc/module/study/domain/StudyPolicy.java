package org.researchedc.module.study.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.researchedc.module.study.application.command.CreateStudyCommand;
import org.researchedc.module.study.application.command.UpdateStudyCommand;
import org.springframework.stereotype.Component;

@Component
public class StudyPolicy {

    // Regex for valid unique identifiers: alphanumeric, hyphens, underscores, dots.
    private static final Pattern UNIQUE_IDENTIFIER_PATTERN =
            Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._\\-]*$");

    private static final int MAX_UNIQUE_IDENTIFIER_LENGTH = 30;
    private static final int MAX_NAME_LENGTH = 60;

    public List<String> validateCreate(CreateStudyCommand command) {
        List<String> errors = new ArrayList<>();

        if (command.getName() == null || command.getName().isBlank()) {
            errors.add("Study name is required");
        } else if (command.getName().trim().length() > MAX_NAME_LENGTH) {
            errors.add("Study name must not exceed " + MAX_NAME_LENGTH + " characters");
        }

        if (command.getUniqueIdentifier() != null && !command.getUniqueIdentifier().isBlank()) {
            validateUniqueIdentifierFormat(command.getUniqueIdentifier(), errors);
        }

        if (command.getExpectedTotalEnrollment() != null && command.getExpectedTotalEnrollment() < 0) {
            errors.add("Expected total enrollment must not be negative");
        }

        return errors;
    }

    public List<String> validateUpdate(UpdateStudyCommand command) {
        List<String> errors = new ArrayList<>();

        if (command.getStudyId() == null) {
            errors.add("Study ID is required for update");
        }

        if (command.getName() != null) {
            if (command.getName().isBlank()) {
                errors.add("Study name must not be blank");
            } else if (command.getName().trim().length() > MAX_NAME_LENGTH) {
                errors.add("Study name must not exceed " + MAX_NAME_LENGTH + " characters");
            }
        }

        if (command.getUniqueIdentifier() != null && !command.getUniqueIdentifier().isBlank()) {
            validateUniqueIdentifierFormat(command.getUniqueIdentifier(), errors);
        }

        if (command.getExpectedTotalEnrollment() != null && command.getExpectedTotalEnrollment() < 0) {
            errors.add("Expected total enrollment must not be negative");
        }

        return errors;
    }

    public List<String> validateDelete(boolean hasSites) {
        List<String> errors = new ArrayList<>();

        if (hasSites) {
            errors.add("Cannot delete a study that has sites assigned to it. Remove all sites first.");
        }

        return errors;
    }

    public List<String> validateStatusTransition(Integer currentStatusId, Integer newStatusId) {
        List<String> errors = new ArrayList<>();

        if (currentStatusId == null) {
            errors.add("Current study status is not set");
            return errors;
        }

        if (newStatusId == null) {
            errors.add("New status ID is required");
            return errors;
        }

        if (currentStatusId.equals(newStatusId)) {
            return errors;
        }

        if (newStatusId < 1) {
            errors.add("Invalid status ID: " + newStatusId);
        }

        return errors;
    }

    private void validateUniqueIdentifierFormat(String uid, List<String> errors) {
        if (uid.length() > MAX_UNIQUE_IDENTIFIER_LENGTH) {
            errors.add("Unique identifier must not exceed " + MAX_UNIQUE_IDENTIFIER_LENGTH + " characters");
        }
        if (!UNIQUE_IDENTIFIER_PATTERN.matcher(uid).matches()) {
            errors.add("Unique identifier contains invalid characters. "
                    + "Only alphanumeric, hyphens, underscores, and dots are allowed");
        }
    }
}
