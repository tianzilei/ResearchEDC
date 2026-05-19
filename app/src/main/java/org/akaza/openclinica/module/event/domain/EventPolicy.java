package org.akaza.openclinica.module.event.domain;

public final class EventPolicy {

    public static final int COMPLETED_STATUS_ID = 7;

    private EventPolicy() {
    }

    /**
     * @throws IllegalArgumentException if either required field is null
     */
    public static void validateScheduling(Integer studySubjectId,
                                          Integer studyEventDefinitionId) {
        if (studySubjectId == null) {
            throw new IllegalArgumentException("studySubjectId is required");
        }
        if (studyEventDefinitionId == null) {
            throw new IllegalArgumentException("studyEventDefinitionId is required");
        }
    }

    /**
     * @throws IllegalStateException if the event is already completed
     */
    public static void validateCompletion(Integer currentStatusId) {
        if (currentStatusId != null && currentStatusId == COMPLETED_STATUS_ID) {
            throw new IllegalStateException(
                "Cannot complete an event that is already completed");
        }
    }
}
