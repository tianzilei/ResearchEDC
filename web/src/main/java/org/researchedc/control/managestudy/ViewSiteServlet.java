/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.core.SecureController;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.domain.SourceDataVerification;
import org.researchedc.service.managestudy.EventDefinitionCrfTagService;
import org.researchedc.service.pmanage.Authorization;
import org.researchedc.service.pmanage.ParticipantPortalRegistrar;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * @author jxu
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ViewSiteServlet extends SecureController {
    
    @Autowired
    private CRFVersionDAO crfVersionDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;

EventDefinitionCrfTagService eventDefinitionCrfTagService = null;

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }
        int siteId = request.getParameter("id") == null ? 0 : Integer.valueOf(request.getParameter("id"));
        if (currentStudy.getId() == siteId) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        IStudyDAO sdao = this.studyDao;
        String idString = "";
        if (request.getAttribute("siteId") == null) {
            idString = request.getParameter("id");
        } else {
            idString = request.getAttribute("siteId").toString();
        }
        logger.info("site id:" + idString);
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_site_to_edit"));
            forwardPage(Page.SITE_LIST_SERVLET);
        } else {
            int siteId = Integer.valueOf(idString.trim()).intValue();
            StudyBean study = (StudyBean) sdao.findByPK(siteId);

            checkRoleByUserAndStudy(ub, study.getParentStudyId(), study.getId());
            // if (currentStudy.getId() != study.getId()) {

            ArrayList configs = new ArrayList();
            IStudyParameterValueDAO spvdao = this.studyParameterValueDao;
            configs = spvdao.findParamConfigByStudy(study);
            study.setStudyParameters(configs);

            // }

            String parentStudyName = "";
            if (study.getParentStudyId() > 0) {
                StudyBean parent = (StudyBean) sdao.findByPK(study.getParentStudyId());
                parentStudyName = parent.getName();
            }
            request.setAttribute("parentName", parentStudyName);
            request.setAttribute("siteToView", study);
            request.setAttribute("idToSort", request.getAttribute("idToSort"));
            viewSiteEventDefinitions(study);

            forwardPage(Page.VIEW_SITE);
        }
    }

    private void viewSiteEventDefinitions(StudyBean siteToView) throws MalformedURLException {
        int siteId = siteToView.getId();
        ArrayList<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
        IStudyEventDefinitionDAO sedDao = this.studyEventDefinitionDao;
        EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;
        CRFVersionDAO cvdao = this.crfVersionDao;
        ICrfDAO cdao = this.crfDao;
        seds = sedDao.findAllByStudy(siteToView);
        int start = 0;
        for (StudyEventDefinitionBean sed : seds) {
            IStudyParameterValueDAO spvdao = this.studyParameterValueDao;    
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();       
            request.setAttribute("participateFormStatus",participateFormStatus );            
            if (participateFormStatus.equals("enabled")) baseUrl();
        
            request.setAttribute("participateFormStatus",participateFormStatus );

            int defId = sed.getId();
            ArrayList<EventDefinitionCRFBean> edcs =
                (ArrayList<EventDefinitionCRFBean>) edcdao.findAllByDefinitionAndSiteIdAndParentStudyId(defId, siteId, siteToView.getParentStudyId());
            ArrayList<EventDefinitionCRFBean> defCrfs = new ArrayList<EventDefinitionCRFBean>();
            for (EventDefinitionCRFBean edcBean : edcs) {
                CRFBean cBean = (CRFBean) cdao.findByPK(edcBean.getCrfId());                
                String crfPath=sed.getOid()+"."+cBean.getOid();
                edcBean.setOffline(getEventDefinitionCrfTagService().getEventDefnCrfOfflineStatus(2,crfPath,true));
                
                int edcStatusId = edcBean.getStatus().getId();
                CRFBean crf = (CRFBean) cdao.findByPK(edcBean.getCrfId());
                int crfStatusId = crf.getStatusId();
                ArrayList<CRFVersionBean> versions = (ArrayList<CRFVersionBean>) cvdao.findAllActiveByCRF(edcBean.getCrfId());
                edcBean.setVersions(versions);
                edcBean.setCrfName(crf.getName());
                CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
                edcBean.setDefaultVersionName(defaultVersion.getName());
                String sversionIds = edcBean.getSelectedVersionIds();
                ArrayList<Integer> idList = new ArrayList<Integer>();
                String idNames = "";
                if (sversionIds.length() > 0) {
                    String[] ids = sversionIds.split("\\,");
                    for (String id : ids) {
                        idList.add(Integer.valueOf(id));
                        for (CRFVersionBean v : versions) {
                            if (v.getId() == Integer.valueOf(id)) {
                                idNames += v.getName() + ",";
                                break;
                            }
                        }
                    }
                    idNames = idNames.substring(0, idNames.length() - 1);
                }
                if(edcBean.getParentId()<1){
                	edcBean.setSubmissionUrl("");
                }
                edcBean.setSelectedVersionIdList(idList);
                edcBean.setSelectedVersionNames(idNames);
                defCrfs.add(edcBean);
                ++start;
            }
            sed.setCrfs(defCrfs);
            sed.setCrfNum(defCrfs.size());
        }

        request.setAttribute("definitions", seds);
        ArrayList<String> sdvOptions = new ArrayList<String>();
        sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
        request.setAttribute("sdvOptions", sdvOptions);

    }

    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
        eventDefinitionCrfTagService=
         this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

         return eventDefinitionCrfTagService;
     }

}
