package org.researchedc.module.audit.internal.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.dao.spi.DatabaseChangeLogDao;
import org.researchedc.domain.technicaladmin.DatabaseChangeLogBean;
import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;
import org.researchedc.module.audit.service.DatabaseChangeLogPort;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Primary adapter implementing {@link DatabaseChangeLogDao} (legacy SPI)
 * using JPA with injected {@link EntityManager}.
 *
 * <p>Replaces the hibernate {@code DatabaseChangeLogDao} implementation so the
 * HibernateConfig {@code @Bean} and the hibernate impl file can be deleted.</p>
 */
@Component("databaseChangeLogDao")
@Primary
@Transactional(readOnly = true)
public class DatabaseChangeLogDaoAdapter implements DatabaseChangeLogDao, DatabaseChangeLogPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ArrayList<DatabaseChangeLogBean> findAll() {
        TypedQuery<DatabaseChangeLogBean> query = entityManager.createQuery(
                "FROM DatabaseChangeLogBean dcl ORDER BY dcl.id DESC",
                DatabaseChangeLogBean.class);
        return new ArrayList<>(query.getResultList());
    }

    @Override
    public List<DatabaseChangeLogDTO> findChangeLogs() {
        return findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public DatabaseChangeLogBean findById(String id, String author, String fileName) {
        TypedQuery<DatabaseChangeLogBean> query = entityManager.createQuery(
                "FROM DatabaseChangeLogBean dcl WHERE dcl.id = :id AND dcl.author = :author AND dcl.fileName = :fileName",
                DatabaseChangeLogBean.class);
        query.setParameter("id", id);
        query.setParameter("author", author);
        query.setParameter("fileName", fileName);
        var results = query.getResultList();
        return results.isEmpty() ? null : results.getFirst();
    }

    @Override
    public Long count() {
        return entityManager.createQuery(
                "SELECT COUNT(dcl) FROM DatabaseChangeLogBean dcl",
                Long.class).getSingleResult();
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
