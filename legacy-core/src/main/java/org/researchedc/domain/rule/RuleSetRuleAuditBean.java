/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.researchedc.domain.rule;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.domain.AbstractMutableDomainObject;
import org.researchedc.domain.Status;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "rule_set_rule_audit")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "rule_set_rule_audit_id_seq") })
public class RuleSetRuleAuditBean extends AbstractMutableDomainObject {

    RuleSetRuleBean ruleSetRuleBean;
    Status status;
    UserAccountBean updater;
    Date dateUpdated;

    // TODO: phase out the use of these Once the above beans become Hibernated
    protected Integer updaterId;

    /**
     * @return the ruleSetBean
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rule_set_rule_id")
    public RuleSetRuleBean getRuleSetRuleBean() {
        return ruleSetRuleBean;
    }

    /**
     * @param ruleSetBean the ruleSetBean to set
     */
    public void setRuleSetRuleBean(RuleSetRuleBean ruleSetRuleBean) {
        this.ruleSetRuleBean = ruleSetRuleBean;
    }

    @Type(value = org.researchedc.domain.enumsupport.CodedEnumType.class, parameters = @org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.researchedc.bean.core.Status"))
    @Column(name = "status_id")
    public Status getStatus() {
        if (status != null) {
            return status;
        } else
            return Status.AVAILABLE;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the updater
     */
    @Transient
    public UserAccountBean getUpdater() {
        return updater;
    }

    /**
     * @param updater the updater to set
     */
    public void setUpdater(UserAccountBean updater) {
        this.updater = updater;
        if (updater != null) {
            this.updaterId = updater.getId();
        }
    }

    /**
     * @return the dateUpdated
     */
    @Column(name = "date_updated")
    public Date getDateUpdated() {
        return new Date();
    }

    @Transient
    public Date getCurrentUpdatedDate() {
        return this.dateUpdated;
    }

    /**
     * @param dateUpdated the dateUpdated to set
     */
    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    /**
     * @return the updaterId
     */
    @Column(name = "updater_id")
    public Integer getUpdaterId() {
        return updaterId;
    }

    /**
     * @param updaterId the updaterId to set
     */
    public void setUpdaterId(Integer updaterId) {
        this.updaterId = updaterId;
    }

}
