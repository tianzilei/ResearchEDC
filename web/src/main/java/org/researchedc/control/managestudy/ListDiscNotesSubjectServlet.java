/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.dao.submit.SubjectGroupMapDAO;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.submit.ListDiscNotesSubjectTableFactory;
import org.researchedc.control.submit.SubmitDataServlet;
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
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.dao.spi.StudyGroupDao;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.service.DiscrepancyNoteUtil;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;

/**
 * @author Bruce W. Perry, 5/1/08
 */
public class ListDiscNotesSubjectServlet extends SecureController {
    
    @Autowired
    private IDiscrepancyNoteDAO discrepancyNoteDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;
    @Autowired
    private ISubjectDAO subjectDao;
    @Autowired
    private SubjectGroupMapDAO subjectGroupMapDao;

public static final String RESOLUTION_STATUS = "resolutionStatus";
    // Include extra path info on the URL, which generates a file name hint in
    // some
    // browser's "save as..." dialog boxes
    public static final String EXTRA_PATH_INFO = "discrepancyNoteReport";
    public static final String DISCREPANCY_NOTE_TYPE = "discrepancyNoteType";
    public static final String FILTER_SUMMARY = "filterSummary";
    Locale locale;

    // < ResourceBundleresexception,respage;
    @Override
    protected void processRequest() throws Exception {

        String module = request.getParameter("module");
        String moduleStr = "manage";
        if (module != null && module.trim().length() > 0) {
            if ("submit".equals(module)) {
                request.setAttribute("module", "submit");
                moduleStr = "submit";
            } else if ("admin".equals(module)) {
                request.setAttribute("module", "admin");
                moduleStr = "admin";
            } else {
                request.setAttribute("module", "manage");
            }
        }
        // << tbh 02/2010 filter out the entire module parameter to catch injections

        // BWP 3098>> close the info side panel and show icons
        request.setAttribute("closeInfoShowIcons", true);
        // <<
        // Determine whether to limit the displayed DN's to a certain DN type
        int resolutionStatus = 0;
        try {
            resolutionStatus = Integer.parseInt(request.getParameter("resolutionStatus"));
        } catch (NumberFormatException nfe) {
            // Show all DN's
            resolutionStatus = -1;
        }
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

        DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();
        // Generate a summary of how we are filtering;
        Map<String, List<String>> filterSummary = discNoteUtil.generateFilterSummary(discNoteType, resolutionStatusIds);

        if (!filterSummary.isEmpty()) {
            request.setAttribute(FILTER_SUMMARY, filterSummary);
        }
        locale = LocaleResolver.getLocale(request);

        StudyBean sbean = (StudyBean) session.getAttribute("study");
        //List<DiscrepancyNoteBean> allDiscNotes = discNoteUtil.getThreadedDNotesForStudy(sbean, resolutionStatusIds, sm.getDataSource(), discNoteType, true);

        //Map stats = discNoteUtil.generateDiscNoteSummary(allDiscNotes);
        Map stats = discNoteUtil.generateDiscNoteSummaryRefactored(sm.getDataSource(), currentStudy, resolutionStatusIds, discNoteType);
        request.setAttribute("summaryMap", stats);
        Set mapKeys = stats.keySet();
        request.setAttribute("mapKeys", mapKeys);

        // < resword =
        // ResourceBundle.getBundle("org.researchedc.i18n.words",locale);

        IStudyDAO studyDAO = this.studyDao;
        IStudySubjectDAO sdao = this.studySubjectDao;
        IStudyEventDAO sedao = this.studyEventDao;
        IStudyEventDefinitionDAO seddao = this.studyEventDefinitionDao;
        SubjectGroupMapDAO sgmdao = this.subjectGroupMapDao;
        StudyGroupClassDao sgcdao = this.studyGroupClassDao;
        StudyGroupDao sgdao = this.studyGroupDao;
        IStudySubjectDAO ssdao = this.studySubjectDao;
        EventCRFDao edao = this.eventCrfDao;
        EventDefinitionCRFDao eddao = this.eventDefinitionCrfDao;
        ISubjectDAO subdao = this.subjectDao;
        IDiscrepancyNoteDAO dnDAO = this.discrepancyNoteDao;

        ListDiscNotesSubjectTableFactory factory = new ListDiscNotesSubjectTableFactory(ResourceBundleProvider.getTermsBundle(locale));
        factory.setStudyEventDefinitionDao(seddao);
        factory.setSubjectDAO(subdao);
        factory.setStudySubjectDAO(sdao);
        factory.setStudyEventDAO(sedao);
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(sgcdao);
        factory.setSubjectGroupMapDAO(sgmdao);
        factory.setStudyDAO(studyDAO);
        factory.setCurrentRole(currentRole);
        factory.setCurrentUser(ub);
        factory.setEventCRFDAO(edao);
        factory.setEventDefintionCRFDAO(eddao);
        factory.setStudyGroupDAO(sgdao);
        factory.setDiscrepancyNoteDAO(dnDAO);

        factory.setModule(moduleStr);
        factory.setDiscNoteType(discNoteType);
        // factory.setStudyHasDiscNotes(allDiscNotes != null &&
        // !allDiscNotes.isEmpty());
        factory.setResolutionStatus(resolutionStatus);
        factory.setResolutionStatusIds(resolutionStatusIds);
        factory.setResword(ResourceBundleProvider.getWordsBundle(locale));
        String listDiscNotesHtml = factory.createTable(request, response).render();
        request.setAttribute("listDiscNotesHtml", listDiscNotesHtml);

        forwardPage(getJSP());
    }

    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

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
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    protected Page getJSP() {
        return Page.LIST_SUBJECT_DISC_NOTE;
    }

    protected String getBaseURL() {
        return "ListDiscNotesSubjectServlet";
    }

}
