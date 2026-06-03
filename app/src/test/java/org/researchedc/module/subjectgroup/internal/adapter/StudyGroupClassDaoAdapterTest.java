package org.researchedc.module.subjectgroup.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.core.GroupClassType;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.module.subjectgroup.entity.StudyGroupClassEntity;
import org.researchedc.module.subjectgroup.repository.StudyGroupClassRepository;

@ExtendWith(MockitoExtension.class)
class StudyGroupClassDaoAdapterTest {

    @Mock
    private StudyGroupClassRepository repository;

    private StudyGroupClassDaoAdapter adapter;

    @BeforeAll
    static void setUpI18n() {
        ResourceBundleProvider.updateLocale(Locale.getDefault());
    }

    @BeforeEach
    void setUp() {
        adapter = new StudyGroupClassDaoAdapter(repository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        StudyGroupClassEntity entity = groupClass(9, "Arm", 3, Status.AVAILABLE.getId());
        entity.setDateCreated(LocalDateTime.now());
        entity.setOwnerId(100);
        entity.setUpdateId(101);
        when(repository.findById(9)).thenReturn(Optional.of(entity));

        StudyGroupClassBean bean = (StudyGroupClassBean) adapter.findByPK(9);

        assertEquals(9, bean.getId());
        assertEquals("Arm", bean.getName());
        assertEquals(3, bean.getStudyId());
        assertEquals(GroupClassType.ARM.getId(), bean.getGroupClassTypeId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals("manual", bean.getSubjectAssignment());
        assertEquals(100, bean.getOwnerId());
        assertEquals(101, bean.getUpdaterId());
    }

    @Test
    void findAllActiveByStudy_queriesStudyAndChildStudiesAndMarksUnselected() {
        StudyBean study = new StudyBean();
        study.setId(3);
        when(repository.findByStudyOrChildStudyAndStatus(3, Status.AVAILABLE.getId()))
                .thenReturn(List.of(groupClass(2, "Beta", 4, 1), groupClass(1, "Alpha", 3, 1)));

        ArrayList result = adapter.findAllActiveByStudy(study);

        assertEquals(2, result.size());
        assertEquals("Alpha", ((StudyGroupClassBean) result.get(0)).getName());
        assertEquals("Beta", ((StudyGroupClassBean) result.get(1)).getName());
        assertFalse(((StudyGroupClassBean) result.get(0)).isSelected());
    }

    @Test
    void findAllByStudy_queriesStudyAndChildStudies() {
        StudyBean study = new StudyBean();
        study.setId(8);
        when(repository.findByStudyOrChildStudy(8)).thenReturn(List.of(groupClass(1, "Class", 8, 2)));

        ArrayList result = adapter.findAllByStudy(study);

        assertEquals(1, result.size());
        verify(repository).findByStudyOrChildStudy(8);
    }

    @Test
    void create_mapsLegacyBeanToEntity() {
        StudyGroupClassEntity saved = groupClass(12, "Created", 6, Status.AVAILABLE.getId());
        when(repository.save(argThat(e -> {
            assertEquals("Created", e.getName());
            assertEquals(6, e.getStudyId());
            assertEquals(7, e.getOwnerId());
            assertEquals(GroupClassType.DEMOGRAPHIC.getId(), e.getGroupClassTypeId());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals("auto", e.getSubjectAssignment());
            return true;
        }))).thenReturn(saved);

        StudyGroupClassBean input = new StudyGroupClassBean();
        input.setName("Created");
        input.setStudyId(6);
        input.setOwnerId(7);
        input.setGroupClassTypeId(GroupClassType.DEMOGRAPHIC.getId());
        input.setStatus(Status.AVAILABLE);
        input.setSubjectAssignment("auto");

        StudyGroupClassBean result = (StudyGroupClassBean) adapter.create(input);

        assertEquals(12, result.getId());
        verify(repository).save(argThat(e -> e.getDateCreated() != null));
    }

    @Test
    void update_mapsUpdaterAndDate() {
        StudyGroupClassEntity existing = groupClass(12, "Old", 6, Status.AVAILABLE.getId());
        when(repository.findById(12)).thenReturn(Optional.of(existing));
        when(repository.save(argThat(e -> {
            assertEquals(12, e.getStudyGroupClassId());
            assertEquals("Updated", e.getName());
            assertEquals(99, e.getUpdateId());
            assertEquals(Status.DELETED.getId(), e.getStatusId());
            return true;
        }))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyGroupClassBean input = new StudyGroupClassBean();
        input.setId(12);
        input.setName("Updated");
        input.setStudyId(6);
        input.setUpdaterId(99);
        input.setGroupClassTypeId(GroupClassType.OTHER.getId());
        input.setStatus(Status.DELETED);

        StudyGroupClassBean result = (StudyGroupClassBean) adapter.update(input);

        assertEquals("Updated", result.getName());
        verify(repository).save(argThat(e -> e.getDateUpdated() != null));
    }

    @Test
    void getCurrentPK_returnsMaxId() {
        when(repository.findAll()).thenReturn(List.of(
                groupClass(10, "A", 1, 1),
                groupClass(15, "B", 1, 1),
                groupClass(null, "C", 1, 1)));

        assertEquals(15, adapter.getCurrentPK());
    }

    private static StudyGroupClassEntity groupClass(Integer id, String name, Integer studyId, Integer statusId) {
        StudyGroupClassEntity entity = new StudyGroupClassEntity();
        entity.setStudyGroupClassId(id);
        entity.setName(name);
        entity.setStudyId(studyId);
        entity.setGroupClassTypeId(GroupClassType.ARM.getId());
        entity.setStatusId(statusId);
        entity.setSubjectAssignment("manual");
        return entity;
    }
}
