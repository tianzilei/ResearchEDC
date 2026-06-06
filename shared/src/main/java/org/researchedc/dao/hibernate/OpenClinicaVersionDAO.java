/**
 * 
 */
package org.researchedc.dao.hibernate;

import java.sql.Timestamp;

import org.researchedc.domain.OpenClinicaVersionBean;
import org.researchedc.dao.spi.OpenClinicaVersionDao;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author pgawade
 *
 */
public class OpenClinicaVersionDAO extends AbstractDomainDao<OpenClinicaVersionBean> implements OpenClinicaVersionDao {

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
    @Override
    public Class<OpenClinicaVersionBean> domainClass() {
        return OpenClinicaVersionBean.class;
    }

    @Transactional
    public OpenClinicaVersionBean findDefault() {
        String query = "from " + getDomainClassName() + " ocVersion";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        return (OpenClinicaVersionBean) q.uniqueResult();
    }

    @Transactional
    public void saveOCVersionToDB(String openClinicaVersion) {
        logger.debug("OpenClinicaVersionDAO -> saveOCVersionToDB");
        logger.debug("openClinicaVersion: " + openClinicaVersion);
        // Delete the previous entry if exists in the database
        deleteDefault();
        // Insert new entry
        Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
        OpenClinicaVersionBean openClinicaVersionBean = new OpenClinicaVersionBean();
        openClinicaVersionBean.setName(openClinicaVersion);
        openClinicaVersionBean.setUpdate_timestamp(currentTimestamp);
        saveOrUpdate(openClinicaVersionBean);

    }

    @Transactional
    public int deleteDefault() {
        String query = "delete from " + getDomainClassName() + " ocVersion";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        return q.executeUpdate();
    }

}
