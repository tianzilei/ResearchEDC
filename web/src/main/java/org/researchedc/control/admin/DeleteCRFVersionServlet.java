/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.admin;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.admin.NewCRFBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.managestudy.StudySubjectDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.domain.datamap.VersioningMap;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DeleteCRFVersionServlet extends SecureController {
    public static final String VERSION_ID = "verId";

    public static final String VERSION_TO_DELETE = "version";

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.CRF_LIST_SERVLET, "not admin", "1");
    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int versionId = fp.getInt(VERSION_ID, true);
        String action = request.getParameter("action");
        if (versionId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_version_to_delete"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
            StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(sm.getDataSource());
            StudyEventDAO seDao = new StudyEventDAO(sm.getDataSource());
            ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
            EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
            StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
            CRFVersionBean version = (CRFVersionBean) cvdao.findByPK(versionId);

            // find definitions using this version
            ArrayList definitions = edcdao.findByDefaultVersion(version.getId());
            for (Object edcBean: definitions) {
                StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean)sedDao.findByPK(((EventDefinitionCRFBean)edcBean).getStudyEventDefinitionId());
                ((EventDefinitionCRFBean)edcBean).setEventName(sedBean.getName());
            }

            // find event crfs using this version
            		
            ArrayList<ItemDataBean> idBeans = iddao.findByCRFVersion(version);
            ArrayList <EventCRFBean> eCRFs = ecdao.findAllByCRF(version.getCrfId());
               for(EventCRFBean eCRF : eCRFs){
            	   
            	   StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(eCRF.getStudySubjectId());
            	   eCRF.setStudySubject(ssBean);
                   StudyEventBean seBean = (StudyEventBean) seDao.findByPK(eCRF.getStudyEventId());
                   StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) sedDao.findByPK(seBean.getStudyEventDefinitionId());
                   seBean.setStudyEventDefinition(sedBean);
                   eCRF.setStudyEvent(seBean);
               }
            
            ArrayList eventCRFs = ecdao.findAllByCRFVersion(versionId);
            boolean canDelete = true;
            if (!definitions.isEmpty()) {// used in definition
                canDelete = false;
                request.setAttribute("definitions", definitions);
                addPageMessage(respage.getString("this_CRF_version") + " "+ version.getName()
                    + respage.getString("has_associated_study_events_definitions_cannot_delete"));

            } else if (!idBeans.isEmpty()) {
                canDelete = false;
                request.setAttribute("eventCRFs", eCRFs);
                request.setAttribute("itemDataForVersion", idBeans);
                addPageMessage(respage.getString("this_CRF_version") +" "+ version.getName() + respage.getString("has_associated_item_data_cannot_delete"));
            
            } else if (!eventCRFs.isEmpty()) {
                canDelete = false;
                request.setAttribute("eventsForVersion", eventCRFs);
                addPageMessage(respage.getString("this_CRF_version") + " "+ version.getName() + respage.getString("has_associated_study_events_cannot_delete"));
            }
            if ("confirm".equalsIgnoreCase(action)) {
                request.setAttribute(VERSION_TO_DELETE, version);
                forwardPage(Page.DELETE_CRF_VERSION);
            } else {
                // submit
                if (canDelete) {
                    ArrayList items = cvdao.findNotSharedItemsByVersion(versionId);
                    NewCRFBean nib = new NewCRFBean(sm.getDataSource(), version.getCrfId());
                    nib.setDeleteQueries(cvdao.generateDeleteQueries(versionId, items));
                    nib.deleteFromDB();
                    addPageMessage(respage.getString("the_CRF_version_has_been_deleted_succesfully"));
                } else {
                    addPageMessage(respage.getString("the_CRF_version_cannot_be_deleted"));
                }
                forwardPage(Page.CRF_LIST_SERVLET);
            }

        }

    }

}
