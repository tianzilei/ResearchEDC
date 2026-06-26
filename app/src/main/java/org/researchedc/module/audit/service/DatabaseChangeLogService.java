package org.researchedc.module.audit.service;

import java.util.List;

import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatabaseChangeLogService {

    private final DatabaseChangeLogPort databaseChangeLogPort;

    public DatabaseChangeLogService(DatabaseChangeLogPort databaseChangeLogPort) {
        this.databaseChangeLogPort = databaseChangeLogPort;
    }

    public List<DatabaseChangeLogDTO> listChangeLogs() {
        return databaseChangeLogPort.findChangeLogs();
    }
}
