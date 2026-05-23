/*
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.researchedc.control.admin;

import java.util.ArrayList;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings("serial")
public class BatchCRFMigrationServlet extends SecureController {

    @Autowired
    private CRFVersionDAO crfVersionDao;

    private static String CRF_ID = "crfId";
    private static String CRF = "crf";
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void processRequest() throws Exception {


        FormProcessor fp = new FormProcessor(request);
        
        ArrayList<CRFVersionBean> crfVersionList=null;
        ArrayList<StudyEventDefinitionBean> eventList = null;
        ArrayList<StudyBean> siteList = null;
        
        
        // checks which module the requests are from, manage or admin
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int crfId = fp.getInt(CRF_ID);
        if (crfId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_view"));
            forwardPage(Page.CRF_LIST);
        } else {
            ICrfDAO cdao = this.crfDao;
            CRFVersionDAO vdao = this.crfVersionDao;
            CRFBean crf = (CRFBean) cdao.findByPK(crfId);
            request.setAttribute("crfName", crf.getName());
            ArrayList<CRFVersionBean> versions = (ArrayList<CRFVersionBean>) vdao.findAllByCRF(crfId);
            crfVersionList = new ArrayList<CRFVersionBean>();
             for(CRFVersionBean version:versions){
                 if(version.getStatus().isAvailable())
                     crfVersionList.add(version);
             }                       
            crf.setVersions(crfVersionList);
            ArrayList<StudyBean> listOfSites = (ArrayList<StudyBean>) sdao().findAllByParent(currentStudy.getId());
            siteList = new ArrayList<StudyBean>();
            StudyBean studyBean = new StudyBean();
            studyBean.setOid(currentStudy.getOid()); 
            studyBean.setName(resterm.getString("Study_Level_Subjects_Only"));
            siteList.add(studyBean);
            for (StudyBean s : listOfSites) {
                if (s.getStatus().isAvailable()) {
                    siteList.add(s);
                }
            }
     
            ArrayList<StudyEventDefinitionBean> listOfDefn = seddao().findAllByStudy(currentStudy);
            eventList = new ArrayList<StudyEventDefinitionBean>();
            for (StudyEventDefinitionBean d : listOfDefn) {
                if (d.getStatus().isAvailable()) {
                    eventList.add(d);
                }
            }
    
            // if coming from change crf version -> display message
     String crfVersionChangeMsg = fp.getString("isFromCRFVersionBatchChange");
     if ( crfVersionChangeMsg!= null && !crfVersionChangeMsg.equals("")){
         addPageMessage(crfVersionChangeMsg);
    }

            
            request.setAttribute("study", currentStudy);
            request.setAttribute("siteList", siteList);
            request.setAttribute("eventList", eventList);
            request.setAttribute(CRF, crf);
            forwardPage(Page.BATCH_CRF_MIGRATION);

        }
    }


    @SuppressWarnings("rawtypes")
    private IStudyDAO sdao() {
        return this.studyDao;
    }

    @SuppressWarnings("rawtypes")
    private IStudyEventDefinitionDAO seddao() {
        return this.studyEventDefinitionDao;
    }



}
