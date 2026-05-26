/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.DisplayTableOfContentsBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.submit.TableOfContentsServlet;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;
import org.researchedc.dao.spi.DaoProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * To view the table of content of an event CRF
 *
 * @author jxu
 */
public class ViewTableOfContentServlet extends SecureController {
    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.RESEARCHASSISTANT) || currentRole.getRole().equals(Role.RESEARCHASSISTANT2)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int crfVersionId = fp.getInt("crfVersionId");
        // YW <<
        int sedId = fp.getInt("sedId");
        request.setAttribute("sedId", Integer.valueOf(sedId) + "");
        // YW >>
        DisplayTableOfContentsBean displayBean = getDisplayBean(sm.getDataSource(), crfVersionId);
        request.setAttribute("toc", displayBean);
        forwardPage(Page.VIEW_TABLE_OF_CONTENT);
    }

    public static DisplayTableOfContentsBean getDisplayBean(DataSource ds, int crfVersionId) {
        DisplayTableOfContentsBean answer = new DisplayTableOfContentsBean();

        SectionDAO sdao = DaoProvider.getDao(SectionDAO.class);
        ArrayList sections = getSections(crfVersionId, ds);
        answer.setSections(sections);

        CRFVersionDAO cvdao = DaoProvider.getDao(CRFVersionDAO.class);
        CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
        answer.setCrfVersion(cvb);

        ICrfDAO cdao = DaoProvider.getDao(CRFDAO.class);
        CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());
        answer.setCrf(cb);

        answer.setEventCRF(new EventCRFBean());

        answer.setStudyEventDefinition(new StudyEventDefinitionBean());

        return answer;
    }

    public static ArrayList getSections(int crfVersionId, DataSource ds) {
        SectionDAO sdao = DaoProvider.getDao(SectionDAO.class);

        HashMap numItemsBySectionId = sdao.getNumItemsBySectionId();
        ArrayList sections = sdao.findAllByCRFVersionId(crfVersionId);

        for (int i = 0; i < sections.size(); i++) {
            SectionBean sb = (SectionBean) sections.get(i);

            int sectionId = sb.getId();
            Integer key = Integer.valueOf(sectionId);
            sb.setNumItems(TableOfContentsServlet.getIntById(numItemsBySectionId, key));
            sections.set(i, sb);
        }

        return sections;
    }

}
