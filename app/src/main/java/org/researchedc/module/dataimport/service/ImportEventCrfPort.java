package org.researchedc.module.dataimport.service;

import java.util.List;

import org.researchedc.module.dataimport.dto.ImportEventCrf;

public interface ImportEventCrfPort {

    List<ImportEventCrf> findImportEventCrfsByEventSubjectVersion(
            int studyEventId, int studySubjectId, int crfVersionId);

    List<ImportEventCrf> findImportEventCrfsByEventSubjectCrfId(
            int studyEventId, int studySubjectId, int crfVersionId);

    ImportEventCrf createImportEventCrf(
            int studyEventId,
            int studySubjectId,
            int crfVersionId,
            int ownerId,
            String interviewerName,
            int statusId);
}
