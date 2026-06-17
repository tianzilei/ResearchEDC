package org.researchedc.module.audit.service;

import java.util.List;

import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;

public interface DatabaseChangeLogPort {

    List<DatabaseChangeLogDTO> findChangeLogs();
}
