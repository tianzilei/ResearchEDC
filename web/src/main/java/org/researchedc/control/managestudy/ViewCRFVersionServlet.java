/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.bean.submit.ItemGroupMetadataBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.dao.submit.ItemGroupDAO;
import org.researchedc.dao.submit.ItemGroupMetadataDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ViewCRFVersionServlet extends SecureController {
    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int crfVersionId = fp.getInt("id");

        if (crfVersionId == 0) {
            addPageMessage(respage.getString("please_choose_a_crf_to_view_details"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            CRFVersionBean version = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            // tbh
            CRFDAO crfdao = new CRFDAO(sm.getDataSource());
            CRFBean crf = (CRFBean) crfdao.findByPK(version.getCrfId());
            CRFVersionMetadataUtil metadataUtil = new CRFVersionMetadataUtil(sm.getDataSource());
            ArrayList<SectionBean> sections = metadataUtil.retrieveFormMetadata(version); 
            request.setAttribute("sections", sections);
            request.setAttribute("version", version);
            // tbh
            request.setAttribute("crfname", crf.getName());
            // tbh
            forwardPage(Page.VIEW_CRF_VERSION);

        }
    }

}
