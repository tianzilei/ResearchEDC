package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.domain.user.UserAccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface IUserAccountDAO {
    EntityBean findByPK(int ID);
    EntityBean findByPK(int ID, boolean findOwner);
    EntityBean findByUserName(String name);
    EntityBean findByAccessCode(String name);
    EntityBean findByApiKey(String name);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    void delete(UserAccountBean u);
    void deleteTestOnly(String name);
    void restore(UserAccountBean u);
    void lockUser(Integer id);
    void updateLockCounter(Integer id, Integer newCounterNumber);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByLimit(boolean hasLimit);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    Collection findAllByRole(String role);
    Collection findAllByRole(String role1, String role2);
    Collection findAllParticipantsByStudyOid(String studyOid);
    ArrayList findAllByStudyId(int studyId);
    ArrayList findAllUsersByStudyIdAndLimit(int studyId, boolean isLimited);
    ArrayList findAllUsersByStudy(int studyId);
    ArrayList findAllAssignedUsersByStudy(int studyId);
    ArrayList findAllUsersByStudyOrSite(int studyId, int parentStudyId, int studySubjectId);
    Collection findAllRolesByUserName(String userName);
    ArrayList findStudyByUser(String userName, ArrayList allStudies);
    UserAccountBean findStudyUserRole(UserAccountBean user, StudyUserRoleBean studyRole);
    StudyUserRoleBean findRoleByUserNameAndStudyId(String userName, int studyId);
    int findRoleCountByUserNameAndStudyId(String userName, int studyId, int childStudyId);
    StudyUserRoleBean createStudyUserRole(UserAccountBean user, StudyUserRoleBean studyRole);
    StudyUserRoleBean updateStudyUserRole(StudyUserRoleBean s, String userName);
    Collection findPrivilegesByRole(int roleId);
    Collection findPrivilegesByRoleName(String roleName);
    void setSysAdminRole(UserAccountBean uab, boolean creating);
    Object getEntityFromHashMap(HashMap hm);
    Object getEntityFromHashMap(HashMap hm, boolean findOwner);
    StudyUserRoleBean getRoleFromHashMap(HashMap hm);
    boolean isQuerySuccessful();
    default UserAccount findById(Integer id) { throw new UnsupportedOperationException(); }
}
