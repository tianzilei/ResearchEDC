package org.researchedc.module.audit.service;

import org.researchedc.module.audit.dto.AuditStudySubjectEventsDTO;

public interface AuditStudySubjectEventPort {

    AuditStudySubjectEventsDTO findStudySubjectEvents(int studyId);
}
