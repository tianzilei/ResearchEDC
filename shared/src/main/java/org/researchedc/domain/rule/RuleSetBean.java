/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research
 */
package org.researchedc.domain.rule;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.domain.AbstractAuditableMutableDomainObject;
import org.researchedc.domain.rule.expression.ExpressionBean;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * <p> RuleSetBean, Holds a collection of Rules & Actions </p>
 * @author Krikor Krumlian
 */
@Entity
@Table(name = "rule_set")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "rule_set_id_seq") })
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class RuleSetBean extends AbstractAuditableMutableDomainObject implements Serializable{

    private StudyEventDefinitionBean studyEventDefinition;
    private StudyBean study;
    private CRFBean crf;
    private CRFVersionBean crfVersion;
    private ItemBean item;
	private boolean runSchedule=false;
    private String runTime;

    
	private RunOnSchedule runOnSchedule;
    private List<RuleSetRuleBean> ruleSetRules;
    private ExpressionBean target;
    private ExpressionBean originalTarget;

    // transient properties
    private List<ExpressionBean> expressions; // itemGroup & item populated when RuleSets are retrieved
    private ItemGroupBean itemGroup;

    // TODO : Pending conversion of the objects below to use Hibernate
    private Integer studyEventDefinitionId;
    private Integer studyId;
    private Integer crfId;
    private Integer crfVersionId;
    private Integer itemId;
    private Integer itemGroupId;

    // Getters & Setters
    @Transient
    public StudyEventDefinitionBean getStudyEventDefinition() {
        return studyEventDefinition;
    }

    public void setStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition) {
        if (studyEventDefinition != null && studyEventDefinition.getId() > 0) {
            this.studyEventDefinitionId = studyEventDefinition.getId();
        }
        this.studyEventDefinition = studyEventDefinition;
    }

    // @OneToMany(mappedBy = "ruleSetBean")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "rule_set_id", nullable = false)
    public List<RuleSetRuleBean> getRuleSetRules() {
        return ruleSetRules;
    }

    public void setRuleSetRules(List<RuleSetRuleBean> ruleSetRuleAssignment) {
        this.ruleSetRules = ruleSetRuleAssignment;
    }

    @Transient
    public StudyBean getStudy() {
        return study;
    }

    public void setStudy(StudyBean study) {
        if (study.getId() > 0) {
            this.studyId = study.getId();
        }
        this.study = study;
    }

    @Transient
    public ItemGroupBean getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(ItemGroupBean itemGroup) {
        if (itemGroup != null && itemGroup.getId() > 0) {
            this.itemGroupId = itemGroup.getId();
        }
        this.itemGroup = itemGroup;
    }

    @Transient
    public ItemBean getItem() {
        return item;
    }

    public void setItem(ItemBean item) {
        if (item != null && item.getId() > 0) {
            this.itemId = item.getId();
        }
        this.item = item;
    }

    @Transient
    public CRFBean getCrf() {
        return crf;
    }

    public void setCrf(CRFBean crf) {
        if (crf != null && crf.getId() > 0) {
            this.crfId = crf.getId();
        }
        this.crf = crf;
    }

    @Transient
    public CRFVersionBean getCrfVersion() {
        return crfVersion;
    }

    public void setCrfVersion(CRFVersionBean crfVersion) {
        if (crfVersion != null && crfVersion.getId() > 0) {
            this.crfVersionId = crfVersion.getId();
        }
        this.crfVersion = crfVersion;
    }

    @Transient
    public List<ExpressionBean> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<ExpressionBean> expressions) {
        this.expressions = expressions;
    }

    @Transient
    public ExpressionBean getTarget() {
        if (this.target == null) {
            target = originalTarget;
        }
        return target;
    }

    public void setTarget(ExpressionBean target) {
        this.target = target;
    }

    /**
     * @return originalTarget
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rule_expression_id")
    public ExpressionBean getOriginalTarget() {
        return originalTarget;
    }

    /**
     * @param originalTarget
     */
    public void setOriginalTarget(ExpressionBean originalTarget) {
        this.originalTarget = originalTarget;
    }

    /**
     * @return the studyEventDefinitionId
     */
    public Integer getStudyEventDefinitionId() {
        return studyEventDefinitionId;
    }

    /**
     * @param studyEventDefinitionId the studyEventDefinitionId to set
     */
    public void setStudyEventDefinitionId(Integer studyEventDefinitionId) {
        this.studyEventDefinitionId = studyEventDefinitionId;
    }

    /**
     * @return the studyId
     */
    public Integer getStudyId() {
        return studyId;
    }

    /**
     * @param studyId the studyId to set
     */
    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

    /**
     * @return the crfId
     */
    public Integer getCrfId() {
        return crfId;
    }

    /**
     * @param crfId the crfId to set
     */
    public void setCrfId(Integer crfId) {
        this.crfId = crfId;
    }

    /**
     * @return the crfVersionId
     */
    public Integer getCrfVersionId() {
        return crfVersionId;
    }

    /**
     * @param crfVersionId the crfVersionId to set
     */
    public void setCrfVersionId(Integer crfVersionId) {
        this.crfVersionId = crfVersionId;
    }

    /**
     * @return the itemId
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * @param itemId
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * @return the itemGroupId
     */
    public Integer getItemGroupId() {
        return itemGroupId;
    }

    /**
     * @param itemGroupId
     */
    public void setItemGroupId(Integer itemGroupId) {
        this.itemGroupId = itemGroupId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (target == null ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RuleSetBean other = (RuleSetBean) obj;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        if (runTime == null) {
            if (other.runTime != null)
                return false;
        } else if (!runTime.equals(other.runTime))
            return false;
        if (expressions == null && other.expressions != null) return false;
        if (expressions != null && other.expressions == null) return false;
        if (expressions.size() != other.expressions.size()) return false;
        return true;
    }

    @JoinColumn(name = "run_time")
	public String getRunTime() {
		return runTime;
	}

	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}


    
    @Transient
	public RunOnSchedule getRunOnSchedule() {
		return runOnSchedule;
	}



	public void setRunOnSchedule(RunOnSchedule runOnSchedule) {
		this.runOnSchedule = runOnSchedule;
	}

	
    @JoinColumn(name = "run_schedule")
	public boolean isRunSchedule() {
		return runSchedule;
	}

	public void setRunSchedule(boolean runSchedule) {
		this.runSchedule = runSchedule;
	}






}
