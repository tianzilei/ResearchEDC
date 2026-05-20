/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.service.managestudy;

import java.util.List;

import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.DisplayStudyEventBean;
import org.researchedc.bean.managestudy.StudySubjectBean;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public interface StudySubjectService {

    /**
     *
     * @param studySubject
     * @param userAccount
     * @param currentRole
     * @return
     */
    List<DisplayStudyEventBean> getDisplayStudyEventsForStudySubject(StudySubjectBean studySubject,
            UserAccountBean userAccount, StudyUserRoleBean currentRole);

}
