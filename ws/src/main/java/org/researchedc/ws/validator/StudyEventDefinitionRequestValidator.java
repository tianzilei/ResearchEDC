package org.researchedc.ws.validator;

import org.researchedc.bean.core.Status;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.managestudy.StudySubjectDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.ws.bean.BaseStudyDefinitionBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.sql.DataSource;

public class StudyEventDefinitionRequestValidator implements Validator {

    DataSource dataSource;
    @Autowired
    private StudyDAO studyDAO;
    @Autowired
    private StudySubjectDAO studySubjectDAO;
    @Autowired
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    @Autowired
    private UserAccountDAO userAccountDAO;
    BaseVSValidatorImplementation helper;

    public StudyEventDefinitionRequestValidator(DataSource dataSource) {
        this.dataSource = dataSource;
        helper = new BaseVSValidatorImplementation();
    }

    @SuppressWarnings("rawtypes")
    public boolean supports(Class clazz) {
        return BaseStudyDefinitionBean.class.equals(clazz);
    }

    public void validate(Object obj, Errors e) {
    	BaseStudyDefinitionBean studyEventDefinitionRequestBean = (BaseStudyDefinitionBean) obj;

        if (studyEventDefinitionRequestBean.getStudyUniqueId() == null && studyEventDefinitionRequestBean.getSiteUniqueId() == null) {
            e.reject("studyEventDefinitionRequestValidator.invalid_study_identifier");
            return;
        }
        StudyBean study;int study_id = -1;
        if (studyEventDefinitionRequestBean.getStudyUniqueId() != null ) {
	        study = helper.verifyStudy(studyDAO, studyEventDefinitionRequestBean.getStudyUniqueId(),
	        		null, e);
	        if ( study == null){ return; }
	        study_id = study.getId();
        }
        StudyBean site;int site_id = -1;
        if ( studyEventDefinitionRequestBean.getSiteUniqueId() != null) {
        	site = helper.verifySite(studyDAO, studyEventDefinitionRequestBean.getStudyUniqueId(),
        			studyEventDefinitionRequestBean.getSiteUniqueId(), null, e);
        	if (site == null){return;}
        	site_id = site.getId();
        }
        boolean isRoleVerified = helper.verifyRole(studyEventDefinitionRequestBean.getUser(), study_id, site_id,  e);
        
        
//        //verify study ID
//        if (studyEventDefinitionRequestBean.getStudyUniqueId() != null ) {
//            StudyBean study = getStudyDAO().findByUniqueIdentifier(studyEventDefinitionRequestBean.getStudyUniqueId());
//            if (study == null) {
//                e.reject("studyEventDefinitionRequestValidator.invalid_study_identifier");
//                return;
//            }
//            studyEventDefinitionRequestBean.setStudy(study);
//        }
//        if ( studyEventDefinitionRequestBean.getSiteUniqueId() != null) {
//        	StudyBean site = getStudyDAO().findSiteByUniqueIdentifier(studyEventDefinitionRequestBean.getStudyUniqueId(), studyEventDefinitionRequestBean.getSiteUniqueId());
// 	        
//		    if ( site == null ) {
//                e.reject("studyEventDefinitionRequestValidator.invalid_study_identifier_site_identifier");
//                return;
//            }
//		    studyEventDefinitionRequestBean.setStudy(site);
//        }
//        StudyUserRoleBean siteSur = getUserAccountDAO().findRoleByUserNameAndStudyId(studyEventDefinitionRequestBean.getUser().getName(), studyEventDefinitionRequestBean.getStudy().getId());
//        if (siteSur.getStatus() != Status.AVAILABLE) {
//            e.reject("studyEventDefinitionRequestValidator.insufficient_permissions",
//                    "You do not have sufficient privileges to proceed with this operation.");
//            return;
//        }
    
    }

}
