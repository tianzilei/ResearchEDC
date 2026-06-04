package org.researchedc.module.subjectgroup.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyGroupBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.module.subjectgroup.entity.StudyGroupEntity;
import org.researchedc.module.subjectgroup.repository.StudyGroupRepository;

@ExtendWith(MockitoExtension.class)
class StudyGroupDaoAdapterTest {

    @Mock
    private StudyGroupRepository repository;

    private StudyGroupDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new StudyGroupDaoAdapter(repository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        when(repository.findById(5)).thenReturn(Optional.of(group(5, "Treatment", "Arm A", 3)));

        StudyGroupBean bean = (StudyGroupBean) adapter.findByPK(5);

        assertEquals(5, bean.getId());
        assertEquals("Treatment", bean.getName());
        assertEquals("Arm A", bean.getDescription());
        assertEquals(3, bean.getStudyGroupClassId());
    }

    @Test
    void findAllByStudy_queriesStudyAndChildStudies() {
        StudyBean study = new StudyBean();
        study.setId(8);
        when(repository.findByStudyOrChildStudy(8)).thenReturn(List.of(group(2, "B", "", 3), group(1, "A", "", 3)));

        ArrayList result = adapter.findAllByStudy(study);

        assertEquals(2, result.size());
        assertEquals(1, ((StudyGroupBean) result.get(0)).getId());
        assertEquals(2, ((StudyGroupBean) result.get(1)).getId());
        verify(repository).findByStudyOrChildStudy(8);
    }

    @Test
    void findAllByGroupClass_queriesGroupsByClass() {
        StudyGroupClassBean groupClass = new StudyGroupClassBean();
        groupClass.setId(9);
        when(repository.findByStudyGroupClassId(9)).thenReturn(List.of(group(3, "Dose", "", 9)));

        ArrayList result = adapter.findAllByGroupClass(groupClass);

        assertEquals(1, result.size());
        assertEquals("Dose", ((StudyGroupBean) result.get(0)).getName());
    }

    @Test
    void getGroupByStudySubject_preservesLegacyStudyScope() {
        when(repository.findByStudySubjectInStudyOrChildStudy(4, 10, 1)).thenReturn(List.of(group(7, "Site Arm", "", 2)));

        ArrayList result = adapter.getGroupByStudySubject(4, 10, 1);

        assertEquals(1, result.size());
        assertEquals(7, ((StudyGroupBean) result.get(0)).getId());
    }

    @Test
    void findSubjectStudyGroup_returnsFirstMatchingGroup() {
        when(repository.findSubjectStudyGroup(11, "Randomization Arm"))
                .thenReturn(List.of(group(6, "Arm B", "", 5)));

        StudyGroupBean bean = adapter.findSubjectStudyGroup(11, "Randomization Arm");

        assertEquals("Arm B", bean.getName());
    }

    @Test
    void findByStudySubject_returnsMapKeyedByGroupClassId() {
        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setId(15);
        when(repository.findByStudySubject(15)).thenReturn(List.of(group(4, "Alpha", "", 20)));

        HashMap result = adapter.findByStudySubject(studySubject);

        assertEquals("Alpha", ((StudyGroupBean) result.get(20)).getName());
    }

    @Test
    void findSubjectGroupMaps_preservesLegacyNestedMapShape() {
        when(repository.findSubjectGroupMapRows(30)).thenReturn(List.of(
                new Object[] {1, "Arm A", "", 100, 2000},
                new Object[] {2, "Cohort X", "", 101, 2000},
                new Object[] {3, "Arm B", "", 100, 2001}));

        HashMap result = adapter.findSubjectGroupMaps(30);

        ArrayList subject2000Maps = (ArrayList) result.get(2000);
        assertEquals(2, subject2000Maps.size());
        assertEquals("Arm A", ((StudyGroupBean) ((HashMap) subject2000Maps.get(0)).get(100)).getName());
        assertEquals("Cohort X", ((StudyGroupBean) ((HashMap) subject2000Maps.get(1)).get(101)).getName());
        assertEquals("Arm B", ((StudyGroupBean) ((HashMap) ((ArrayList) result.get(2001)).get(0)).get(100)).getName());
    }

    @Test
    void create_mapsLegacyBeanToEntity() {
        when(repository.save(argThat(e -> {
            assertEquals("Created", e.getName());
            assertEquals("desc", e.getDescription());
            assertEquals(42, e.getStudyGroupClassId());
            return true;
        }))).thenReturn(group(12, "Created", "desc", 42));

        StudyGroupBean input = new StudyGroupBean();
        input.setName("Created");
        input.setDescription("desc");
        input.setStudyGroupClassId(42);

        StudyGroupBean result = (StudyGroupBean) adapter.create(input);

        assertEquals(12, result.getId());
        verify(repository).save(argThat(e -> e.getStudyGroupId() == null));
    }

    private static StudyGroupEntity group(Integer id, String name, String description, Integer classId) {
        StudyGroupEntity entity = new StudyGroupEntity();
        entity.setStudyGroupId(id);
        entity.setName(name);
        entity.setDescription(description);
        entity.setStudyGroupClassId(classId);
        return entity;
    }
}
