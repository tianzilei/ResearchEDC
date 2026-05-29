/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2006 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.Utils;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.DisplayItemBean;
import org.researchedc.bean.submit.DisplayItemGroupBean;
import org.researchedc.bean.submit.DisplaySectionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.control.form.DiscrepancyValidator;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.submit.DataEntryServlet;
import org.researchedc.control.submit.SubmitDataServlet;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.ItemGroupDAO;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.view.display.DisplaySectionBeanHandler;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

/**
 * @author Krikor Krumlian 10/26/2006
 *
 *
 * View a CRF version section data entry
 */
public class PrintDataEntryServlet extends DataEntryServlet {

    @Autowired
    protected ICrfDAO crfDao;

    @Autowired
    protected IStudyEventDefinitionDAO studyEventDefinitionDao;

    @Autowired
    protected IStudySubjectDAO studySubjectDao;

    @Autowired
    protected EventCRFDao eventCrfDao;

    @Autowired
    protected IStudyEventDAO studyEventDao;

    @Autowired
    protected IStudyDAO studyDao;

    
    @Autowired
    private CRFVersionDAO crfVersionDao;
    @Autowired
    private ItemGroupDAO itemGroupDao;
    @Autowired
    private SectionDAO sectionDao;
    @Autowired
    private ISubjectDAO subjectDao;

Locale locale;

    // < ResourceBundleresword, resworkflow, respage,resexception;

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed(HttpServletRequest request, HttpServletResponse response) throws InsufficientPermissionException {
        StudyUserRoleBean  currentRole = (StudyUserRoleBean) request.getSession().getAttribute("userRole");
        locale = LocaleResolver.getLocale(request);
        UserAccountBean ub =(UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
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

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"), request);
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        FormProcessor fp = new FormProcessor(request);
        boolean isSubmitted = false;
        int eventCRFId = fp.getInt("ecId");
        //JN:The following were the the global variables, moved as local.
        EventCRFBean ecb ;
        SectionDAO sdao = this.sectionDao;
       ArrayList<SectionBean> allSectionBeans = new ArrayList<SectionBean>();
        ArrayList sectionBeans = new ArrayList();
        String age = "";
        StudyBean currentStudy =    (StudyBean)  request.getSession().getAttribute("study");
        SectionBean sb = (SectionBean)request.getAttribute(SECTION_BEAN);
        // Whether IE6 or IE7 is involved
        String isIE = fp.getString("ie");
        if ("y".equalsIgnoreCase(isIE)) {
            request.setAttribute("isInternetExplorer", "true");
        }

        if (eventCRFId == 0) {
            ecb = new EventCRFBean();
            // super.ecb.setCRFVersionId(sb.getCRFVersionId());
        } else {
            EventCRFDao ecdao = this.eventCrfDao;
            ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);

            // Get all the SectionBeans attached to this ECB
            ArrayList sects = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());
            for (int i = 0; i < sects.size(); i++) {
                 sb = (SectionBean) sects.get(i);
//                super.sb = sb;
                int sectId = sb.getId();
                if (sectId > 0) {
                    allSectionBeans.add((SectionBean) sdao.findByPK(sectId));
                }
            }
            // This is the StudySubjectBean
            IStudySubjectDAO ssdao = this.studySubjectDao;
            StudySubjectBean sub = (StudySubjectBean) ssdao.findByPK(ecb.getStudySubjectId());
            // This is the SubjectBean
            ISubjectDAO subjectDao = this.subjectDao;
            int subjectId = sub.getSubjectId();
            int studyId = sub.getStudyId();
            SubjectBean subject = (SubjectBean) subjectDao.findByPK(subjectId);
            IStudyEventDAO sedao = this.studyEventDao;
            StudyEventBean se = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
            IStudyEventDefinitionDAO seddao = this.studyEventDefinitionDao;
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
            se.setStudyEventDefinition(sed);
            // Let us process the age
            if (currentStudy.getStudyParameterConfig().getCollectDob().equals("1")) {
                // YW 11-16-2007 enrollment-date is used for computing age
                age = Utils.getInstacne().processAge(sub.getEnrollmentDate(), subject.getDateOfBirth());
            }
            // Get the study then the parent study
            IStudyDAO studydao = this.studyDao;
            StudyBean study = (StudyBean) studydao.findByPK(studyId);

            if (study.getParentStudyId() > 0) {
                // this is a site,find parent
                StudyBean parentStudy = (StudyBean) studydao.findByPK(study.getParentStudyId());
                request.setAttribute("studyTitle", parentStudy.getName() + " - " + study.getName());
            } else {
                request.setAttribute("studyTitle", study.getName());
            }

            request.setAttribute("studySubject", sub);
            request.setAttribute("subject", subject);
            request.setAttribute("studyEvent", se);
            request.setAttribute("age", age);
            request.setAttribute(INPUT_EVENT_CRF,ecb);
            request.setAttribute(SECTION_BEAN,sb);
            request.setAttribute(ALL_SECTION_BEANS, allSectionBeans);
            // Get the section beans from super
            sectionBeans = super.getAllDisplayBeans(request);

        }

