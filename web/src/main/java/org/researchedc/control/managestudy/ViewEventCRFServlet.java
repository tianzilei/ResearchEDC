/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.DisplayItemBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.managestudy.StudySubjectDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * Views the detail of an event CRF
 */
public class ViewEventCRFServlet extends SecureController {
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int eventCRFId = fp.getInt("id", true);
        int studySubId = fp.getInt("studySubId", true);

        IStudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        EventCRFDao ecdao = new EventCRFDAO(sm.getDataSource());
        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(sm.getDataSource());
        ICrfDAO cdao = new CRFDAO(sm.getDataSource());
        SectionDAO secdao = new SectionDAO(sm.getDataSource());

        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_view"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
        } else {
            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            request.setAttribute("studySub", studySub);

            EventCRFBean eventCRF = (EventCRFBean) ecdao.findByPK(eventCRFId);
            CRFBean crf = cdao.findByVersionId(eventCRF.getCRFVersionId());
            request.setAttribute("crf", crf);

            ArrayList sections = secdao.findAllByCRFVersionId(eventCRF.getCRFVersionId());
            for (int j = 0; j < sections.size(); j++) {
                SectionBean section = (SectionBean) sections.get(j);
                ArrayList itemData = iddao.findAllByEventCRFId(eventCRFId);

                ArrayList displayItemData = new ArrayList();
                for (int i = 0; i < itemData.size(); i++) {
                    ItemDataBean id = (ItemDataBean) itemData.get(i);
                    DisplayItemBean dib = new DisplayItemBean();
                    ItemBean item = (ItemBean) idao.findByPK(id.getItemId());
                    ItemFormMetadataBean ifm = ifmdao.findByItemIdAndCRFVersionId(item.getId(), eventCRF.getCRFVersionId());

                    item.setItemMeta(ifm);
                    dib.setItem(item);
                    dib.setData(id);
                    dib.setMetadata(ifm);
                    displayItemData.add(dib);
                }
                section.setItems(displayItemData);
            }

            request.setAttribute("sections", sections);
            request.setAttribute("studySubId", new Integer(studySubId).toString());
            forwardPage(Page.VIEW_EVENT_CRF);
        }
    }

}
