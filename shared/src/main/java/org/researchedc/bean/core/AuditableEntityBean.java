/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.core;

import java.util.Date;

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

    protected Object owner;

    protected int updaterId;

    protected Object updater;

    protected Status status;

    protected Status oldStatus;

    public AuditableEntityBean() {
        createdDate = new Date(0);
        updatedDate = new Date(0);
        ownerId = 0;
        owner = null;
        updaterId = 0;
        updater = null;
        status = null;
    }

    /**
     * @return Returns the owner.
     */
    public Object getOwner() {
        return owner;
    }

    /**
     * @param owner
     *            The owner to set.
     */
    public void setOwner(Object owner) {
        this.owner = owner;
        ownerId = readId(owner);
    }

    /**
     * @return Returns the ownerId.
     */
    public int getOwnerId() {
        if (owner == null) {
            return ownerId;
        }
        return readId(owner);
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
     * @param updater
     *            The updater to set.
     */
    public void setUpdater(Object updater) {
        this.updater = updater;
        updaterId = readId(updater);
    }

    /**
     * @return Returns the updaterId.
     */
    public int getUpdaterId() {
        if (updater == null) {
            return updaterId;
        }
        return readId(updater);
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

    private int readId(Object candidate) {
        if (candidate == null) {
            return 0;
        }
        if (candidate instanceof EntityBean entity) {
            return entity.getId();
        }
        try {
            Object value = candidate.getClass().getMethod("getId").invoke(candidate);
            if (value instanceof Number number) {
                return number.intValue();
            }
        } catch (ReflectiveOperationException | SecurityException e) {
            return 0;
        }
        return 0;
    }
}
