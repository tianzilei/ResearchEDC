package org.researchedc.module.export.internal;

import java.util.List;

public record OdmStudyEventDataSnapshot(
    String studyEventOid,
    String eventRepeatKey,
    List<OdmFormDataSnapshot> forms
) {
}
