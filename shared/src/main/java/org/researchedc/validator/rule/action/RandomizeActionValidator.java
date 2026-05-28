package org.researchedc.validator.rule.action;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.NullValue;
import org.researchedc.bean.core.ResponseType;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.ResponseOptionBean;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.domain.rule.AuditableBeanWrapper;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.action.PropertyBean;
import org.researchedc.domain.rule.action.RandomizeActionBean;
import org.researchedc.domain.rule.action.StratificationFactorBean;
import org.researchedc.domain.rule.expression.Context;
import org.researchedc.domain.rule.expression.ExpressionBean;
import org.researchedc.domain.rule.expression.ExpressionObjectWrapper;
import org.researchedc.domain.rule.expression.ExpressionProcessor;
import org.researchedc.domain.rule.expression.ExpressionProcessorFactory;
import org.researchedc.exception.OpenClinicaSystemException;
import org.researchedc.logic.expressionTree.ExpressionTreeHelper;
import org.researchedc.service.rule.expression.ExpressionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.sql.DataSource;

public class RandomizeActionValidator implements Validator {

    ItemDAO itemDAO;
    ItemFormMetadataDAO itemFormMetadataDAO;
    EventDefinitionCRFDAO eventDefinitionCRFDAO;
    StudyEventDefinitionDAO studyEventDefinitionDAO;
    CRFDAO crfDAO;
    DataSource dataSource;
    EventDefinitionCRFBean eventDefinitionCRFBean;
    ExpressionService expressionService;
    RuleSetBean ruleSetBean;
    ResourceBundle respage;


    public RandomizeActionValidator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This Validator validates just Person instances
     */
    public boolean supports(Class clazz) {
        return RandomizeActionBean.class.equals(clazz);
    }

    public void validateOidInPropertyBean(PropertyBean propertyBean, Errors e, String p) {
        if (getExpressionService().isExpressionPartial(getRuleSetBean().getTarget().getValue())) {
            if (getExpressionService().getExpressionSize(propertyBean.getOid()).intValue() > 3) {
                e.rejectValue(p + "oid", "oid.invalid", "OID: " + propertyBean.getOid() + " is Invalid.");
            }
            try {
                getExpressionService().isExpressionValid(propertyBean.getOid());
            } catch (OpenClinicaSystemException ose) {
                e.rejectValue(p + "oid", "oid.invalid", "OID: " + propertyBean.getOid() + " is Invalid.");
            }
            // Use OID in destinationProperty to get CRF
            CRFBean destinationPropertyOidCrf = getExpressionService().getCRFFromExpression(propertyBean.getOid());
            if (destinationPropertyOidCrf == null) {
                ItemBean item = getExpressionService().getItemBeanFromExpression(propertyBean.getOid());
                destinationPropertyOidCrf = getCrfDAO().findByItemOid(item.getOid());
            }
            // Use Target get CRF
            CRFBean targetCrf = getExpressionService().getCRFFromExpression(getRuleSetBean().getTarget().getValue());
            if (targetCrf == null) {
                ItemBean item = getExpressionService().getItemBeanFromExpression(getRuleSetBean().getTarget().getValue());
                targetCrf = getCrfDAO().findByItemOid(item.getOid());

            }
            // Get All event definitions the selected CRF belongs to
            List<StudyEventDefinitionBean> destinationPropertyStudyEventDefinitions = getStudyEventDefinitionDAO().findAllByCrf(destinationPropertyOidCrf);
            List<StudyEventDefinitionBean> targetStudyEventDefinitions = getStudyEventDefinitionDAO().findAllByCrf(targetCrf);
            Collection intersection = CollectionUtils.intersection(destinationPropertyStudyEventDefinitions, targetStudyEventDefinitions);
            if (intersection.size() == 0) {
                e.rejectValue(p + "oid", "oid.invalid", "OID: " + propertyBean.getOid() + " is Invalid.");
            }
        } else {
            String expression = getExpressionService().constructFullExpressionIfPartialProvided(propertyBean.getOid(), getRuleSetBean().getTarget().getValue());
            ItemBean item = getExpressionService().getItemBeanFromExpression(expression);

            if (!getExpressionService().isRandomizeActionExpressionValid(propertyBean.getOid(), getRuleSetBean(), 3) || item == null) {
                e.rejectValue(p + "oid", "oid.invalid", "OID: " + propertyBean.getOid() + " is Invalid.");
            }
        }
    }

    
    public void validate(Object obj, Errors e) {
        RandomizeActionBean randomizeActionBean = (RandomizeActionBean) obj;
        String p="";
        for (int i = 0; i < randomizeActionBean.getProperties().size(); i++) {
             p = "properties[" + i + "].";
            PropertyBean propertyBean = randomizeActionBean.getProperties().get(i);
            ValidationUtils.rejectIfEmpty(e, p + "oid", "oid.empty");
            validateOidInPropertyBean(propertyBean, e, p);
        }

    }



    public ItemDAO getItemDAO() {
        if (itemDAO == null) {
            itemDAO = new ItemDAO(dataSource);
        }
        return itemDAO;
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDAO() {
        if (studyEventDefinitionDAO == null) {
            studyEventDefinitionDAO = new StudyEventDefinitionDAO(dataSource);
        }
        return studyEventDefinitionDAO;
    }

    public CRFDAO getCrfDAO() {
        if (crfDAO == null) {
            crfDAO = new CRFDAO(dataSource);
        }
        return crfDAO;
    }

    public EventDefinitionCRFDAO getEventDefinitionCRFDAO() {
        if (eventDefinitionCRFDAO == null) {
            eventDefinitionCRFDAO = new EventDefinitionCRFDAO(dataSource);
        }
        return eventDefinitionCRFDAO;
    }

    public ItemFormMetadataDAO getItemFormMetadataDAO() {
        if (itemFormMetadataDAO == null) {
            itemFormMetadataDAO = new ItemFormMetadataDAO(dataSource);
        }
        return itemFormMetadataDAO;
    }

    public EventDefinitionCRFBean getEventDefinitionCRFBean() {
        return eventDefinitionCRFBean;
    }

    public void setEventDefinitionCRFBean(EventDefinitionCRFBean eventDefinitionCRFBean) {
        this.eventDefinitionCRFBean = eventDefinitionCRFBean;
    }

    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public RuleSetBean getRuleSetBean() {
        return ruleSetBean;
    }

    public void setRuleSetBean(RuleSetBean ruleSetBean) {
        this.ruleSetBean = ruleSetBean;
    }
}
