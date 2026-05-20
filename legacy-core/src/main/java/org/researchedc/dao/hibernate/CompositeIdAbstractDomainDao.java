package org.researchedc.dao.hibernate;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.researchedc.domain.CompositeIdDomainObject;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

public abstract class CompositeIdAbstractDomainDao<T extends CompositeIdDomainObject> {

    @PersistenceContext
    private EntityManager entityManager;

    abstract Class<T> domainClass();

    public String getDomainClassName() {
        return domainClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<T> findAll() {
        String queryStr = "from " + getDomainClassName() + " do";
        Query q = getEntityManager().createQuery(queryStr);
        return q.getResultList();
    }

    @Transactional
    public T saveOrUpdate(T domainObject) {
        if (domainObject.getId() == null) {
            getEntityManager().persist(domainObject);
            return domainObject;
        } else {
            return getEntityManager().merge(domainObject);
        }
    }

    @Transactional
    public Serializable save(T domainObject) {
        getEntityManager().persist(domainObject);
        return (Serializable) domainObject.getId();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public T findByColumnName(Object id, String key) {
        String queryStr = "from " + getDomainClassName() + " do where do." + key + " = :value";
        Query q = getEntityManager().createQuery(queryStr);
        q.setParameter("value", id);
        return (T) q.getSingleResult();
    }

    @Transactional
    public Long count() {
        String queryStr = "select count(*) from " + getDomainClassName();
        return (Long) getEntityManager().createQuery(queryStr).getSingleResult();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }
}
