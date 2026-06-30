package org.researchedc.module.subjectgroup.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.subjectgroup.entity.StudyGroupClassEntity;
import org.researchedc.module.subjectgroup.entity.StudyGroupEntity;
import org.researchedc.module.subjectgroup.repository.StudyGroupClassRepository;
import org.researchedc.module.subjectgroup.repository.StudyGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class SubjectGroupServiceTest {

    @Mock private StudyGroupClassRepository classRepository;
    @Mock private StudyGroupRepository groupRepository;
    @Mock private AuditService auditService;
    @Mock private CurrentStudyAccessService currentStudyAccessService;

    private SubjectGroupService service;

    @BeforeEach
    void setUp() {
        service = new SubjectGroupService(classRepository, groupRepository,
                auditService, currentStudyAccessService);
    }

    // --- listClassesByStudy ---

    @Test
    void listClassesByStudy_returnsClasses() {
        StudyGroupClassEntity c1 = createClassEntity(1, "Race", 10, "Subject");
        StudyGroupClassEntity c2 = createClassEntity(2, "Age Group", 10, "Subject");
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(true);
        when(classRepository.findByStudyId(10)).thenReturn(List.of(c1, c2));

        List<StudyGroupClassEntity> result = service.listClassesByStudy(10, 42);

        assertEquals(2, result.size());
        assertEquals("Race", result.get(0).getName());
        assertEquals("Age Group", result.get(1).getName());
        verify(classRepository).findByStudyId(10);
    }

    @Test
    void listClassesByStudy_emptyStudy_returnsEmptyList() {
        when(currentStudyAccessService.canReadStudy(42, 999)).thenReturn(true);
        when(classRepository.findByStudyId(999)).thenReturn(List.of());

        List<StudyGroupClassEntity> result = service.listClassesByStudy(999, 42);

        assertTrue(result.isEmpty());
    }

    @Test
    void listClassesByStudy_whenAccessDenied_throwsAndDoesNotQuery() {
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.listClassesByStudy(10, 42));
        verifyNoInteractions(classRepository);
    }

    // --- getClassById ---

    @Test
    void getClassById_whenFound_returnsClass() {
        StudyGroupClassEntity c = createClassEntity(1, "Race", 10, "Subject");
        when(classRepository.findById(1)).thenReturn(Optional.of(c));
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(true);

        StudyGroupClassEntity result = service.getClassById(1, 42);

        assertEquals(1, result.getStudyGroupClassId());
        assertEquals("Race", result.getName());
    }

    @Test
    void getClassById_whenNotFound_throwsException() {
        when(classRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getClassById(99, 42));
    }

    @Test
    void getClassById_whenAccessDenied_throws() {
        StudyGroupClassEntity c = createClassEntity(1, "Race", 10, "Subject");
        when(classRepository.findById(1)).thenReturn(Optional.of(c));
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getClassById(1, 42));
    }

    // --- getGroupsByClassId ---

    @Test
    void getGroupsByClassId_returnsGroups() {
        StudyGroupClassEntity c = createClassEntity(10, "Race", 20, "Subject");
        StudyGroupEntity g1 = createGroupEntity(1, "Group A", "desc", 10);
        StudyGroupEntity g2 = createGroupEntity(2, "Group B", "desc", 10);
        when(classRepository.findById(10)).thenReturn(Optional.of(c));
        when(currentStudyAccessService.canReadStudy(42, 20)).thenReturn(true);
        when(groupRepository.findByStudyGroupClassId(10)).thenReturn(List.of(g1, g2));

        List<StudyGroupEntity> result = service.getGroupsByClassId(10, 42);

        assertEquals(2, result.size());
        assertEquals("Group A", result.get(0).getName());
        assertEquals("Group B", result.get(1).getName());
    }

    @Test
    void getGroupsByClassId_emptyClass_returnsEmptyList() {
        StudyGroupClassEntity c = createClassEntity(999, "Race", 20, "Subject");
        when(classRepository.findById(999)).thenReturn(Optional.of(c));
        when(currentStudyAccessService.canReadStudy(42, 20)).thenReturn(true);
        when(groupRepository.findByStudyGroupClassId(999)).thenReturn(List.of());

        assertTrue(service.getGroupsByClassId(999, 42).isEmpty());
    }

    @Test
    void getGroupsByClassId_whenAccessDenied_throwsAndDoesNotQueryGroups() {
        StudyGroupClassEntity c = createClassEntity(10, "Race", 20, "Subject");
        when(classRepository.findById(10)).thenReturn(Optional.of(c));
        when(currentStudyAccessService.canReadStudy(42, 20)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getGroupsByClassId(10, 42));
        verifyNoInteractions(groupRepository);
    }

    // --- createClass ---

    @Test
    void createClass_savesAndReturns() {
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(classRepository.save(any(StudyGroupClassEntity.class)))
                .thenAnswer(i -> {
                    StudyGroupClassEntity e = i.getArgument(0);
                    e.setStudyGroupClassId(5);
                    return e;
                });

        StudyGroupClassEntity result = service.createClass("Ethnicity", 10, "Subject", 42, 42);

        assertEquals(5, result.getStudyGroupClassId());
        assertEquals("Ethnicity", result.getName());
        assertEquals(10, result.getStudyId());
        assertEquals("Subject", result.getSubjectAssignment());
        assertEquals(42, result.getOwnerId());
        assertEquals(1, result.getStatusId());
        assertNotNull(result.getDateCreated());
        verify(classRepository).save(any(StudyGroupClassEntity.class));
    }

    @Test
    void createClass_whenAccessDenied_throwsAndDoesNotSave() {
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.createClass("Ethnicity", 10, "Subject", 42, 42));
        verify(classRepository, never()).save(any());
    }

    // --- updateClass ---

    @Test
    void updateClass_updatesFieldsAndReturns() {
        StudyGroupClassEntity existing = createClassEntity(1, "Old Name", 10, "Subject");
        when(classRepository.findById(1)).thenReturn(Optional.of(existing));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(classRepository.save(any(StudyGroupClassEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        StudyGroupClassEntity result = service.updateClass(1, "New Name", "Study", 42);

        assertEquals("New Name", result.getName());
        assertEquals("Study", result.getSubjectAssignment());
        assertNotNull(result.getDateUpdated());
        verify(classRepository).save(existing);
    }

    @Test
    void updateClass_whenNotFound_throwsException() {
        when(classRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.updateClass(99, "Name", "Subject", 42));
    }

    @Test
    void updateClass_whenAccessDenied_throwsAndDoesNotSave() {
        StudyGroupClassEntity existing = createClassEntity(1, "Old Name", 10, "Subject");
        when(classRepository.findById(1)).thenReturn(Optional.of(existing));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.updateClass(1, "Name", "Subject", 42));
        verify(classRepository, never()).save(any());
    }

    // --- createGroup ---

    @Test
    void createGroup_savesAndReturns() {
        StudyGroupClassEntity groupClass = createClassEntity(10, "Race", 20, "Subject");
        when(classRepository.findById(10)).thenReturn(Optional.of(groupClass));
        when(currentStudyAccessService.canWriteStudy(42, 20)).thenReturn(true);
        when(groupRepository.save(any(StudyGroupEntity.class)))
                .thenAnswer(i -> {
                    StudyGroupEntity e = i.getArgument(0);
                    e.setStudyGroupId(7);
                    return e;
                });

        StudyGroupEntity result = service.createGroup("Group A", "A description", 10, 42, 42);

        assertEquals(7, result.getStudyGroupId());
        assertEquals("Group A", result.getName());
        assertEquals("A description", result.getDescription());
        assertEquals(10, result.getStudyGroupClassId());
    }

    @Test
    void createGroup_nullDescription_defaultsToEmpty() {
        StudyGroupClassEntity groupClass = createClassEntity(10, "Race", 20, "Subject");
        when(classRepository.findById(10)).thenReturn(Optional.of(groupClass));
        when(currentStudyAccessService.canWriteStudy(42, 20)).thenReturn(true);
        when(groupRepository.save(any(StudyGroupEntity.class)))
                .thenAnswer(i -> {
                    StudyGroupEntity e = i.getArgument(0);
                    e.setStudyGroupId(8);
                    return e;
                });

        StudyGroupEntity result = service.createGroup("Group B", null, 10, 42, 42);

        assertEquals("", result.getDescription());
    }

    @Test
    void createGroup_whenAccessDenied_throwsAndDoesNotSave() {
        StudyGroupClassEntity groupClass = createClassEntity(10, "Race", 20, "Subject");
        when(classRepository.findById(10)).thenReturn(Optional.of(groupClass));
        when(currentStudyAccessService.canWriteStudy(42, 20)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.createGroup("Group B", null, 10, 42, 42));
        verifyNoInteractions(groupRepository);
    }

    // --- updateGroup ---

    @Test
    void updateGroup_updatesFieldsAndReturns() {
        StudyGroupEntity existing = createGroupEntity(1, "Old", "old desc", 10);
        StudyGroupClassEntity groupClass = createClassEntity(10, "Race", 20, "Subject");
        when(groupRepository.findById(1)).thenReturn(Optional.of(existing));
        when(classRepository.findById(10)).thenReturn(Optional.of(groupClass));
        when(currentStudyAccessService.canWriteStudy(42, 20)).thenReturn(true);
        when(groupRepository.save(any(StudyGroupEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        StudyGroupEntity result = service.updateGroup(1, "Updated", "new desc", 42);

        assertEquals("Updated", result.getName());
        assertEquals("new desc", result.getDescription());
        verify(groupRepository).save(existing);
    }

    @Test
    void updateGroup_nullDescription_defaultsToEmpty() {
        StudyGroupEntity existing = createGroupEntity(1, "Old", "old desc", 10);
        StudyGroupClassEntity groupClass = createClassEntity(10, "Race", 20, "Subject");
        when(groupRepository.findById(1)).thenReturn(Optional.of(existing));
        when(classRepository.findById(10)).thenReturn(Optional.of(groupClass));
        when(currentStudyAccessService.canWriteStudy(42, 20)).thenReturn(true);
        when(groupRepository.save(any(StudyGroupEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        StudyGroupEntity result = service.updateGroup(1, "Updated", null, 42);

        assertEquals("", result.getDescription());
    }

    @Test
    void updateGroup_whenNotFound_throwsException() {
        when(groupRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.updateGroup(99, "Name", "desc", 42));
    }

    @Test
    void updateGroup_whenAccessDenied_throwsAndDoesNotSave() {
        StudyGroupEntity existing = createGroupEntity(1, "Old", "old desc", 10);
        StudyGroupClassEntity groupClass = createClassEntity(10, "Race", 20, "Subject");
        when(groupRepository.findById(1)).thenReturn(Optional.of(existing));
        when(classRepository.findById(10)).thenReturn(Optional.of(groupClass));
        when(currentStudyAccessService.canWriteStudy(42, 20)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.updateGroup(1, "Updated", null, 42));
        verify(groupRepository, never()).save(any());
    }

    // --- factory methods ---

    private static StudyGroupClassEntity createClassEntity(Integer id, String name,
                                                           Integer studyId, String subjectAssignment) {
        StudyGroupClassEntity e = new StudyGroupClassEntity();
        e.setStudyGroupClassId(id);
        e.setName(name);
        e.setStudyId(studyId);
        e.setSubjectAssignment(subjectAssignment);
        e.setOwnerId(1);
        e.setStatusId(1);
        return e;
    }

    private static StudyGroupEntity createGroupEntity(Integer id, String name,
                                                       String description, Integer classId) {
        StudyGroupEntity e = new StudyGroupEntity();
        e.setStudyGroupId(id);
        e.setName(name);
        e.setDescription(description);
        e.setStudyGroupClassId(classId);
        return e;
    }
}
