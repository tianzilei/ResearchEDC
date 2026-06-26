package org.researchedc.module.audit.service;

import org.researchedc.module.audit.dto.AuditUserEventsDTO;

public interface AuditUserEventPort {

    AuditUserEventsDTO findUserEvents(int userId);
}
