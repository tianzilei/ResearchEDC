package org.researchedc.control.managestudy;

/**
 *
 */

import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;
import org.researchedc.dao.submit.SubjectGroupMapDAO;
import org.researchedc.dao.managestudy.StudyGroupClassDAO;
import org.researchedc.dao.managestudy.StudyGroupDAO;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.submit.ListDiscNotesForCRFTableFactory;
import org.researchedc.control.submit.SubmitDataServlet;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.dao.spi.SubjectGroupMapDao;
import org.researchedc.dao.spi.StudyGroupDao;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.springframework.beans.factory.annotation.Autowired;

public class ListDiscNotesForCRFServlet extends SecureController {

    
    @Autowired
    private IDiscrepancyNoteDAO discrepancyNoteDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;
    @Autowired
    private ISubjectDAO subjectDao;
    @Autowired
    private SubjectGroupMapDAO subjectGroupMapDao;

public static final String DISCREPANCY_NOTE_TYPE = "discrepancyNoteType";
    public static final String RESOLUTION_STATUS = "resolutionStatus";
    public static final String FILTER_SUMMARY = "filterSummary";
    Locale locale;
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
    private IDiscrepancyNoteDAO discrepancyNoteDAO;
    private ICrfDAO crfDAO;

    // < ResourceBundleresword;
    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.core.SecureController#mayProceed()
     */
    public static boolean mayViewDN(UserAccountBean ub, StudyUserRoleBean currentRole) {
    	if (currentRole != null) {
            Role r = currentRole.getRole();

            if (r != null && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) ||
                    r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r.equals(Role.RESEARCHASSISTANT2) ||r.equals(Role.MONITOR) )) {
                return true;
            }
        }

        return false;
    }

    
    
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < resword =
        // ResourceBundle.getBundle("org.researchedc.i18n.words",locale);

        if (ub.isSysAdmin()) {
            return;
        }

        
        if (ListDiscNotesForCRFServlet.mayViewDN(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    public void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        // Determine whether to limit the displayed DN's to a certain DN type
        int resolutionStatus = 0;
        try {
            resolutionStatus = Integer.parseInt(request.getParameter("resolutionStatus"));
        } catch (NumberFormatException nfe) {
            // Show all DN's
            resolutionStatus = -1;
        }
        // request.setAttribute(RESOLUTION_STATUS,resolutionStatus);

        // Determine whether we already have a collection of resolutionStatus
        // Ids, and if not
        // create a new attribute. If there is no resolution status, then the
        // Set object should be cleared,
        // because we do not have to save a set of filter IDs.
        boolean hasAResolutionStatus = resolutionStatus >= 1 && resolutionStatus <= 5;
        Set<Integer> resolutionStatusIds = (HashSet) session.getAttribute(RESOLUTION_STATUS);
        // remove the session if there is no resolution status
        if (!hasAResolutionStatus && resolutionStatusIds != null) {
            session.removeAttribute(RESOLUTION_STATUS);
            resolutionStatusIds = null;
        }
        if (hasAResolutionStatus) {
            if (resolutionStatusIds == null) {
                resolutionStatusIds = new HashSet<Integer>();
            }
            resolutionStatusIds.add(resolutionStatus);
            session.setAttribute(RESOLUTION_STATUS, resolutionStatusIds);
        }
        int discNoteType = 0;
        try {
            discNoteType = Integer.parseInt(request.getParameter("type"));
        } catch (NumberFormatException nfe) {
            // Show all DN's
            discNoteType = -1;
        }
        request.setAttribute(DISCREPANCY_NOTE_TYPE, discNoteType);

        /*
         * DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil(); //
         * Generate a summary of how we are filtering; Map<String, List<String>>
         * filterSummary = discNoteUtil.generateFilterSummary(discNoteType,
         * resolutionStatusIds);
         *
         * if (!filterSummary.isEmpty()) { request.setAttribute(FILTER_SUMMARY,
         * filterSummary); }
         */

        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int definitionId = fp.getInt("defId");
        int tabId = fp.getInt("tab");
        if (definitionId <= 0) {
            addPageMessage(respage.getString("please_choose_an_ED_ta_to_vies_details"));
            forwardPage(Page.LIST_SUBJECT_DISC_NOTE_SERVLET);
            return;
        }

        request.setAttribute("eventDefinitionId", definitionId);

        ListDiscNotesForCRFTableFactory factory = new ListDiscNotesForCRFTableFactory();
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
        factory.setDiscrepancyNoteDAO(getDiscrepancyNoteDAO());
        // factory.setStudyHasDiscNotes(allThreadedDiscNotes != null &&
        // !allThreadedDiscNotes.isEmpty());
        factory.setDiscNoteType(discNoteType);
        factory.setModule(module);
        factory.setResolutionStatus(resolutionStatus);
        factory.setResolutionStatusIds(resolutionStatusIds);
        factory.setSelectedStudyEventDefinition((StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(definitionId));
        String listDiscNotesForCRFHtml = factory.createTable(request, response).render();
        request.setAttribute("listDiscNotesForCRFHtml", listDiscNotesForCRFHtml);
        request.setAttribute("defId", definitionId);

        forwardPage(Page.LIST_DNOTES_FOR_CRF);
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

    public StudyGroupDAO getStudyGroupDAO() {
        studyGroupDAO = this.studyGroupDAO == null ? this.studyGroupDao : studyGroupDAO;
        return (StudyGroupDAO) studyGroupDAO;
    }

    public DiscrepancyNoteDAO getDiscrepancyNoteDAO() {
        discrepancyNoteDAO = this.discrepancyNoteDAO == null ? this.discrepancyNoteDao : discrepancyNoteDAO;
        return (DiscrepancyNoteDAO) discrepancyNoteDAO;
    }

}
