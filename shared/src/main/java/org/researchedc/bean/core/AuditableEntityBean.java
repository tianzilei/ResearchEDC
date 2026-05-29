/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.core;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.core.SessionManager;
import org.researchedc.dao.LegacyDaoFactory;
import org.researchedc.dao.spi.IUserAccountDAO;

import java.util.Date;
import java.util.function.Function;

import javax.sql.DataSource;

/**
 * <P>
 * Auditable Entity Bean, soon to take the place of Entity Bean, by Tom
 * Hickerson.
 *
 * <P>
 * We plan to make the following division:
 * <P>
 * Entity Bean--holds a name and id, the base class for a controlled vocab;
 * <P>
 * Auditable Entity Bean, holding more information such as date updated, date
 * created, who updated, and who created the object in the database.
 *
 * @author thickerson
 *
 *
 */
public class AuditableEntityBean extends EntityBean {

    protected Date createdDate;

    protected Date updatedDate;

    protected int ownerId;

    protected UserAccountBean owner;

    protected int updaterId;

    protected UserAccountBean updater;

    protected Status status;

    protected Status oldStatus;

    // used to retrieve the owner and updater when needed
    protected IUserAccountDAO udao;
    private Function<DataSource, IUserAccountDAO> userAccountDaoFactory = LegacyDaoFactory::userAccountDao;

    public AuditableEntityBean() {
        createdDate = new Date(0);
        updatedDate = new Date(0);
        ownerId = 0;
        owner = null;
        updaterId = 0;
        updater = null;
        status = null;
        udao = null;
    }

    /**
     * @return Returns the owner.
     */
    public UserAccountBean getOwner() {
        if (owner != null) {
            return owner;
        }

        try {
            IUserAccountDAO userAccountDao = getUserAccountDao();
            if (owner == null || owner.getId() != ownerId) {
                owner = (UserAccountBean) userAccountDao.findByPK(ownerId);
            }
        } catch (Exception e) {
            owner = null;
        }

        return owner;
    }

    /**
     * @param owner
     *            The owner to set.
     */
    public void setOwner(UserAccountBean owner) {
        this.owner = owner;
        ownerId = owner.getId();
    }

    /**
     * @return Returns the ownerId.
     */
    public int getOwnerId() {
        if (owner == null) {
            return ownerId;
        }
        return owner.getId();
    }

    /**
     * @deprecated
     * @param ownerId
     *            The ownerId to set.
     */
    @Deprecated
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;

        /*
         * if ((owner != null) || (owner.getId() != ownerId)) { owner = null;
         * getOwner(); }
         */}

    /**
     * @return Returns the updater.
     */
    public UserAccountBean getUpdater() {
        if (updater != null) {
            return updater;
        }

        try {
            IUserAccountDAO userAccountDao = getUserAccountDao();
            if (updater == null || updater.getId() != updaterId) {
                updater = (UserAccountBean) userAccountDao.findByPK(updaterId);
            }

        } catch (Exception e) {
            updater = null;
        }

        // try {
        // if (udao == null) {
        // SessionManager sm = new SessionManager(null, "tomh");
        // }
        // updater = (UserAccountBean) udao.findByPK(updaterId);
        // }
        // catch (Exception e) {
        // updater = null;
        // }

        return updater;
    }

    private IUserAccountDAO getUserAccountDao() {
        if (udao == null) {
            udao = userAccountDaoFactory.apply(SessionManager.getStaticDataSource());
        }
        return udao;
    }

    /**
     * @param updater
     *            The updater to set.
     */
    public void setUpdater(UserAccountBean updater) {
        this.updater = updater;
        updaterId = updater.getId();
    }

    /**
     * @return Returns the updaterId.
     */
    public int getUpdaterId() {
        if (updater == null) {
            return updaterId;
        }
        return updater.getId();
    }

    /**
     * @deprecated
     * @param updaterId
     *            The updaterId to set.
     */
    @Deprecated
    public void setUpdaterId(int updaterId) {
        this.updaterId = updaterId;

        /*
         * if ((updater != null) || (updater.getId() != updaterId)) { updater =
         * null; getUpdater(); }
         */}

    /**
     * @return Returns the createdDate.
     */
    public java.util.Date getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate
     *            The createdDate to set.
     */
    public void setCreatedDate(java.util.Date createdDate) {
        this.createdDate = createdDate;
    }

    public java.util.Date getUpdatedDate() {
        return updatedDate;
    }

    /**
     * @param updatedDate
     *            The updatedDate to set.
     */
    public void setUpdatedDate(java.util.Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * @param status
     *            The status to set.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(Status oldStatus) {
        this.oldStatus = oldStatus;
    }
}
