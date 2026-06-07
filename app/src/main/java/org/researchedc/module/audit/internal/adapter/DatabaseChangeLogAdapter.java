package org.researchedc.module.audit.internal.adapter;

import java.util.List;

import org.researchedc.domain.technicaladmin.DatabaseChangeLogBean;
import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;
import org.researchedc.module.audit.service.DatabaseChangeLogPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
class DatabaseChangeLogAdapter implements DatabaseChangeLogPort {

    private final org.researchedc.dao.spi.DatabaseChangeLogDao databaseChangeLogDao;

    DatabaseChangeLogAdapter(org.researchedc.dao.spi.DatabaseChangeLogDao databaseChangeLogDao) {
        this.databaseChangeLogDao = databaseChangeLogDao;
    }

    @Override
    public List<DatabaseChangeLogDTO> findAll() {
        return databaseChangeLogDao.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    private DatabaseChangeLogDTO toDto(DatabaseChangeLogBean bean) {
        return new DatabaseChangeLogDTO(
                bean.getId(),
                bean.getAuthor(),
                bean.getFileName(),
                bean.getDataExecuted() != null ? bean.getDataExecuted().toInstant().toString() : null,
                bean.getMd5Sum(),
                bean.getDescription(),
                bean.getComments(),
                bean.getTag(),
                bean.getLiquibase());
    }
}
