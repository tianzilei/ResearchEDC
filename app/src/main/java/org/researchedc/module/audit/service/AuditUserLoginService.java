package org.researchedc.module.audit.service;

import org.researchedc.module.audit.dto.AuditUserLoginDTO;
import org.researchedc.module.audit.dto.AuditUserLoginQuery;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditUserLoginService {

    private final AuditUserLoginPort auditUserLoginPort;

    public AuditUserLoginService(AuditUserLoginPort auditUserLoginPort) {
        this.auditUserLoginPort = auditUserLoginPort;
    }

    public Page<AuditUserLoginDTO> listUserLogins(AuditUserLoginQuery query) {
        return auditUserLoginPort.findUserLogins(query);
    }
}
