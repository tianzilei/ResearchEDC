package org.researchedc.module.subject.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.module.subject.entity.SubjectEntity;
import org.researchedc.module.subject.repository.SubjectRepository;

@ExtendWith(MockitoExtension.class)
class SubjectDaoAdapterTest {

    @Mock
    private SubjectRepository subjectRepository;

    private SubjectDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SubjectDaoAdapter(subjectRepository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        LocalDateTime created = LocalDateTime.now().minusYears(1);
        SubjectEntity entity = subject(7, "SUBJ-7", "f", Status.AVAILABLE.getId());
        entity.setDobCollected(true);
        entity.setDateOfBirth(created.minusYears(30));
        entity.setDateCreated(created);
        entity.setDateUpdated(created.plusDays(1));
        entity.setOwnerId(20);
        entity.setUpdateId(21);
        when(subjectRepository.findById(7)).thenReturn(Optional.of(entity));

        SubjectBean bean = (SubjectBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals("SUBJ-7", bean.getUniqueIdentifier());
        assertEquals('f', bean.getGender());
        assertTrue(bean.isDobCollected());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(20, bean.getOwnerId());
        assertEquals(21, bean.getUpdaterId());
        assertTrue(bean.isActive());
    }

    @Test
    void findByPK_whenMissing_returnsEmptySubjectBean() {
        when(subjectRepository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(SubjectBean.class, bean);
        assertEquals(0, ((SubjectBean) bean).getId());
    }

    @Test
    void findAll_returnsAvailableSubjects() {
        when(subjectRepository.findByStatusId(Status.AVAILABLE.getId()))
                .thenReturn(List.of(subject(1, "A", "m", 1), subject(2, "B", "f", 1)));

        ArrayList subjects = (ArrayList) adapter.findAll();

        assertEquals(2, subjects.size());
        verify(subjectRepository).findByStatusId(Status.AVAILABLE.getId());
    }

    @Test
    void findByUniqueIdentifierAndStudy_usesNativeRepositoryLookup() {
        when(subjectRepository.findByUniqueIdentifierAndStudyNative("SUBJ", 9))
                .thenReturn(Optional.of(subject(3, "SUBJ", "m", Status.AVAILABLE.getId())));

        SubjectBean bean = adapter.findByUniqueIdentifierAndStudy("SUBJ", 9);

        assertEquals(3, bean.getId());
        verify(subjectRepository).findByUniqueIdentifierAndStudyNative("SUBJ", 9);
    }

    @Test
    void findByUniqueIdentifier_whenMissing_returnsNullLikeLegacyDao() {
        when(subjectRepository.findByUniqueIdentifier("MISSING")).thenReturn(Optional.empty());

        assertNull(adapter.findByUniqueIdentifier("MISSING"));
    }

    @Test
    void findAllByGenderNotSelf_delegatesToGenderAndIdRepositoryMethod() {
        when(subjectRepository.findByGenderAndSubjectIdNot("f", 4))
                .thenReturn(List.of(subject(5, "OTHER", "f", Status.AVAILABLE.getId())));

        ArrayList subjects = adapter.findAllByGenderNotSelf('f', 4);

        assertEquals(1, subjects.size());
        assertEquals(5, ((SubjectBean) subjects.get(0)).getId());
        verify(subjectRepository).findByGenderAndSubjectIdNot("f", 4);
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        SubjectEntity saved = subject(11, "CREATED", "f", Status.AVAILABLE.getId());
        when(subjectRepository.save(argThat(e -> {
            assertEquals("CREATED", e.getUniqueIdentifier());
            assertEquals("f", e.getGender());
            assertEquals(true, e.getDobCollected());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals(31, e.getOwnerId());
            return e.getDateCreated() != null;
        }))).thenReturn(saved);

        SubjectBean input = new SubjectBean();
        input.setUniqueIdentifier("CREATED");
        input.setGender('f');
        input.setDobCollected(true);
        input.setStatus(Status.AVAILABLE);
        input.setOwnerId(31);

        SubjectBean result = (SubjectBean) adapter.create(input);

        assertEquals(11, result.getId());
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        Date now = new Date();
        HashMap row = new HashMap();
        row.put("subject_id", 30);
        row.put("unique_identifier", "ROW");
        row.put("date_of_birth", now);
        row.put("gender", "f");
        row.put("dob_collected", true);
        row.put("status_id", Status.AVAILABLE.getId());
        row.put("date_created", now);
        row.put("owner_id", 31);
        row.put("update_id", 32);

        SubjectBean bean = (SubjectBean) adapter.getEntityFromHashMap(row);

        assertEquals(30, bean.getId());
        assertEquals("ROW", bean.getUniqueIdentifier());
        assertEquals('f', bean.getGender());
        assertTrue(bean.isDobCollected());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(31, bean.getOwnerId());
        assertEquals(32, bean.getUpdaterId());
    }

    private static SubjectEntity subject(Integer id, String uniqueIdentifier, String gender, Integer statusId) {
        SubjectEntity entity = new SubjectEntity();
        entity.setSubjectId(id);
        entity.setUniqueIdentifier(uniqueIdentifier);
        entity.setGender(gender);
        entity.setStatusId(statusId);
        return entity;
    }
}
