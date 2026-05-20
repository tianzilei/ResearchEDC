package org.researchedc.dao.hibernate;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import jakarta.persistence.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import org.researchedc.domain.DomainObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract Domain DAO - Updated for JPA/Hibernate 6.x
 * Uses EntityManager instead of deprecated HibernateTemplate
 */
public abstract class AbstractDomainDao<T extends DomainObject> {

    @PersistenceContext
    private EntityManager entityManager;

    abstract Class<T> domainClass();

    public String getDomainClassName() {
        return domainClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public T findById(Integer id) {
        String queryStr = "from " + getDomainClassName() + " do where do.id = :id";
        Query q = getEntityManager().createQuery(queryStr);
        q.setParameter("id", id);
        return (T) q.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<T> findAll() {
        String queryStr = "from " + getDomainClassName() + " do";
        Query q = getEntityManager().createQuery(queryStr);
        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public T findByOcOID(String OCOID) {
        String queryStr = "from " + getDomainClassName() + " do where do.ocOid = :ocOid";
        Query q = getEntityManager().createQuery(queryStr);
        q.setParameter("ocOid", OCOID);
        return (T) q.getSingleResult();
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
        return domainObject.getId();
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

    public SessionFactory getSessionFactory() {
        return getCurrentSession().getSessionFactory();
    }
}
