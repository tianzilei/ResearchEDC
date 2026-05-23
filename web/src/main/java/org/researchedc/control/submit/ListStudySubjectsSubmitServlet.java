/*
 * Created on Jan 21, 2005
 */
package org.researchedc.control.submit;

import org.researchedc.control.managestudy.ListStudySubjectServlet;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ssachs
 */
public class ListStudySubjectsSubmitServlet extends ListStudySubjectServlet {

    Locale locale;

    // < ResourceBundleresexception,respage;

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.managestudy.ListStudySubjectServlet#getJSP()
     */
    @Override
    protected Page getJSP() {
        return Page.SUBMIT_DATA;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // <
        // resexception=ResourceBundle.getBundle("org.researchedc.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",locale);

        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.managestudy.ListStudySubjectServlet#getBaseURL()
     */
    @Override
    protected String getBaseURL() {
        return "ListStudySubjectsSubmit";
    }
}
