/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.*;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.form.Validator;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.hibernate.EventDefinitionCrfTagDao;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.domain.SourceDataVerification;
import org.researchedc.domain.datamap.EventDefinitionCrfTag;
import org.researchedc.service.managestudy.EventDefinitionCrfTagService;
import org.researchedc.service.pmanage.Authorization;
import org.researchedc.service.pmanage.ParticipantPortalRegistrar;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;

/**
 * @author jxu
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class UpdateEventDefinitionServlet extends SecureController {
    
    @Autowired
    private ICrfVersionDAO crfVersionDao;
    @Autowired
    private IDiscrepancyNoteDAO discrepancyNoteDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;

EventDefinitionCrfTagService eventDefinitionCrfTagService = null;

    /**
     * 
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_DEFINITION_SERVLET, respage.getString("current_study_locked"));
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }
        addPageMessage(respage.getString("no_have_permission_to_update_study_event_definition") + "<br>" + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_study_director"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        String action = request.getParameter("action");
        if (StringUtil.isBlank(action)) {

            forwardPage(Page.UPDATE_EVENT_DEFINITION1);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                confirmDefinition();

            } else if ("submit".equalsIgnoreCase(action)) {
                submitDefinition();

            } else {
                addPageMessage(respage.getString("updating_ED_is_cancelled"));
                forwardPage(Page.LIST_DEFINITION_SERVLET);
            }
        }

    }

    /**
     * 
     * @throws Exception
     */
    private void confirmDefinition() throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);

        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) session.getAttribute("definition");
        IStudyParameterValueDAO spvdao = this.studyParameterValueDao;    
        String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
        if (participateFormStatus.equals("enabled")) baseUrl();

        request.setAttribute("participateFormStatus",participateFormStatus );

        v.addValidation("name", Validator.NO_BLANKS);
        v.addValidation("name", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
        v.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
        v.addValidation("category", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);

        ArrayList <EventDefinitionCRFBean>  edcsInSession = (ArrayList<EventDefinitionCRFBean>) session.getAttribute("eventDefinitionCRFs");
        int parentStudyId=sed.getStudyId();
        EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;
        ArrayList <EventDefinitionCRFBean> eventDefCrfList =(ArrayList <EventDefinitionCRFBean>) edcdao.findAllActiveSitesAndStudiesPerParentStudy(parentStudyId);
         

         //   logger.info("no errors");

            sed.setName(fp.getString("name"));
            
            //OC-12780
            boolean canBeChanged = true;
            boolean repeating = fp.getBoolean("repeating");
            
            if(sed.isRepeating() && !repeating) {            	
            	 
            	 IStudyEventDAO seDao = this.studyEventDao;
            	 if(seDao.isThisRepeatingEventScheduledMoreThanOneTime(parentStudyId, sed.getId())) {
            		 v.addValidation("repeating", Validator.CAN_NOT_CHANGE_NONE_REPEATING_NOW);
            		 canBeChanged = false;
            	 }
            }
            
            if(canBeChanged) {
            	sed.setRepeating(repeating);
            }
            
            sed.setCategory(fp.getString("category"));
            sed.setDescription(fp.getString("description"));
            sed.setType(fp.getString("type"));
            
            //OC-12780
            

            session.setAttribute("definition", sed);
            ICrfVersionDAO cvdao = this.crfVersionDao;
            ArrayList<EventDefinitionCRFBean> edcs = (ArrayList) session.getAttribute("eventDefinitionCRFs");
            for (int i = 0; i < edcs.size(); i++) {
                EventDefinitionCRFBean edcBean = (EventDefinitionCRFBean) edcs.get(i);
                if (!edcBean.getStatus().equals(Status.DELETED) && !edcBean.getStatus().equals(Status.AUTO_DELETED)) {
                    // only get inputs from web page if AVAILABLE
                    int defaultVersionId = fp.getInt("defaultVersionId" + i);
                    edcBean.setDefaultVersionId(defaultVersionId);
                    CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
                    edcBean.setDefaultVersionName(defaultVersion.getName());

                    String requiredCRF = fp.getString("requiredCRF" + i);
                    String doubleEntry = fp.getString("doubleEntry" + i);
                    String decisionCondition = fp.getString("decisionCondition" + i);
                    String electronicSignature = fp.getString("electronicSignature" + i);
                    String hideCRF = fp.getString("hideCRF" + i);
                    int sdvId = fp.getInt("sdvOption" + i);
                    String participantForm = fp.getString("participantForm"+i);
                    String allowAnonymousSubmission = fp.getString("allowAnonymousSubmission" + i);
                    String submissionUrl = fp.getString("submissionUrl" + i);
                    String offline = fp.getString("offline" + i);

                    System.out.println("submission :"+ submissionUrl);
                    
                    
                    
                    if (!StringUtil.isBlank(hideCRF) && "yes".equalsIgnoreCase(hideCRF.trim())) {
                        edcBean.setHideCrf(true);
                    } else {
                        edcBean.setHideCrf(false);
                    }

                    if (!StringUtil.isBlank(requiredCRF) && "yes".equalsIgnoreCase(requiredCRF.trim())) {
                        edcBean.setRequiredCRF(true);
                    } else {
                        edcBean.setRequiredCRF(false);
                    }
                    if (!StringUtil.isBlank(doubleEntry) && "yes".equalsIgnoreCase(doubleEntry.trim())) {
                        edcBean.setDoubleEntry(true);
                    } else {
                        edcBean.setDoubleEntry(false);
                    }

                    if (!StringUtil.isBlank(electronicSignature) && "yes".equalsIgnoreCase(electronicSignature.trim())) {
                        edcBean.setElectronicSignature(true);
                    } else {
                        edcBean.setElectronicSignature(false);
                    }

                    if (!StringUtil.isBlank(decisionCondition) && "yes".equalsIgnoreCase(decisionCondition.trim())) {
                        edcBean.setDecisionCondition(true);
                    } else {
                        edcBean.setDecisionCondition(false);
                    }
                    if (!StringUtil.isBlank(participantForm) && "yes".equalsIgnoreCase(participantForm.trim())) {
                        edcBean.setParticipantForm(true);
                    } else {
                        edcBean.setParticipantForm(false);
                    }
                    if (!StringUtils.isBlank(allowAnonymousSubmission) && "yes".equalsIgnoreCase(allowAnonymousSubmission.trim())) {
                        edcBean.setAllowAnonymousSubmission(true);
                    } else {
                        edcBean.setAllowAnonymousSubmission(false);
                    }
                    edcBean.setSubmissionUrl(submissionUrl.trim());
                    if (!StringUtils.isBlank(offline) && "yes".equalsIgnoreCase(offline.trim())) {
                        edcBean.setOffline(true);
                    } else {
                        edcBean.setOffline(false);
                    }

                    
                    
                    String nullString = "";
                    // process null values
                    ArrayList nulls = NullValue.toArrayList();
                    for (int a = 0; a < nulls.size(); a++) {
                        NullValue n = (NullValue) nulls.get(a);
                        String myNull = fp.getString(n.getName().toLowerCase() + i);
                        if (!StringUtil.isBlank(myNull) && "yes".equalsIgnoreCase(myNull.trim())) {
                            nullString = nullString + n.getName().toUpperCase() + ",";
                        }

                    }

                    if (sdvId > 0 && (edcBean.getSourceDataVerification() == null || sdvId != edcBean.getSourceDataVerification().getCode())) {
                        edcBean.setSourceDataVerification(SourceDataVerification.getByCode(sdvId));
                    }

                    edcBean.setNullValues(nullString);
                    logger.info("found null values: " + nullString);
                }

            }
            
            validateSubmissionUrl(edcsInSession,eventDefCrfList,v);
            errors = v.validate();

            if (!errors.isEmpty()) {
                logger.info("has errors");
                session.setAttribute("eventDefinitionCRFs", edcs);
                request.setAttribute("formMessages", errors);
                forwardPage(Page.UPDATE_EVENT_DEFINITION1);

            } 

            session.setAttribute("eventDefinitionCRFs", edcs);
            forwardPage(Page.UPDATE_EVENT_DEFINITION_CONFIRM);
        
        

    }

    /**
     * Updates the definition
     * 
     */
    private void submitDefinition() {
        ArrayList edcs = (ArrayList) session.getAttribute("eventDefinitionCRFs");
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) session.getAttribute("definition");
        IStudyEventDefinitionDAO edao = this.studyEventDefinitionDao;
        if (sed !=null)
        logger.info("Definition bean to be updated:" + sed.getName() + sed.getCategory());

        sed.setUpdater(ub);
        sed.setUpdatedDate(new Date());
        sed.setStatus(Status.AVAILABLE);
        edao.update(sed);

        EventDefinitionCRFDao cdao = this.eventDefinitionCrfDao;
        ICrfDAO crfdao = this.crfDao;

        for (int i = 0; i < edcs.size(); i++) {
            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) edcs.get(i);
            if (edc.getId() > 0) {// need to do update
                edc.setUpdater(ub);
                edc.setUpdatedDate(new Date());
                logger.info("Status:" + edc.getStatus().getName());
                logger.info("version:" + edc.getDefaultVersionId());
                logger.info("Electronic Signature [" + edc.isElectronicSignature() + "]");
                if (!sed.isRepeating()){
                    edc.setAllowAnonymousSubmission(false);
                    edc.setSubmissionUrl("");
                }
                cdao.update(edc);

                
                String crfPath=sed.getOid()+"."+edc.getCrf().getOid();
                getEventDefinitionCrfTagService().saveEventDefnCrfOfflineTag(2, crfPath, edc ,sed);
                
                ArrayList <EventDefinitionCRFBean> eventDefCrfBeans = cdao.findAllByCrfDefinitionInSiteOnly(edc.getStudyEventDefinitionId(), edc.getCrfId());
                for (EventDefinitionCRFBean eventDefCrfBean :eventDefCrfBeans){
                	eventDefCrfBean.setParticipantForm(edc.isParticipantForm());
                	eventDefCrfBean.setAllowAnonymousSubmission(edc.isAllowAnonymousSubmission());          
                	eventDefCrfBean.setStatus(edc.getStatus());
                	
                	cdao.update(eventDefCrfBean);
                }
                
                

                if (edc.getStatus().equals(Status.DELETED)
                        || edc.getStatus().equals(Status.AUTO_DELETED)) {
                    removeAllEventsItems(edc, sed);
                }
                if (edc.getOldStatus()!=null && edc.getOldStatus().equals(Status.DELETED)) {
                    restoreAllEventsItems(edc, sed);
                }

            } else { // to insert
                edc.setOwner(ub);
                edc.setCreatedDate(new Date());
                edc.setStatus(Status.AVAILABLE);
                if (!sed.isRepeating()){
                    edc.setAllowAnonymousSubmission(false);
                    edc.setSubmissionUrl("");
                }
                cdao.create(edc);
                CRFBean cBean = (CRFBean) crfdao.findByPK(edc.getCrfId());                
                String crfPath=sed.getOid()+"."+cBean.getOid();
                getEventDefinitionCrfTagService().saveEventDefnCrfOfflineTag(2, crfPath, edc ,sed);

            }
        }
        session.removeAttribute("definition");
        session.removeAttribute("eventDefinitionCRFs");

        session.removeAttribute("tmpCRFIdMap");
        session.removeAttribute("crfsWithVersion");
        session.removeAttribute("eventDefinitionCRFs");
        
        addPageMessage(respage.getString("the_ED_has_been_updated_succesfully"));
        forwardPage(Page.LIST_DEFINITION_SERVLET);
    }

    public void removeAllEventsItems(EventDefinitionCRFBean edc, StudyEventDefinitionBean sed){
        IStudyEventDAO seDao = this.studyEventDao;
        EventCRFDao ecrfDao = this.eventCrfDao;
        IItemDataDAO iddao = this.itemDataDao;
        
        // Getting Study Events
        ArrayList seList = seDao.findAllByStudyEventDefinitionAndCrfOids(sed.getOid(), edc.getCrf().getOid());
        for (int j = 0; j < seList.size(); j++) {
            StudyEventBean seBean = (StudyEventBean) seList.get(j);
            // Getting Event CRFs
            ArrayList ecrfList = ecrfDao.findAllByStudyEventAndCrfOrCrfVersionOid(seBean, edc.getCrf().getOid());
            for (int k = 0; k < ecrfList.size(); k++) {
                EventCRFBean ecrfBean = (EventCRFBean) ecrfList.get(k);
                ecrfBean.setOldStatus(ecrfBean.getStatus());
                ecrfBean.setStatus(Status.AUTO_DELETED);
                ecrfBean.setUpdater(ub);
                ecrfBean.setUpdatedDate(new Date());
                ecrfDao.update(ecrfBean);
                // Getting Item Data
                ArrayList itemData = iddao.findAllByEventCRFId(ecrfBean.getId());
                // remove all the item data
                for (int a = 0; a < itemData.size(); a++) {
                    ItemDataBean item = (ItemDataBean) itemData.get(a);
                    if (!item.getStatus().equals(Status.DELETED)) {
                        item.setOldStatus(item.getStatus());
                        item.setStatus(Status.AUTO_DELETED);
                        item.setUpdater(ub);
                        item.setUpdatedDate(new Date());
                        iddao.update(item);
                        IDiscrepancyNoteDAO dnDao = this.discrepancyNoteDao;
                        List dnNotesOfRemovedItem = dnDao.findExistingNotesForItemData(item.getId());
                        if (!dnNotesOfRemovedItem.isEmpty()) {
                            DiscrepancyNoteBean itemParentNote = null;
                            for (Object obj : dnNotesOfRemovedItem) {
                                if (((DiscrepancyNoteBean)obj).getParentDnId() == 0) {
                                    itemParentNote = (DiscrepancyNoteBean)obj;
                                }
                            }
                            DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
                            if (itemParentNote != null) {
                                dnb.setParentDnId(itemParentNote.getId());
                                dnb.setDiscrepancyNoteTypeId(itemParentNote.getDiscrepancyNoteTypeId());
                            }
                            dnb.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
                            dnb.setStudyId(currentStudy.getId());
                            dnb.setAssignedUserId(ub.getId());
                            dnb.setOwner(ub);
                            dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
                            dnb.setEntityId(item.getId());
                            dnb.setColumn("value");
                            dnb.setCreatedDate(new Date());
                            dnb.setDescription("The item has been removed, this Discrepancy Note has been Closed.");
                            dnDao.create(dnb);
                            dnDao.createMapping(dnb);
                            itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
                            dnDao.update(itemParentNote);
                        }
                    }
                }
            }
        }
    }

    public void restoreAllEventsItems(EventDefinitionCRFBean edc, StudyEventDefinitionBean sed){
        IStudyEventDAO seDao = this.studyEventDao;
        EventCRFDao ecrfDao = this.eventCrfDao;
        IItemDataDAO iddao = this.itemDataDao;

        // All Study Events
        ArrayList seList = seDao.findAllByStudyEventDefinitionAndCrfOids(sed.getOid(), edc.getCrf().getOid());
        for (int j = 0; j < seList.size(); j++) {
            StudyEventBean seBean = (StudyEventBean) seList.get(j);
            // All Event CRFs
            ArrayList ecrfList = ecrfDao.findAllByStudyEventAndCrfOrCrfVersionOid(seBean, edc.getCrf().getOid());
            for (int k = 0; k < ecrfList.size(); k++) {
                EventCRFBean ecrfBean = (EventCRFBean) ecrfList.get(k);
                ecrfBean.setStatus(ecrfBean.getOldStatus());
                ecrfBean.setUpdater(ub);
                ecrfBean.setUpdatedDate(new Date());
                ecrfDao.update(ecrfBean);
                // All Item Data
                ArrayList itemData = iddao.findAllByEventCRFId(ecrfBean.getId());
                // remove all the item data
                for (int a = 0; a < itemData.size(); a++) {
                    ItemDataBean item = (ItemDataBean) itemData.get(a);
                    if (item.getStatus().equals(Status.DELETED) || item.getStatus().equals(Status.AUTO_DELETED)) {
                        item.setStatus(item.getOldStatus());
                        item.setUpdater(ub);
                        item.setUpdatedDate(new Date());
                        iddao.update(item);
                    }
                }
            }
        }

    }

    public void validateSubmissionUrl(ArrayList <EventDefinitionCRFBean> edcsInSession ,ArrayList <EventDefinitionCRFBean> eventDefCrfList ,Validator v){
    	for (int i = 0; i < edcsInSession.size(); i++) {
            v.addValidation("submissionUrl"+ i, Validator.NO_SPACES_ALLOWED);	
            EventDefinitionCRFBean sessionBean=null;
            boolean isExist = false;
            for (EventDefinitionCRFBean eventDef : eventDefCrfList){
      		  sessionBean = edcsInSession.get(i);
            	if(!sessionBean.isAllowAnonymousSubmission() || !sessionBean.isParticipantForm()){ 
                	isExist = true;
            		break;
            	}
            		System.out.println("iter:           "+eventDef.getId()+            "--db:    "+eventDef.getSubmissionUrl()); 
            		System.out.println("edcsInSession:  "+sessionBean.getId()  + "--session:"+sessionBean.getSubmissionUrl()); 
            		System.out.println();
            	if(sessionBean.getSubmissionUrl().trim().equals("") || sessionBean.getSubmissionUrl().trim() ==null){
            		break;
            	}else{
                if (eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() != sessionBean.getId()) ||
                		(eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() == sessionBean.getId()) && sessionBean.getId()==0)){
                	v.addValidation("submissionUrl"+ i, Validator.SUBMISSION_URL_NOT_UNIQUE);
                	System.out.println("Duplicate ****************************");
                	isExist = true;
            	   break;
            	}else if(eventDef.getSubmissionUrl().trim().equalsIgnoreCase(sessionBean.getSubmissionUrl().trim()) && (eventDef.getId() == sessionBean.getId()) && sessionBean.getId()!=0){
                	System.out.println("Not Duplicate  ***********");
                	isExist = true;
            		break;
            	}
            }
            }
            	if(!isExist){ 
            		eventDefCrfList.add(sessionBean);
            	}
        }

    }
    
    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
        eventDefinitionCrfTagService=
         this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

         return eventDefinitionCrfTagService;
     }

}
        
        
        
    
    
