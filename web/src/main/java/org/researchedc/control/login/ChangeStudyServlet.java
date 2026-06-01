/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.login;

import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.submit.SubjectGroupMapDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.service.StudyParameterValueBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.admin.EventStatusStatisticsTableFactory;
import org.researchedc.control.admin.SiteStatisticsTableFactory;
import org.researchedc.control.admin.StudyStatisticsTableFactory;
import org.researchedc.control.admin.StudySubjectStatusStatisticsTableFactory;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.form.Validator;
import org.researchedc.control.submit.ListStudySubjectTableFactory;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.service.StudyConfigService;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.dao.spi.StudyGroupDao;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.researchedc.web.table.sdv.SDVUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author jxu
 *
 * Processes the request of changing current study
 */
public class ChangeStudyServlet extends SecureController {

    Locale locale;

    @Autowired
    protected ISubjectDAO subjectDao;
    @Autowired
    protected SubjectGroupMapDAO subjectGroupMapDao;
    @Autowired
    protected EventDefinitionCRFDao eventDefinitionCrfDao;
    @Autowired
    protected IDiscrepancyNoteDAO discrepancyNoteDao;
    @Autowired
    protected IUserAccountDAO userAccountDao;

    // < ResourceBundlerestext;

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("org.researchedc.i18n.notes",locale);

    }

    @Override
    public void processRequest() throws Exception {

        String action = request.getParameter("action");// action sent by user
        ArrayList studies = userAccountDao.findStudyByUser(ub.getName(), (ArrayList) studyDao.findAll());
        request.setAttribute("siteRoleMap", Role.siteRoleMap);
        request.setAttribute("studyRoleMap", Role.studyRoleMap);
        if(request.getAttribute("label")!=null) {
            String label = (String) request.getAttribute("label");
            if(label.length()>0) {
                request.setAttribute("label", label);
            }
        }

        ArrayList validStudies = new ArrayList();
        for (int i = 0; i < studies.size(); i++) {
            StudyUserRoleBean sr = (StudyUserRoleBean) studies.get(i);
            StudyBean study = (StudyBean) studyDao.findByPK(sr.getStudyId());
            if (study != null && study.getStatus().equals(Status.PENDING)) {
                sr.setStatus(study.getStatus());
            }
            if (study != null && study.getStatus().equals(Status.DELETED)) {
                sr.setStatus(study.getStatus());
            }
            validStudies.add(sr);
        }


        if (StringUtil.isBlank(action)) {
            request.setAttribute("studies", validStudies);

            forwardPage(Page.CHANGE_STUDY);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                logger.info("confirm");
                
                // pull out/update the roles and privs here               
                ArrayList userRoleBeans = (ArrayList) userAccountDao.findAllRolesByUserName(ub.getName());
                ub.setRoles(userRoleBeans);
               
                confirmChangeStudy(studies);

            } else if ("submit".equalsIgnoreCase(action)) {
                logger.info("submit");
                changeStudy();
            }
        }

    }

    private void confirmChangeStudy(ArrayList studies) throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);
        v.addValidation("studyId", Validator.IS_AN_INTEGER);

        errors = v.validate();

        if (!errors.isEmpty()) {
            request.setAttribute("studies", studies);
            forwardPage(Page.CHANGE_STUDY);
        } else {
            int studyId = fp.getInt("studyId");
            logger.info("new study id:" + studyId);
            for (int i = 0; i < studies.size(); i++) {
                StudyUserRoleBean studyWithRole = (StudyUserRoleBean) studies.get(i);
                if (studyWithRole.getStudyId() == studyId) {
                    request.setAttribute("studyId", Integer.valueOf(studyId));
                    session.setAttribute("studyWithRole", studyWithRole);
                    request.setAttribute("currentStudy", currentStudy);
                    forwardPage(Page.CHANGE_STUDY_CONFIRM);
                    return;

                }
            }
            addPageMessage(restext.getString("no_study_selected"));

            forwardPage(Page.CHANGE_STUDY);
        }
    }

    private void changeStudy() throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);
        int studyId = fp.getInt("studyId");
        int prevStudyId = currentStudy.getId();

        StudyBean current = (StudyBean) studyDao.findByPK(studyId);

        // reset study parameters -jxu 02/09/2007
        ArrayList studyParameters = studyParameterValueDao.findParamConfigByStudy(current);
        current.setStudyParameters(studyParameters);
        int parentStudyId = currentStudy.getParentStudyId()>0?currentStudy.getParentStudyId():currentStudy.getId();
        StudyParameterValueBean parentSPV = studyParameterValueDao.findByHandleAndStudy(parentStudyId, "subjectIdGeneration");
        current.getStudyParameterConfig().setSubjectIdGeneration(parentSPV.getValue());
        String idSetting = current.getStudyParameterConfig().getSubjectIdGeneration();
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
            int nextLabel = this.getStudySubjectDAO().findTheGreatestLabel() + 1;
            request.setAttribute("label", Integer.valueOf(nextLabel).toString());
        }

        if (current.getParentStudyId() <= 0) {// top study
            studyConfigService.setParametersForStudy(current);

        } else {
            // YW <<
            if (current.getParentStudyId() > 0) {
                current.setParentStudyName(((StudyBean) studyDao.findByPK(current.getParentStudyId())).getName());

            }
            // YW 06-12-2007>>
            studyConfigService.setParametersForSite(current);

        }
        if (current.getStatus().equals(Status.DELETED) || current.getStatus().equals(Status.AUTO_DELETED)) {
            session.removeAttribute("studyWithRole");
            addPageMessage(restext.getString("study_choosed_removed_restore_first"));
        } else {
            session.setAttribute("study", current);
            currentStudy = current;
            // change user's active study id
            ub.setActiveStudyId(current.getId());
            ub.setUpdater(ub);
            ub.setUpdatedDate(new java.util.Date());
            userAccountDao.update(ub);

            if (current.getParentStudyId() > 0) {
                /*
                 * The Role decription will be set depending on whether the user
                 * logged in at study lever or site level. issue-2422
                 */
                List roles = Role.toArrayList();
                for (Iterator it = roles.iterator(); it.hasNext();) {
                    Role role = (Role) it.next();
                    switch (role.getId()) {
                    case 2:
                        role.setDescription("site_Study_Coordinator");
                        break;
                    case 3:
                        role.setDescription("site_Study_Director");
                        break;
                    case 4:
                        role.setDescription("site_investigator");
                        break;
                    case 5:
                        role.setDescription("site_Data_Entry_Person");
                        break;
                    case 6:
                        role.setDescription("site_monitor");
                        break;
                    case 7:
                        role.setDescription("site_Data_Entry_Person2");
                        break;
                    default:
                        // logger.info("No role matched when setting role description");
                    }
                }
            } else {
                /*
                 * If the current study is a site, we will change the role
                 * description. issue-2422
                 */
                List roles = Role.toArrayList();
                for (Iterator it = roles.iterator(); it.hasNext();) {
                    Role role = (Role) it.next();
                    switch (role.getId()) {
                    case 2:
                        role.setDescription("Study_Coordinator");
                        break;
                    case 3:
                        role.setDescription("Study_Director");
                        break;
                    case 4:
                        role.setDescription("investigator");
                        break;
                    case 5:
                        role.setDescription("Data_Entry_Person");
                        break;
                    case 6:
                        role.setDescription("monitor");
                        break;
                    default:
                        // logger.info("No role matched when setting role description");
                    }
                }
            }

            currentRole = (StudyUserRoleBean) session.getAttribute("studyWithRole");
            session.setAttribute("userRole", currentRole);
            session.removeAttribute("studyWithRole");
            addPageMessage(restext.getString("current_study_changed_succesfully"));
        }
        ub.incNumVisitsToMainMenu();
        // YW 2-18-2008, if study has been really changed <<
        if (prevStudyId != studyId) {
            session.removeAttribute("eventsForCreateDataset");
            session.setAttribute("tableFacadeRestore", "false");
        }
        request.setAttribute("studyJustChanged", "yes");
        // YW >>

        //Integer assignedDiscrepancies = getDiscrepancyNoteDAO().countAllItemDataByStudyAndUser(currentStudy, ub);
        Integer assignedDiscrepancies = getDiscrepancyNoteDAO().getViewNotesCountWithFilter(" AND dn.assigned_user_id ="
                + ub.getId() + " AND (dn.resolution_status_id=1 OR dn.resolution_status_id=2 OR dn.resolution_status_id=3)", currentStudy);
        request.setAttribute("assignedDiscrepancies", assignedDiscrepancies == null ? 0 : assignedDiscrepancies);

        if (currentRole.isInvestigator() || currentRole.isResearchAssistant()|| currentRole.isResearchAssistant2()) {
            setupListStudySubjectTable();
        }
        if (currentRole.isMonitor()) {
            setupSubjectSDVTable();
        } else if (currentRole.isCoordinator() || currentRole.isDirector()) {
            if (currentStudy.getStatus().isPending()) {
                response.sendRedirect(request.getContextPath() + Page.MANAGE_STUDY_MODULE.getFileName());
                return;
            }
            setupStudySiteStatisticsTable();
            setupSubjectEventStatusStatisticsTable();
            setupStudySubjectStatusStatisticsTable();
            if (currentStudy.getParentStudyId() == 0) {
                setupStudyStatisticsTable();
            }

        }

        forwardPage(Page.MENU);

    }

    private void setupSubjectSDVTable() {

        request.setAttribute("studyId", currentStudy.getId());
        String sdvMatrix = getSDVUtil().renderEventCRFTableWithLimit(request, currentStudy.getId(), "");
        request.setAttribute("sdvMatrix", sdvMatrix);
    }

    private void setupStudySubjectStatusStatisticsTable() {

        StudySubjectStatusStatisticsTableFactory factory = new StudySubjectStatusStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyDao(getStudyDAO());
        String studySubjectStatusStatistics = factory.createTable(request, response).render();
        request.setAttribute("studySubjectStatusStatistics", studySubjectStatusStatistics);
    }

    private void setupSubjectEventStatusStatisticsTable() {

        EventStatusStatisticsTableFactory factory = new EventStatusStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyEventDao(getStudyEventDAO());
        factory.setStudyDao(getStudyDAO());
        String subjectEventStatusStatistics = factory.createTable(request, response).render();
        request.setAttribute("subjectEventStatusStatistics", subjectEventStatusStatistics);
    }

    private void setupStudySiteStatisticsTable() {

        SiteStatisticsTableFactory factory = new SiteStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyDao(getStudyDAO());
        String studySiteStatistics = factory.createTable(request, response).render();
        request.setAttribute("studySiteStatistics", studySiteStatistics);

    }

    private void setupStudyStatisticsTable() {

        StudyStatisticsTableFactory factory = new StudyStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyDao(getStudyDAO());
        String studyStatistics = factory.createTable(request, response).render();
        request.setAttribute("studyStatistics", studyStatistics);

    }

    private void setupListStudySubjectTable() {

        ListStudySubjectTableFactory factory = new ListStudySubjectTableFactory(true);
        factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
        factory.setSubjectDAO(getSubjectDAO());
        factory.setStudySubjectDAO(getStudySubjectDAO());
        factory.setStudyEventDAO(getStudyEventDAO());
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(getStudyGroupClassDAO());
        factory.setSubjectGroupMapDAO(getSubjectGroupMapDAO());
        factory.setStudyDAO(getStudyDAO());
        factory.setCurrentRole(currentRole);
        factory.setCurrentUser(ub);
        factory.setEventCRFDAO(getEventCRFDAO());
        factory.setEventDefintionCRFDAO(getEventDefinitionCRFDAO());
        factory.setStudyGroupDAO(getStudyGroupDAO());
        factory.setStudyParameterValueDAO(getStudyParameterValueDAO());
        String findSubjectsHtml = factory.createTable(request, response).render();
        request.setAttribute("findSubjectsHtml", findSubjectsHtml);
    }

    public IStudyEventDefinitionDAO getStudyEventDefinitionDao() {
        return studyEventDefinitionDao;
    }

    public ISubjectDAO getSubjectDAO() {
        return subjectDao;
    }

    public IStudySubjectDAO getStudySubjectDAO() {
        return studySubjectDao;
    }

    public StudyGroupClassDao getStudyGroupClassDAO() {
        return studyGroupClassDao;
    }

    public SubjectGroupMapDAO getSubjectGroupMapDAO() {
        return subjectGroupMapDao;
    }

    public IStudyEventDAO getStudyEventDAO() {
        return studyEventDao;
    }

    public IStudyDAO getStudyDAO() {
        return studyDao;
    }

    public EventCRFDao getEventCRFDAO() {
        return eventCrfDao;
    }

    public EventDefinitionCRFDao getEventDefinitionCRFDAO() {
        return eventDefinitionCrfDao;
    }

    public StudyGroupDao getStudyGroupDAO() {
        return studyGroupDao;
    }

    public IDiscrepancyNoteDAO getDiscrepancyNoteDAO() {
        return discrepancyNoteDao;
    }

    public SDVUtil getSDVUtil() {
        return (SDVUtil) SpringServletAccess.getApplicationContext(context).getBean("sdvUtil");
    }

    public IStudyParameterValueDAO getStudyParameterValueDAO() {
        return studyParameterValueDao;
    }

    public void setStudyParameterValueDAO(StudyParameterValueDAO studyParameterValueDAO) {
        this.studyParameterValueDao = studyParameterValueDAO;
    }

    
}
