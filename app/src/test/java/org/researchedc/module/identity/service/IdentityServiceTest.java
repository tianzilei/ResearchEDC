package org.researchedc.module.identity.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.identity.dto.AssignRoleRequest;
import org.researchedc.module.identity.dto.CreateUserRequest;
import org.researchedc.module.identity.dto.RoleDTO;
import org.researchedc.module.identity.dto.UserDTO;
import org.researchedc.module.identity.entity.RoleEntity;
import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.identity.repository.RoleRepository;
import org.researchedc.module.identity.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private AuditService auditService;
    @Mock private PasswordEncoder passwordEncoder;

    private IdentityService service;

    @BeforeEach
    void setUp() {
        service = new IdentityService(userAccountRepository, roleRepository, auditService, passwordEncoder);
    }

    private static UserAccountEntity createUser(Integer id, String userName) {
        UserAccountEntity e = new UserAccountEntity();
        e.setUserId(id);
        e.setUserName(userName);
        e.setFirstName("John");
        e.setLastName("Doe");
        e.setEnabled(true);
        return e;
    }

    private static RoleEntity createRole(Long id, String userName, String roleName, Integer studyId) {
        RoleEntity e = new RoleEntity();
        e.setStudyUserRoleId(id);
        e.setUserName(userName);
        e.setRoleName(roleName);
        e.setStudyId(studyId);
        return e;
    }

    @Test
    void searchUsers_returnsMatching() {
        when(userAccountRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("john", "john"))
                .thenReturn(List.of(createUser(1, "jdoe")));

        List<UserDTO> result = service.searchUsers("john");

        assertEquals(1, result.size());
        assertEquals("jdoe", result.getFirst().getUserName());
    }

    @Test
    void getUserByUsername_whenFound_returnsDto() {
        when(userAccountRepository.findByUserName("jdoe"))
                .thenReturn(Optional.of(createUser(1, "jdoe")));

        UserDTO result = service.getUserByUsername("jdoe");

        assertEquals("jdoe", result.getUserName());
    }

    @Test
    void getUserByUsername_whenNotFound_throwsException() {
        when(userAccountRepository.findByUserName("nobody"))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.getUserByUsername("nobody"));
    }

    @Test
    void getUser_whenFound_returnsDto() {
        when(userAccountRepository.findById(1))
                .thenReturn(Optional.of(createUser(1, "jdoe")));

        UserDTO result = service.getUser(1);

        assertEquals("jdoe", result.getUserName());
    }

    @Test
    void getUser_whenNotFound_throwsException() {
        when(userAccountRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getUser(99));
    }

    @Test
    void getUserRoles_returnsByUserName() {
        when(roleRepository.findByUserName("jdoe"))
                .thenReturn(List.of(createRole(1L, "jdoe", "admin", 1)));

        List<RoleDTO> result = service.getUserRoles("jdoe");

        assertEquals(1, result.size());
        assertEquals("admin", result.getFirst().getRoleName());
    }

    @Test
    void getStudyRoles_returnsByStudyId() {
        when(roleRepository.findByStudyId(1))
                .thenReturn(List.of(createRole(1L, "jdoe", "admin", 1)));

        List<RoleDTO> result = service.getStudyRoles(1);

        assertEquals(1, result.size());
    }

    @Test
    void createUser_withValidRequest_savesAndReturns() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("newuser");
        request.setFirstName("New");
        request.setLastName("User");

        when(userAccountRepository.findByUserName("newuser"))
                .thenReturn(Optional.empty());
        when(userAccountRepository.save(any(UserAccountEntity.class)))
                .thenAnswer(i -> {
                    UserAccountEntity e = i.getArgument(0);
                    if (e.getUserId() == null) e.setUserId(1);
                    return e;
                });

        UserDTO result = service.createUser(request, 42);

        assertEquals("newuser", result.getUserName());
        verify(auditService).recordAudit(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void createUser_withDuplicateUsername_throwsException() {
        when(userAccountRepository.findByUserName("existing"))
                .thenReturn(Optional.of(createUser(1, "existing")));

        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("existing");

        assertThrows(IllegalArgumentException.class,
                () -> service.createUser(request, 42));
    }

    @Test
    void createUser_withBlankUsername_throwsException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("");

        assertThrows(IllegalArgumentException.class,
                () -> service.createUser(request, 42));
    }

    @Test
    void assignRole_savesAndAudits() {
        when(roleRepository.save(any(RoleEntity.class)))
                .thenAnswer(i -> {
                    RoleEntity e = i.getArgument(0);
                    if (e.getStudyUserRoleId() == null) e.setStudyUserRoleId(1L);
                    return e;
                });

        AssignRoleRequest request = new AssignRoleRequest();
        request.setUserName("jdoe");
        request.setStudyId(1);
        request.setRoleName("admin");
        request.setStatusId(1);

        service.assignRole(request, 42);

        verify(roleRepository).save(any(RoleEntity.class));
        verify(auditService).recordAudit(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }
}
