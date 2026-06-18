package org.researchedc.module.audit.internal.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

import org.researchedc.domain.technicaladmin.DatabaseChangeLogBean;
import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;
import org.researchedc.module.audit.service.DatabaseChangeLogPort;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Module-owned database changelog query adapter backed by Liquibase's changelog table.
 */
@Component
@Transactional(readOnly = true)
public class DatabaseChangeLogDaoAdapter implements DatabaseChangeLogPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<DatabaseChangeLogDTO> findChangeLogs() {
        TypedQuery<DatabaseChangeLogBean> query = entityManager.createQuery(
                "FROM DatabaseChangeLogBean dcl ORDER BY dcl.id DESC",
                DatabaseChangeLogBean.class);
        return query.getResultList().stream()
                .map(this::toDto)
                .toList();
    }

    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
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
