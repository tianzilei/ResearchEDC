/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.core.form.StringUtil;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RestoreCRFFromDefinitionServlet extends SecureController {
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

        addPageMessage(respage.getString("no_have_permission_to_update_study_event_definition") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.STUDY_EVENT_DEFINITION_LIST, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        ArrayList edcs = (ArrayList) session.getAttribute("eventDefinitionCRFs");
        String crfName = "";

        String idString = request.getParameter("id");
        logger.info("crf id:" + idString);

        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) session.getAttribute("definition");
        IStudyParameterValueDAO spvdao = this.studyParameterValueDao;    
        String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
    
        request.setAttribute("participateFormStatus",participateFormStatus );

        
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_restore"));
            forwardPage(Page.UPDATE_EVENT_DEFINITION1);
        } else {
            // event crf definition id
            int id = Integer.valueOf(idString.trim()).intValue();
            for (int i = 0; i < edcs.size(); i++) {
                EventDefinitionCRFBean edc = (EventDefinitionCRFBean) edcs.get(i);
                if (edc.getCrfId() == id) {
                    edc.setStatus(Status.AVAILABLE);
                    edc.setOldStatus(Status.DELETED);
                    crfName = edc.getCrfName();
                }

            }
            session.setAttribute("eventDefinitionCRFs", edcs);
            addPageMessage(crfName + " " + respage.getString("has_been_restored"));
            forwardPage(Page.UPDATE_EVENT_DEFINITION1);
        }

    }
}
