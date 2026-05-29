/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.dao.submit.SubjectGroupMapDAO;
import org.researchedc.dao.managestudy.StudyGroupClassDAO;
import org.researchedc.dao.managestudy.StudyGroupDAO;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormDiscrepancyNotes;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.submit.AddNewSubjectServlet;
import org.researchedc.control.submit.SubmitDataServlet;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.Locale;
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.dao.spi.SubjectGroupMapDao;
import org.researchedc.dao.spi.StudyGroupDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Krikor Krumlian
 */
public class ListEventsForSubjectsServlet extends SecureController {

    
    @Autowired
    private CRFVersionDAO crfVersionDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;
    @Autowired
    private ISubjectDAO subjectDao;
    @Autowired
    private SubjectGroupMapDAO subjectGroupMapDao;

// Shaoyu Su
    private static final long serialVersionUID = 1L;
    private IStudyEventDefinitionDAO studyEventDefinitionDAO;
    private ISubjectDAO subjectDAO;
    private IStudySubjectDAO studySubjectDAO;
    private IStudyEventDAO studyEventDAO;
    private StudyGroupClassDao studyGroupClassDAO;
    private SubjectGroupMapDao subjectGroupMapDAO;
    private IStudyDAO studyDAO;
    private StudyGroupDao studyGroupDAO;
    private EventCRFDao eventCRFDAO;
    private EventDefinitionCRFDao eventDefintionCRFDAO;
    private ICrfDAO crfDAO;
    Locale locale;
    private boolean showMoreLink;
	private Object crfVersionDAO;
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
    public void processRequest() throws Exception {

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
            //int nextLabel = getStudySubjectDAO().findTheGreatestLabel() + 1;
            //request.setAttribute("label", new Integer(nextLabel).toString());
            request.setAttribute("label", resword.getString("id_generated_Save_Add"));
        }

        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int definitionId = fp.getInt("defId");
        if (definitionId <= 0) {
            addPageMessage(respage.getString("please_choose_an_ED_ta_to_vies_details"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
            return;
        }

        ListEventsForSubjectTableFactory factory = new ListEventsForSubjectTableFactory(showMoreLink);
        factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
        factory.setSubjectDAO(getSubjectDAO());
        factory.setStudySubjectDAO(getStudySubjectDAO());
        factory.setStudyEventDAO(getStudyEventDAO());
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(getStudyGroupClassDAO());
        factory.setSubjectGroupMapDAO(getSubjectGroupMapDAO());
        factory.setStudyDAO(getStudyDAO());
        factory.setStudyGroupDAO(getStudyGroupDAO());
        factory.setCurrentRole(currentRole);
        factory.setCurrentUser(ub);
        factory.setEventCRFDAO(getEventCRFDAO());
        factory.setEventDefintionCRFDAO(getEventDefinitionCRFDAO());
        factory.setCrfDAO(getCrfDAO());
        factory.setCrfVersionDAO(getCRFVersionDAO());
        factory.setSelectedStudyEventDefinition((StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(definitionId));
        String listEventsForSubjectsHtml = factory.createTable(request, response).render();
        request.setAttribute("listEventsForSubjectsHtml", listEventsForSubjectsHtml);
        request.setAttribute("defId", definitionId);
        // A. Hamid.
        // For event definitions and group class list in the add subject popup
        request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
        request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
        session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
        //
        forwardPage(Page.LIST_EVENTS_FOR_SUBJECTS);

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

    public StudyGroupClassDAO getStudyGroupClassDAO() {
        studyGroupClassDAO = this.studyGroupClassDAO == null ? this.studyGroupClassDao : studyGroupClassDAO;
        return (StudyGroupClassDAO) studyGroupClassDAO;
    }

    public SubjectGroupMapDAO getSubjectGroupMapDAO() {
        subjectGroupMapDAO = this.subjectGroupMapDAO == null ? this.subjectGroupMapDao : subjectGroupMapDAO;
        return (SubjectGroupMapDAO) subjectGroupMapDAO;
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

    public ICrfDAO getCrfDAO() {
        crfDAO = this.crfDAO == null ? this.crfDao : crfDAO;
        return crfDAO;
    }

    public CRFVersionDAO getCRFVersionDAO(){
    	CRFVersionDAO	crfVersionDAO =this.crfVersionDao;
    	return crfVersionDAO;
    	}
    public StudyGroupDAO getStudyGroupDAO() {
        studyGroupDAO = this.studyGroupDAO == null ? this.studyGroupDao : studyGroupDAO;
        return (StudyGroupDAO) studyGroupDAO;
    }

}
