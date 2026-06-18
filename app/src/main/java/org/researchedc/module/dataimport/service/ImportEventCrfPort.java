package org.researchedc.module.dataimport.service;

import java.util.List;

public interface ImportEventCrfPort {

    List<Object[]> findImportEventCrfsByEventSubjectVersion(
            int studyEventId, int studySubjectId, int crfVersionId);

    List<Object[]> findImportEventCrfsByEventSubjectCrfId(
            int studyEventId, int studySubjectId, int crfVersionId);

    Object[] createImportEventCrf(
            int studyEventId,
            int studySubjectId,
            int crfVersionId,
            int ownerId,
            String interviewerName,
            int statusId);
}
