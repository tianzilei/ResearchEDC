/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.managestudy;

import org.researchedc.dao.submit.SubjectGroupMapDAO;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.extract.DatasetBean;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.bean.managestudy.StudyGroupBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.SubjectGroupMapBean;
import org.researchedc.control.core.SecureController;
import org.researchedc.core.form.StringUtil;
import org.researchedc.dao.spi.DatasetDao;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.StudyGroupDao;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.view.Page;
import org.researchedc.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.researchedc.dao.spi.IItemDataDAO;

/**
 * @author jxu
 * 
 *         Removes a site from a study
 */
public class RemoveSiteServlet extends SecureController {

    
    @Autowired
    private DatasetDao datasetDao;
    @Autowired
    private EventDefinitionCRFDao eventDefinitionCrfDao;
    @Autowired
    private SubjectGroupMapDAO subjectGroupMapDao;
    @Autowired
    private IUserAccountDAO userAccountDao;

/**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.SITE_LIST_SERVLET, respage.getString("current_study_locked"));
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.SITE_LIST_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        IStudyDAO sdao = this.studyDao;
        String idString = request.getParameter("id");
        logger.info("site id:" + idString);

        int siteId = Integer.valueOf(idString.trim()).intValue();
        StudyBean study = (StudyBean) sdao.findByPK(siteId);
        if (currentStudy.getId() != study.getParentStudyId()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                    + " " + respage.getString("change_active_study_or_contact"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }

        // find all user and roles
        IUserAccountDAO udao = this.userAccountDao;
        ArrayList userRoles = udao.findAllByStudyId(siteId);

        // find all subjects
        IStudySubjectDAO ssdao = this.studySubjectDao;
        ArrayList subjects = ssdao.findAllByStudy(study);

        // find all events
        IStudyEventDefinitionDAO sefdao = this.studyEventDefinitionDao;
        ArrayList definitions = sefdao.findAllByStudy(study);

        String action = request.getParameter("action");
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_site_to_remove"));
            forwardPage(Page.SITE_LIST_SERVLET);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                request.setAttribute("siteToRemove", study);

                request.setAttribute("userRolesToRemove", userRoles);

                request.setAttribute("subjectsToRemove", subjects);

                forwardPage(Page.REMOVE_SITE);
            } else {
                logger.info("submit to remove the site");
                // change all statuses to unavailable
                IStudyDAO studao = this.studyDao;
                study.setOldStatus(study.getStatus());
                study.setStatus(Status.DELETED);
                study.setUpdater(ub);
                study.setUpdatedDate(new Date());
                studao.update(study);

                // remove all users and roles
                for (int i = 0; i < userRoles.size(); i++) {
                    StudyUserRoleBean role = (StudyUserRoleBean) userRoles.get(i);
                    if (!role.getStatus().equals(Status.DELETED)) {
                        role.setStatus(Status.AUTO_DELETED);
                        role.setUpdater(ub);
                        role.setUpdatedDate(new Date());
                        // YW << So study_user_role table status_id field can be
                        // updated
                        udao.updateStudyUserRole(role, role.getUserName());
                    }
                    // YW 06-18-2007 >>
                }

                // YW << bug fix that current active study has been deleted
                if (study.getId() == currentStudy.getId()) {
                    currentStudy.setStatus(Status.DELETED);
                    // currentRole.setRole(Role.INVALID);
                    currentRole.setStatus(Status.DELETED);
                }
                // YW 06-18-2007 >>

                // remove all subjects
                for (int i = 0; i < subjects.size(); i++) {
                    StudySubjectBean subject = (StudySubjectBean) subjects.get(i);

                }

                // remove all study_group
                StudyGroupDao sgdao = this.studyGroupDao;
                SubjectGroupMapDAO sgmdao = this.subjectGroupMapDao;
                ArrayList groups = sgdao.findAllByStudy(study);
                for (int i = 0; i < groups.size(); i++) {
                    StudyGroupBean group = (StudyGroupBean) groups.get(i);
                    if (!group.getStatus().equals(Status.DELETED)) {
                        group.setStatus(Status.AUTO_DELETED);
                        group.setUpdater(ub);
                        group.setUpdatedDate(new Date());
                        sgdao.update(group);
                        // all subject_group_map
                        ArrayList subjectGroupMaps = sgmdao.findAllByStudyGroupId(group.getId());
                        for (int j = 0; j < subjectGroupMaps.size(); j++) {
                            SubjectGroupMapBean sgMap = (SubjectGroupMapBean) subjectGroupMaps.get(j);
                            if (!sgMap.getStatus().equals(Status.DELETED)) {
                                sgMap.setStatus(Status.AUTO_DELETED);
                                sgMap.setUpdater(ub);
                                sgMap.setUpdatedDate(new Date());
                                sgmdao.update(sgMap);
                            }
                        }
                    }
                }

                // remove all events
                EventDefinitionCRFDao edcdao = this.eventDefinitionCrfDao;
                IStudyEventDAO sedao = this.studyEventDao;
                for (int i = 0; i < subjects.size(); i++) {
                    StudySubjectBean subject = (StudySubjectBean) subjects.get(i);

                    if (!subject.getStatus().equals(Status.DELETED)) {
                        subject.setStatus(Status.AUTO_DELETED);
                        subject.setUpdater(ub);
                        subject.setUpdatedDate(new Date());
                        ssdao.update(subject);

                        ArrayList events = sedao.findAllByStudySubject(subject);
                        EventCRFDao ecdao = this.eventCrfDao;

                        for (int j = 0; j < events.size(); j++) {
                            StudyEventBean event = (StudyEventBean) events.get(j);
                            if (!event.getStatus().equals(Status.DELETED)) {
                                event.setStatus(Status.AUTO_DELETED);
                                event.setUpdater(ub);
                                event.setUpdatedDate(new Date());
                                sedao.update(event);

                                ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

                                IItemDataDAO iddao = this.itemDataDao;
                                for (int k = 0; k < eventCRFs.size(); k++) {
                                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                                    if (!eventCRF.getStatus().equals(Status.DELETED)) {
                                        eventCRF.setOldStatus(eventCRF.getStatus());
                                        eventCRF.setStatus(Status.AUTO_DELETED);
                                        eventCRF.setUpdater(ub);
                                        eventCRF.setUpdatedDate(new Date());
                                        ecdao.update(eventCRF);

                                        ArrayList itemDatas = iddao.findAllByEventCRFId(eventCRF.getId());
                                        for (int a = 0; a < itemDatas.size(); a++) {
                                            ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                                            if (!item.getStatus().equals(Status.DELETED)) {
                                                item.setOldStatus(item.getStatus());
                                                item.setStatus(Status.AUTO_DELETED);
                                                item.setUpdater(ub);
                                                item.setUpdatedDate(new Date());
                                                iddao.update(item);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }// for subjects

                DatasetDao datadao = this.datasetDao;
                ArrayList dataset = datadao.findAllByStudyId(study.getId());
                for (int i = 0; i < dataset.size(); i++) {
                    DatasetBean data = (DatasetBean) dataset.get(i);
                    if (!data.getStatus().equals(Status.DELETED)) {
                        data.setStatus(Status.AUTO_DELETED);
                        data.setUpdater(ub);
                        data.setUpdatedDate(new Date());
                        datadao.update(data);
                    }
                }

                addPageMessage(respage.getString("this_site_has_been_removed_succesfully"));

                String fromListSite = (String) session.getAttribute("fromListSite");
                if (fromListSite != null && fromListSite.equals("yes") && currentRole.getRole().equals(Role.STUDYDIRECTOR)) {
                    session.removeAttribute("fromListSite");
                    forwardPage(Page.SITE_LIST_SERVLET);
                } else {
                    session.removeAttribute("fromListSite");
                    if (currentRole.getRole().equals(Role.ADMIN)) {
                        forwardPage(Page.STUDY_LIST_SERVLET);
                    } else {
                        forwardPage(Page.SITE_LIST_SERVLET);
                    }
                }

            }
        }

    }

}
