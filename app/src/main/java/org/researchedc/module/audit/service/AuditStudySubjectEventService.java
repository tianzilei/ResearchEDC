package org.researchedc.module.audit.service;

import org.researchedc.module.audit.dto.AuditStudySubjectEventsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditStudySubjectEventService {

    private final AuditStudySubjectEventPort auditStudySubjectEventPort;

    public AuditStudySubjectEventService(AuditStudySubjectEventPort auditStudySubjectEventPort) {
        this.auditStudySubjectEventPort = auditStudySubjectEventPort;
    }

    public AuditStudySubjectEventsDTO listStudySubjectEvents(int studyId) {
        return auditStudySubjectEventPort.findStudySubjectEvents(studyId);
    }
}
