package org.researchedc.module.identity.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.UserType;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.domain.user.UserAccount;
import org.researchedc.module.identity.entity.RoleEntity;
import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.identity.repository.RoleRepository;
import org.researchedc.module.identity.repository.UserAccountRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("userAccountDAO")
@Primary
@Transactional(readOnly = true)
public class UserAccountDaoAdapter implements IUserAccountDAO {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;

    public UserAccountDaoAdapter(UserAccountRepository userAccountRepository, RoleRepository roleRepository) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return userAccountRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(UserAccountBean::new);
    }

    @Override
    public EntityBean findByPK(int ID, boolean findOwner) {
        return findByPK(ID);
    }

    @Override
    public EntityBean findByUserName(String name) {
        return userAccountRepository.findByUserName(name)
                .map(this::toBean)
                .orElseGet(UserAccountBean::new);
    }

    @Override
    public EntityBean findByAccessCode(String name) {
        return new UserAccountBean();
    }

    @Override
    public EntityBean findByApiKey(String name) {
        return new UserAccountBean();
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        UserAccountBean bean = (UserAccountBean) eb;
        UserAccountEntity entity = new UserAccountEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(userAccountRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        UserAccountBean bean = (UserAccountBean) eb;
        UserAccountEntity entity = userAccountRepository.findById(bean.getId())
                .orElseGet(UserAccountEntity::new);
        entity.setUserId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(userAccountRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UserAccountBean u) {
        userAccountRepository.findById(u.getId()).ifPresent(entity -> {
            entity.setStatusId(Status.DELETED.getId());
            entity.setDateUpdated(LocalDateTime.now());
            userAccountRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public void deleteTestOnly(String name) {
        userAccountRepository.findByUserName(name).ifPresent(entity ->
                userAccountRepository.delete(entity));
    }

    @Override
    @Transactional
    public void restore(UserAccountBean u) {
        userAccountRepository.findById(u.getId()).ifPresent(entity -> {
            entity.setStatusId(Status.AVAILABLE.getId());
            entity.setDateUpdated(LocalDateTime.now());
            userAccountRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public void lockUser(Integer id) {
        userAccountRepository.findById(id).ifPresent(entity -> {
            entity.setAccountNonLocked(false);
            entity.setStatusId(Status.LOCKED.getId());
            entity.setDateUpdated(LocalDateTime.now());
            userAccountRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public void updateLockCounter(Integer id, Integer newCounterNumber) {
        userAccountRepository.findById(id).ifPresent(entity -> {
            entity.setDateUpdated(LocalDateTime.now());
            userAccountRepository.save(entity);
        });
    }

    @Override
    public Collection findAll() {
        return toBeans(userAccountRepository.findAll());
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByLimit(boolean hasLimit) {
        return findAll();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType,
                                          String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByRole(String role) {
        List<RoleEntity> roles = roleRepository.findByRoleName(role);
        Set<String> userNames = roles.stream()
                .map(RoleEntity::getUserName)
                .collect(Collectors.toSet());
        if (userNames.isEmpty()) {
            return new ArrayList();
        }
        return toBeans(userAccountRepository.findByUserNameIn(userNames));
    }

    @Override
    public Collection findAllByRole(String role1, String role2) {
        List<String> roleNames = new ArrayList<>();
        roleNames.add(role1);
        if (role2 != null && !role2.isEmpty()) {
            roleNames.add(role2);
        }
        List<RoleEntity> roles = roleRepository.findByRoleNameIn(roleNames);
        Set<String> userNames = roles.stream()
                .map(RoleEntity::getUserName)
                .collect(Collectors.toSet());
        if (userNames.isEmpty()) {
            return new ArrayList();
        }
        return toBeans(userAccountRepository.findByUserNameIn(userNames));
    }

    @Override
    public Collection findAllParticipantsByStudyOid(String studyOid) {
        String prefix = studyOid + ".";
        return toBeans(userAccountRepository.findAll().stream()
                .filter(e -> e.getUserName() != null && e.getUserName().startsWith(prefix))
                .collect(Collectors.toList()));
    }

    @Override
    public ArrayList findAllByStudyId(int studyId) {
        return findAllUsersByStudyIdAndLimit(studyId, false);
    }

    @Override
    public ArrayList findAllUsersByStudyIdAndLimit(int studyId, boolean isLimited) {
        List<RoleEntity> roles = roleRepository.findByStudyId(studyId);
        ArrayList result = new ArrayList();
        for (RoleEntity re : roles) {
            result.add(toRoleBean(re));
        }
        return result;
    }

    @Override
    public ArrayList findAllUsersByStudy(int studyId) {
        return buildStudyUserRoleBeans(studyId, false);
    }

    @Override
    public ArrayList findAllAssignedUsersByStudy(int studyId) {
        return buildStudyUserRoleBeans(studyId, true);
    }

    @Override
    public ArrayList findAllUsersByStudyOrSite(int studyId, int parentStudyId, int studySubjectId) {
        ArrayList result = new ArrayList();
        Set<String> seen = new HashSet<>();
        List<RoleEntity> roles = roleRepository.findByStudyId(studyId);
        for (RoleEntity re : roles) {
            if (seen.add(re.getUserName())) {
                result.add(buildFromRoleWithUser(re));
            }
        }
        if (parentStudyId > 0) {
            List<RoleEntity> parentRoles = roleRepository.findByStudyId(parentStudyId);
            for (RoleEntity re : parentRoles) {
                if (seen.add(re.getUserName())) {
                    result.add(buildFromRoleWithUser(re));
                }
            }
        }
        return result;
    }

    @Override
    public Collection findAllRolesByUserName(String userName) {
        List<RoleEntity> roles = roleRepository.findByUserName(userName);
        ArrayList result = new ArrayList();
        for (RoleEntity re : roles) {
            result.add(toRoleBean(re));
        }
        return result;
    }

    @Override
    public ArrayList findStudyByUser(String userName, ArrayList allStudies) {
        List<RoleEntity> roles = roleRepository.findByUserName(userName);
        ArrayList result = new ArrayList();
        for (RoleEntity re : roles) {
            result.add(toRoleBean(re));
        }
        return result;
    }

    @Override
    public UserAccountBean findStudyUserRole(UserAccountBean user, StudyUserRoleBean studyRole) {
        String userName = user.getName();
        List<RoleEntity> roles = roleRepository.findByUserNameAndStudyId(
                userName, studyRole.getStudyId());
        UserAccountBean result = new UserAccountBean();
        result.setName(userName);
        if (!roles.isEmpty()) {
            result.setActive(true);
        }
        return result;
    }

    @Override
    public StudyUserRoleBean findRoleByUserNameAndStudyId(String userName, int studyId) {
        List<RoleEntity> roles = roleRepository.findByUserNameAndStudyId(userName, studyId);
        if (!roles.isEmpty()) {
            StudyUserRoleBean bean = toRoleBean(roles.get(0));
            bean.setActive(true);
            return bean;
        }
        StudyUserRoleBean doesntExist = new StudyUserRoleBean();
        doesntExist.setActive(false);
        return doesntExist;
    }

    @Override
    public int findRoleCountByUserNameAndStudyId(String userName, int studyId, int childStudyId) {
        List<RoleEntity> roles = roleRepository.findByUserNameAndStudyId(userName, studyId);
        int count = roles.size();
        if (childStudyId != 0) {
            List<RoleEntity> childRoles = roleRepository.findByUserNameAndStudyId(userName, childStudyId);
            count += childRoles.size();
        }
        return count;
    }

    @Override
    @Transactional
    public StudyUserRoleBean createStudyUserRole(UserAccountBean user, StudyUserRoleBean studyRole) {
        RoleEntity entity = new RoleEntity();
        entity.setRoleName(studyRole.getRoleName());
        entity.setStudyId(studyRole.getStudyId());
        entity.setUserName(user.getName());
        entity.setStatusId(studyRole.getStatus() != null
                ? studyRole.getStatus().getId() : Status.AVAILABLE.getId());
        entity.setOwnerId(studyRole.getOwnerId() > 0 ? studyRole.getOwnerId() : null);
        return toRoleBean(roleRepository.save(entity));
    }

    @Override
    @Transactional
    public StudyUserRoleBean updateStudyUserRole(StudyUserRoleBean s, String userName) {
        List<RoleEntity> existing = roleRepository.findByUserNameAndStudyId(userName, s.getStudyId());
        RoleEntity entity;
        if (!existing.isEmpty()) {
            entity = existing.get(0);
        } else {
            entity = new RoleEntity();
            entity.setUserName(userName);
            entity.setStudyId(s.getStudyId());
        }
        entity.setRoleName(s.getRoleName());
        entity.setStatusId(s.getStatus() != null
                ? s.getStatus().getId() : Status.AVAILABLE.getId());
        entity.setOwnerId(s.getOwnerId() > 0 ? s.getOwnerId() : null);
        return toRoleBean(roleRepository.save(entity));
    }

    @Override
    public Collection findPrivilegesByRole(int roleId) {
        return new ArrayList();
    }

    @Override
    public Collection findPrivilegesByRoleName(String roleName) {
        return new ArrayList();
    }

    @Override
    public void setSysAdminRole(UserAccountBean uab, boolean creating) {
        // no-op: Role-based admin determination handled by legacy role model
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        return getEntityFromHashMap(hm, true);
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm, boolean findOwner) {
        UserAccountEntity entity = new UserAccountEntity();
        entity.setUserId((Integer) hm.get("user_id"));
        entity.setUserName((String) hm.get("user_name"));
        entity.setFirstName((String) hm.get("first_name"));
        entity.setLastName((String) hm.get("last_name"));
        entity.setPhone((String) hm.get("phone"));
        entity.setInstitutionalAffiliation((String) hm.get("institutional_affiliation"));
        entity.setUserTypeId((Integer) hm.get("user_type_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setActiveStudyId((Integer) hm.get("active_study_id"));
        entity.setEnabled((Boolean) hm.get("enabled"));
        entity.setAccountNonLocked((Boolean) hm.get("account_non_locked"));
        entity.setPasswordHash((String) hm.get("passwd"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        return toBean(entity);
    }

    @Override
    public StudyUserRoleBean getRoleFromHashMap(HashMap hm) {
        StudyUserRoleBean surb = new StudyUserRoleBean();
        surb.setName((String) hm.get("user_name"));
        surb.setUserName((String) hm.get("user_name"));
        surb.setRoleName((String) hm.get("role_name"));
        surb.setCreatedDate((Date) hm.get("date_created"));
        surb.setUpdatedDate((Date) hm.get("date_updated"));
        Integer statusId = (Integer) hm.get("status_id");
        Integer studyId = (Integer) hm.get("study_id");
        Integer ownerId = (Integer) hm.get("owner_id");
        surb.setStatus(Status.get(statusId != null ? statusId : 0));
        surb.setStudyId(studyId != null ? studyId : 0);
        surb.setOwnerId(ownerId != null ? ownerId : 0);
        return surb;
    }

    @Override
    public boolean isQuerySuccessful() {
        return true;
    }

    @Override
    public UserAccount findByUserId(Integer userId) {
        return null;
    }

    @Override
    public UserAccount saveOrUpdate(UserAccount userAccount) {
        return null;
    }

    private ArrayList buildStudyUserRoleBeans(int studyId, boolean assignedOnly) {
        ArrayList result = new ArrayList();
        List<RoleEntity> roles = roleRepository.findByStudyId(studyId);
        for (RoleEntity re : roles) {
            if (assignedOnly && re.getStatusId() != null
                    && re.getStatusId() == Status.DELETED.getId()) {
                continue;
            }
            result.add(buildFromRoleWithUser(re));
        }
        return result;
    }

    private StudyUserRoleBean buildFromRoleWithUser(RoleEntity re) {
        StudyUserRoleBean surb = toRoleBean(re);
        Optional<UserAccountEntity> userOpt = userAccountRepository.findByUserName(re.getUserName());
        userOpt.ifPresent(u -> {
            surb.setLastName(u.getLastName());
            surb.setFirstName(u.getFirstName());
            surb.setUserAccountId(u.getUserId() != null ? u.getUserId() : 0);
        });
        return surb;
    }

    private void apply(UserAccountBean bean, UserAccountEntity entity) {
        entity.setUserName(bean.getName());
        entity.setFirstName(bean.getFirstName());
        entity.setLastName(bean.getLastName());
        entity.setPhone(bean.getPhone());
        entity.setInstitutionalAffiliation(bean.getInstitutionalAffiliation());
        entity.setUserTypeId(UserType.USER.getId());
        entity.setStatusId(bean.getStatus() != null
                ? bean.getStatus().getId() : Status.AVAILABLE.getId());
        entity.setActiveStudyId(bean.getActiveStudyId() > 0 ? bean.getActiveStudyId() : null);
        entity.setEnabled(bean.getEnabled());
        entity.setAccountNonLocked(bean.getAccountNonLocked());
        entity.setPasswordHash(bean.getPasswd());
        entity.setOwnerId(bean.getOwnerId() > 0 ? bean.getOwnerId() : null);
        entity.setUpdateId(bean.getUpdaterId() > 0 ? bean.getUpdaterId() : null);
    }

    private ArrayList toBeans(List<UserAccountEntity> entities) {
        ArrayList beans = new ArrayList();
        for (UserAccountEntity e : entities) {
            beans.add(toBean(e));
        }
        return beans;
    }

    private UserAccountBean toBean(UserAccountEntity entity) {
        UserAccountBean bean = new UserAccountBean();
        if (entity.getUserId() != null) {
            bean.setId(entity.getUserId());
        }
        bean.setName(entity.getUserName());
        bean.setFirstName(entity.getFirstName());
        bean.setLastName(entity.getLastName());
        bean.setPhone(entity.getPhone());
        bean.setInstitutionalAffiliation(entity.getInstitutionalAffiliation());
        if (entity.getUserTypeId() != null) {
            bean.addUserType(UserType.get(entity.getUserTypeId()));
        }
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setActiveStudyId(valueOrZero(entity.getActiveStudyId()));
        if (entity.getEnabled() != null) {
            bean.setEnabled(entity.getEnabled());
        }
        if (entity.getAccountNonLocked() != null) {
            bean.setAccountNonLocked(entity.getAccountNonLocked());
        }
        bean.setPasswd(entity.getPasswordHash());
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setActive(true);
        return bean;
    }

    private StudyUserRoleBean toRoleBean(RoleEntity entity) {
        StudyUserRoleBean bean = new StudyUserRoleBean();
        if (entity.getStudyUserRoleId() != null) {
            bean.setId(entity.getStudyUserRoleId().intValue());
        }
        bean.setName(entity.getUserName());
        bean.setUserName(entity.getUserName());
        bean.setRoleName(entity.getRoleName());
        bean.setStudyId(entity.getStudyId() != null ? entity.getStudyId() : 0);
        bean.setStatus(Status.get(valueOrZero(entity.getStatusId())));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        return bean;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return new Date(0);
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }
}
