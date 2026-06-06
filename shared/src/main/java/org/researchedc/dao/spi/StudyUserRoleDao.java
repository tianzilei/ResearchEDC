package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.StudyUserRole;
import org.researchedc.domain.user.UserAccount;

import java.util.ArrayList;

public interface StudyUserRoleDao {

    ArrayList<StudyUserRole> findAllUserRolesByUserAccount(UserAccount userAccount, int studyId, int parentStudyId);

    StudyUserRole saveOrUpdate(StudyUserRole entity);
}
