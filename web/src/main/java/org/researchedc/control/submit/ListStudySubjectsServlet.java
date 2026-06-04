/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2009 Akaza Research
 */
package org.researchedc.control.submit;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.dao.submit.SubjectGroupMapDAO;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormDiscrepancyNotes;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.dao.spi.SubjectGroupMapDao;
import org.researchedc.dao.spi.StudyGroupDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Servlet for creating a table.
 *
 * @author Krikor Krumlian
 */
public class ListStudySubjectsServlet extends SecureController {

    // Shaoyu Su
    private static final long serialVersionUID = 1L;
    private IStudyEventDefinitionDAO studyEventDefinitionDAO;
    private ISubjectDAO subjectDAO;
    private IStudySubjectDAO studySubjectDAO;
    private IStudyEventDAO studyEventDAO;
    private StudyGroupClassDao studyGroupClassDAO;
    private SubjectGroupMapDao subjectGroupMapDAO;
    private IStudyDAO studyDAO;
    private EventCRFDao eventCRFDAO;
    private EventDefinitionCRFDao eventDefintionCRFDAO;
    private StudyGroupDao studyGroupDAO;
    private boolean showMoreLink;
    private IStudyParameterValueDAO studyParameterValueDAO;
    Locale locale;

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);

        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    protected void processRequest() throws Exception {
        getCrfLocker().unlockAllForUser(ub.getId());
        FormProcessor fp = new FormProcessor(request);
        if(fp.getString("showMoreLink").equals("")){
            showMoreLink = true;
        }else {
            showMoreLink = Boolean.parseBoolean(fp.getString("showMoreLink"));
        }
        String idSetting = currentStudy.getStudyParameterConfig().getSubjectIdGeneration();
        // set up auto study subject id
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
            //Shaoyu Su
            // int nextLabel = getStudySubjectDAO().findTheGreatestLabel() + 1;
            // request.setAttribute("label", new Integer(nextLabel).toString());
            request.setAttribute("label", resword.getString("id_generated_Save_Add"));
            fp.addPresetValue("label", resword.getString("id_generated_Save_Add"));
        }

        if (fp.getRequest().getParameter("subjectOverlay") == null){
            Date today = new Date(System.currentTimeMillis());
            String todayFormatted = local_df.format(today);
            if (request.getAttribute(PRESET_VALUES) != null) {
                fp.setPresetValues((HashMap)request.getAttribute(PRESET_VALUES));
            }
            fp.addPresetValue(AddNewSubjectServlet.INPUT_ENROLLMENT_DATE, todayFormatted);
            fp.addPresetValue(AddNewSubjectServlet.INPUT_EVENT_START_DATE, todayFormatted);
            setPresetValues(fp.getPresetValues());
        }

        request.setAttribute("closeInfoShowIcons", true);
        if (fp.getString("navBar").equals("yes") && fp.getString("findSubjects_f_studySubject.label").trim().length() > 0) {
            StudySubjectBean studySubject = getStudySubjectDAO().findByLabelAndStudy(fp.getString("findSubjects_f_studySubject.label"), currentStudy);
            if (studySubject.getId() > 0) {
                request.setAttribute("id", Integer.valueOf(studySubject.getId()).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            } else {
                createTable();
            }
        } else {
            createTable();
        }

    }

    private void createTable() {

        ListStudySubjectTableFactory factory = new ListStudySubjectTableFactory(showMoreLink);
        factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
        factory.setSubjectDAO(getSubjectDAO());
        factory.setStudySubjectDAO(getStudySubjectDAO());
        factory.setStudyEventDAO(getStudyEventDAO());
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(getStudyGroupClassDao());
        factory.setSubjectGroupMapDAO(getSubjectGroupMapDao());
        factory.setStudyDAO(getStudyDAO());
        factory.setCurrentRole(currentRole);
        factory.setCurrentUser(ub);
        factory.setEventCRFDAO(getEventCRFDAO());
        factory.setEventDefintionCRFDAO(getEventDefinitionCRFDAO());
        factory.setStudyGroupDAO(getStudyGroupDao());
        factory.setStudyParameterValueDAO(getStudyParameterValueDao());
        String findSubjectsHtml = factory.createTable(request, response).render();

        request.setAttribute("findSubjectsHtml", findSubjectsHtml);
        // A. Hamid.
        // For event definitions and group class list in the add subject popup
        request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
        request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
        session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);

        forwardPage(Page.LIST_STUDY_SUBJECTS);

    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
    
    public IStudyParameterValueDAO getStudyParameterValueDao() {
        studyParameterValueDAO = this.studyParameterValueDAO == null ? this.studyParameterValueDao : studyParameterValueDAO;
		return studyParameterValueDAO;
	}

	public void setStudyParameterValueDAO(IStudyParameterValueDAO studyParameterValueDAO) {
		this.studyParameterValueDAO = studyParameterValueDAO;
	}

	public IStudyEventDefinitionDAO getStudyEventDefinitionDao() {
        studyEventDefinitionDAO = studyEventDefinitionDAO == null ? this.studyEventDefinitionDao : studyEventDefinitionDAO;
        return studyEventDefinitionDAO;
    }

    public ISubjectDAO getSubjectDAO() {
        subjectDAO = this.subjectDAO == null ? this.subjectDao : subjectDAO;
        return subjectDAO;
    }

    public IStudySubjectDAO getStudySubjectDAO() {
        studySubjectDAO = this.studySubjectDAO == null ? this.studySubjectDao : studySubjectDAO;
        return studySubjectDAO;
    }

    public StudyGroupClassDao getStudyGroupClassDao() {
        studyGroupClassDAO = this.studyGroupClassDAO == null ? this.studyGroupClassDao : studyGroupClassDAO;
        return studyGroupClassDAO;
    }

    public SubjectGroupMapDao getSubjectGroupMapDao() {
        subjectGroupMapDAO = this.subjectGroupMapDAO == null ? this.subjectGroupMapDao : subjectGroupMapDAO;
        return subjectGroupMapDAO;
    }

    public IStudyEventDAO getStudyEventDAO() {
        studyEventDAO = this.studyEventDAO == null ? this.studyEventDao : studyEventDAO;
        return studyEventDAO;
    }

    public IStudyDAO getStudyDAO() {
        studyDAO = this.studyDAO == null ? this.studyDao : studyDAO;
        return studyDAO;
    }

    public EventCRFDao getEventCRFDAO() {
        eventCRFDAO = this.eventCRFDAO == null ? this.eventCrfDao : eventCRFDAO;
        return eventCRFDAO;
    }

    public EventDefinitionCRFDao getEventDefinitionCRFDAO() {
        eventDefintionCRFDAO = this.eventDefintionCRFDAO == null ? this.eventDefinitionCrfDao : eventDefintionCRFDAO;
        return eventDefintionCRFDAO;
    }

    public StudyGroupDao getStudyGroupDao() {
        studyGroupDAO = this.studyGroupDAO == null ? this.studyGroupDao : studyGroupDAO;
        return studyGroupDAO;
    }

}