        // Find out whether the sections involve groups
        ItemGroupDAO itemGroupDao = this.itemGroupDao;
        // Find truely grouped tables, not groups with a name of 'Ungrouped'
        // CRF VERSION ID WILL BE 0 IF "ecId" IS NOT IN THE QUERYSTRING
        int crfVersionId = ecb.getCRFVersionId();
        List<ItemGroupBean> itemGroupBeans = itemGroupDao.findOnlyGroupsByCRFVersionID(crfVersionId);
        boolean sectionsHaveGroups = false;

        if (itemGroupBeans.size() > 0) {
            sectionsHaveGroups = true;
            // get a DisplaySectionBean for each section of the CRF, sort them,
            // then
            // dispatch the request to a print JSP. the constructor for this
            // handler takes
            // a boolean value depending on whether an event or data is involved
            // or not
            DisplaySectionBeanHandler handler = new DisplaySectionBeanHandler(true, getDataSource(), getServletContext());

            handler.setCrfVersionId(crfVersionId);
            handler.setEventCRFId(eventCRFId);
            List<DisplaySectionBean> displaySectionBeans = handler.getDisplaySectionBeans();

            CRFVersionDAO crfVersionDAO = this.crfVersionDao;
            ICrfDAO crfDao = this.crfDao;

            request.setAttribute("listOfDisplaySectionBeans", displaySectionBeans);
            // Make available the CRF names and versions for
            // the web page's header
            CRFVersionBean crfverBean = (CRFVersionBean) crfVersionDAO.findByPK(crfVersionId);
            request.setAttribute("crfVersionBean", crfverBean);
            CRFBean crfBean = crfDao.findByVersionId(crfVersionId);
            request.setAttribute("crfBean", crfBean);
            // Set an attribute signaling that an event and/or data is involved
            request.setAttribute("dataInvolved", "true");
        }
        request.setAttribute(BEAN_ANNOTATIONS, ecb.getAnnotations());
        request.setAttribute("EventCRFBean", ecb);
        // We do not need most of these attributes if groups are involved
        if (!sectionsHaveGroups) {
            request.setAttribute(INPUT_EVENT_CRF,ecb);
            request.setAttribute(SECTION_BEAN,sb);
            DisplaySectionBean dsb = super.getDisplayBean(false, false, request, isSubmitted);
            request.setAttribute("allSections", sectionBeans);
            request.setAttribute("displayAll", "1");
            request.setAttribute(BEAN_DISPLAY, dsb);

            request.setAttribute("sec", sb);

            forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PRINT, request, response);
        } else { // end if(! sectionsHaveGroups)
            forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PRINT_GROUPS, request, response);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.submit.DataEntryServlet#getBlankItemStatus()
     */
    @Override
    protected Status getBlankItemStatus() {
        return Status.AVAILABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.submit.DataEntryServlet#getNonBlankItemStatus()
     */
    @Override
    protected Status getNonBlankItemStatus(HttpServletRequest request) {
        EventDefinitionCRFBean edcb = (EventDefinitionCRFBean)request.getAttribute(EVENT_DEF_CRF_BEAN);
        return edcb.isDoubleEntry() ? Status.PENDING : Status.UNAVAILABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.submit.DataEntryServlet#getEventCRFAnnotations()
     */
    @Override
    protected String getEventCRFAnnotations(HttpServletRequest request) {
        //JN:The following were the the global variables, moved as local.
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);
        return ecb.getAnnotations();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.submit.DataEntryServlet#setEventCRFAnnotations(java.lang.String)
     */
    @Override
    protected void setEventCRFAnnotations(String annotations, HttpServletRequest request) {
        //JN:The following were the the global variables, moved as local.
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);
        ecb.setAnnotations(annotations);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.submit.DataEntryServlet#getJSPPage()
     */
    @Override
    protected Page getJSPPage() {
        return Page.VIEW_SECTION_DATA_ENTRY;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.submit.DataEntryServlet#getServletPage()
     */
    @Override
    protected String getServletPage(HttpServletRequest request) {
        return Page.VIEW_SECTION_DATA_ENTRY_SERVLET.getFileName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.submit.DataEntryServlet#validateInputOnFirstRound()
     */
    @Override
    protected boolean validateInputOnFirstRound() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.submit.DataEntryServlet#validateDisplayItemBean(org.researchedc.core.form.Validator,
     *      org.researchedc.bean.submit.DisplayItemBean)
     */
    @Override
    protected DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib, String inputName, HttpServletRequest request) {
        ItemBean ib = dib.getItem();
        org.researchedc.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();

        // note that this step sets us up both for
        // displaying the data on the form again, in the event of an error
        // and sending the data to the database, in the event of no error
        dib = loadFormValue(dib, request);

        // types TEL and ED are not supported yet
        if (rt.equals(org.researchedc.bean.core.ResponseType.TEXT) || rt.equals(org.researchedc.bean.core.ResponseType.TEXTAREA)) {
            dib = validateDisplayItemBeanText(v, dib, inputName, request);
        } else if (rt.equals(org.researchedc.bean.core.ResponseType.RADIO) || rt.equals(org.researchedc.bean.core.ResponseType.SELECT)) {
            dib = validateDisplayItemBeanSingleCV(v, dib, inputName);
        } else if (rt.equals(org.researchedc.bean.core.ResponseType.CHECKBOX) || rt.equals(org.researchedc.bean.core.ResponseType.SELECTMULTI)) {
            dib = validateDisplayItemBeanMultipleCV(v, dib, inputName);
        } else if (rt.equals(org.researchedc.bean.core.ResponseType.CALCULATION)
            || rt.equals(org.researchedc.bean.core.ResponseType.GROUP_CALCULATION)) {
            // for now, treat calculation like any other text input --
            // eventually this might need to be customized
            dib = validateDisplayItemBeanText(v, dib, inputName, request);
        }

        return dib;
    }

    @Override
    protected List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v, DisplayItemGroupBean digb, List<DisplayItemGroupBean> digbs,
            List<DisplayItemGroupBean> formGroups, HttpServletRequest request, HttpServletResponse response) {

        return formGroups;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.researchedc.control.submit.DataEntryServlet#loadDBValues()
     */
    @Override
    protected boolean shouldLoadDBValues(DisplayItemBean dib) {
        return true;
    }

    @Override
    protected boolean shouldRunRules() {
        return false;
    }

    @Override
    protected boolean isAdministrativeEditing() {
    	return false;
    }

    @Override
    protected boolean isAdminForcedReasonForChange(HttpServletRequest request) {
    	return false;
    }
}
