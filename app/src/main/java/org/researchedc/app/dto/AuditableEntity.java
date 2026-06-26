package org.researchedc.app.dto;

import java.util.Date;

public class AuditableEntity extends Entity {
    protected Date createdDate;
    protected Date updatedDate;
    protected int ownerId;
    protected int updaterId;
    protected Status status;
    protected Status oldStatus;

    public AuditableEntity() {
        createdDate = new Date(0);
        updatedDate = new Date(0);
        ownerId = 0;
        updaterId = 0;
        status = null;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setUpdaterId(int updaterId) {
        this.updaterId = updaterId;
    }

    public int getUpdaterId() {
        return updaterId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Status getStatus() {
        return status;
    }

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
