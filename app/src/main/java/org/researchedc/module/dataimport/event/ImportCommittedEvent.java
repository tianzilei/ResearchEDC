package org.researchedc.module.dataimport.event;

public record ImportCommittedEvent(
        Long importJobId,
        Integer studyId,
        String importName,
        Integer requestedBy,
        Integer eventCrfs,
        Integer items) {
}
