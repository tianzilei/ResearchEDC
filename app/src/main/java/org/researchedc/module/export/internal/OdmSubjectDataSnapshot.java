package org.researchedc.module.export.internal;

import java.util.List;

public record OdmSubjectDataSnapshot(
    String subjectKey,
    String uniqueId,
    List<OdmStudyEventDataSnapshot> studyEvents
) {
}
