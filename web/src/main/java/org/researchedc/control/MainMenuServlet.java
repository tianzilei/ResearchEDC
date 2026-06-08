/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.dao.spi.SubjectGroupMapDao;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.service.StudyParameterValueBean;
import org.researchedc.control.admin.EventStatusStatisticsTableFactory;
import org.researchedc.control.admin.SiteStatisticsTableFactory;
import org.researchedc.control.admin.StudyStatisticsTableFactory;
import org.researchedc.control.admin.StudySubjectStatusStatisticsTableFactory;
import org.researchedc.control.core.SecureController;
import org.researchedc.control.form.FormProcessor;
import org.researchedc.control.submit.ListStudySubjectTableFactory;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.dao.spi.StudyGroupDao;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;
import org.researchedc.web.SQLInitServlet;
import org.researchedc.web.table.sdv.SDVUtil;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * The main controller servlet for all the work behind study sites for
 * OpenClinica.
 *
 * @author jxu
 *
 */
public class MainMenuServlet extends SecureController {

    //Shaoyu Su
    Locale locale;
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
    private IDiscrepancyNoteDAO discrepancyNoteDAO;
    private IStudyParameterValueDAO studyParameterValueDAO;

    // < ResourceBundle respage;

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);
        // < respage =
        // ResourceBundle.getBundle("org.researchedc.i18n.page_messages",locale);
    }

    @Override
    public void processRequest() throws Exception {

    	FormProcessor fp = new FormProcessor(request);
        ub.incNumVisitsToMainMenu();
        session.setAttribute(USER_BEAN_NAME, ub);
        request.setAttribute("iconInfoShown", true);
        request.setAttribute("closeInfoShowIcons", false);

        if (ub == null || ub.getId() == 0) {// in case database connection is
            // broken
            forwardPage(Page.MENU, false);
            return;
        }

        IStudyDAO sdao = this.studyDao;
        ArrayList studies = null;

        long pwdExpireDay = new Long(SQLInitServlet.getField("passwd_expiration_time")).longValue();
        Date lastPwdChangeDate = ub.getPasswdTimestamp();

        // a flag tells whether users are required to change pwd upon the first
        // time log in or pwd expired
        int pwdChangeRequired = Integer.valueOf(SQLInitServlet.getField("change_passwd_required")).intValue();
        // update last visit date to current date
        IUserAccountDAO udao = this.userAccountDao;
        UserAccountBean ub1 = (UserAccountBean) udao.findByPK(ub.getId());
        ub1.setLastVisitDate(new Date(System.currentTimeMillis()));
        // have to actually set the above to a timestamp? tbh
        ub1.setOwner(ub1);
        ub1.setUpdater(ub1);
        udao.update(ub1);

        // Use study Id in JSPs
        request.setAttribute("studyId", currentStudy.getId());
        // Event Definition list and Group Class list for add suybject window.
        request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
        request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
    if (ub.isLdapUser()) {
            // "Forge" a password change date for LDAP user
            lastPwdChangeDate = new Date();
        }
System.out.println("is ub a ldapuser??"+ub.isLdapUser());


        //@pgawade 18-Sep-2012: fix for issue #14506 (https://issuetracker.openclinica.com/view.php?id=14506#c58197)
        if( (lastPwdChangeDate != null) || ((lastPwdChangeDate == null) && (pwdChangeRequired == 0))) {// not a new user
            if(lastPwdChangeDate != null){

	        	Calendar cal = Calendar.getInstance();

	            // compute difference between current date and lastPwdChangeDate
	            long difference = Math.abs(cal.getTime().getTime() - lastPwdChangeDate.getTime());
	            long days = difference / (1000 * 60 * 60 * 24);
	            session.setAttribute("passwordExpired", "no");

	            if (!ub.isLdapUser() && pwdExpireDay > 0 && days >= pwdExpireDay) {// password expired, need to be changed
			System.out.println("here");
			studies = (ArrayList) sdao.findAllByUser(ub.getName());
	                request.setAttribute("studies", studies);
	                session.setAttribute("userBean1", ub);
	                addPageMessage(respage.getString("password_expired"));
	                // YW 06-25-2007 << add the feature that if password is expired,
	                // have to go through /ResetPassword page
	                session.setAttribute("passwordExpired", "yes");
                if (pwdChangeRequired == 1) {
                    request.setAttribute("mustChangePass", "yes");
                    addPageMessage(respage.getString("your_password_has_expired_must_change"));
                } else {
                    request.setAttribute("mustChangePass", "no");
                    addPageMessage(respage.getString("password_expired") + " " + respage.getString("if_you_do_not_want_change_leave_blank"));
                }
                // Password change enforcement retired — SPA handles password changes via /app/profile
                // YW >>
	            }
            }
//            else {

                if (ub.getNumVisitsToMainMenu() <= 1) {
                    if (ub.getLastVisitDate() != null) {
                        addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName() + ". "
                            + respage.getString("last_logged") + " " + local_df.format(ub.getLastVisitDate()) + ". ");
                    } else {
                        addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName() + ". ");
                    }

                    if (currentStudy.getStatus().isLocked()) {
                        addPageMessage(respage.getString("current_study_locked"));
                    } else if (currentStudy.getStatus().isFrozen()) {
                        addPageMessage(respage.getString("current_study_frozen"));
                    }
                }

                ////Integer assignedDiscrepancies = getDiscrepancyNoteDAO().countAllItemDataByStudyAndUser(currentStudy, ub);
                // when change study will also call the same method, so the logic is consistent
                Integer assignedDiscrepancies = getDiscrepancyNoteDAO().getViewNotesCountWithFilter(" AND dn.assigned_user_id ="
                  + ub.getId() + " AND (dn.resolution_status_id=1 OR dn.resolution_status_id=2 OR dn.resolution_status_id=3)", currentStudy);
                //Yufang code added by Jamuna, to optimize the query on MainMenu

                request.setAttribute("assignedDiscrepancies", assignedDiscrepancies == null ? 0 : assignedDiscrepancies);

                int parentStudyId = currentStudy.getParentStudyId()>0?currentStudy.getParentStudyId():currentStudy.getId();
                IStudyParameterValueDAO spvdao = this.studyParameterValueDao;
                StudyParameterValueBean parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "subjectIdGeneration");
                currentStudy.getStudyParameterConfig().setSubjectIdGeneration(parentSPV.getValue());
                String idSetting = parentSPV.getValue();
                if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
                    //Shaoyu Su
                    //int nextLabel = this.getStudySubjectDAO().findTheGreatestLabel() + 1;
                    //request.setAttribute("label", new Integer(nextLabel).toString());
                    request.setAttribute("label", resword.getString("id_generated_Save_Add"));
                    //@pgawade 27-June-2012 fix for issue 13477: set label to "ID will be generated on Save or Add" in case of auto generated subject id
                    fp.addPresetValue("label", resword.getString("id_generated_Save_Add"));
                }
                setPresetValues(fp.getPresetValues());

                if (currentRole.isInvestigator() || currentRole.isResearchAssistant() || currentRole.isResearchAssistant2()) {
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
//            }

        } else {// a new user's first log in
            studies = (ArrayList) sdao.findAllByUser(ub.getName());
            request.setAttribute("studies", studies);
            session.setAttribute("userBean1", ub);
//            addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName() + ". " + respage.getString("password_set"));
//                + "<a href=\"UpdateProfile\">" + respage.getString("user_profile") + " </a>");

            if (pwdChangeRequired == 1) {
            } else {
                forwardPage(Page.MENU);
            }
        }

    }

    private void setupSubjectSDVTable() {

        request.setAttribute("studyId", currentStudy.getId());
        request.setAttribute("showMoreLink", "true");
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


    public IStudyParameterValueDAO getStudyParameterValueDAO() {
     studyParameterValueDAO = this.studyParameterValueDAO == null ? this.studyParameterValueDao : studyParameterValueDAO;
		return studyParameterValueDAO;
	}

	public void setStudyParameterValueDAO(IStudyParameterValueDAO studyParameterValueDAO) {
		studyParameterValueDAO = studyParameterValueDAO;
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

    public StudyGroupClassDao getStudyGroupClassDAO() {
        studyGroupClassDAO = this.studyGroupClassDAO == null ? this.studyGroupClassDao : studyGroupClassDAO;
        return studyGroupClassDAO;
    }

    public SubjectGroupMapDao getSubjectGroupMapDAO() {
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

    public StudyGroupDao getStudyGroupDAO() {
        studyGroupDAO = this.studyGroupDAO == null ? this.studyGroupDao : studyGroupDAO;
        return studyGroupDAO;
    }

    public IDiscrepancyNoteDAO getDiscrepancyNoteDAO() {
        discrepancyNoteDAO = discrepancyNoteDAO == null ? this.discrepancyNoteDao : discrepancyNoteDAO;
        return discrepancyNoteDAO;
    }

    public SDVUtil getSDVUtil() {
        return (SDVUtil) SpringServletAccess.getApplicationContext(context).getBean("sdvUtil");
    }

}
