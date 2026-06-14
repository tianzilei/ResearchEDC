package org.researchedc.module.subject.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.domain.datamap.StudySubject;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;

@ExtendWith(MockitoExtension.class)
class StudySubjectDaoAdapterTest {

    @Mock
    private StudySubjectRepository repository;

    private StudySubjectDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new StudySubjectDaoAdapter(repository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        LocalDateTime enrolled = LocalDateTime.now().minusDays(4);
        StudySubjectEntity entity = studySubject(7, 3, 4, "SS-7", Status.AVAILABLE.getId());
        entity.setSecondaryLabel("ALT");
        entity.setEnrollmentDate(enrolled);
        entity.setOcOid("SS_OID");
        entity.setDateCreated(enrolled.minusDays(1));
        entity.setDateUpdated(enrolled.plusDays(1));
        entity.setOwnerId(20);
        entity.setUpdateId(21);
        when(repository.findById(7)).thenReturn(Optional.of(entity));

        StudySubjectBean bean = (StudySubjectBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals(3, bean.getStudyId());
        assertEquals(4, bean.getSubjectId());
        assertEquals("SS-7", bean.getLabel());
        assertEquals("ALT", bean.getSecondaryLabel());
        assertEquals("SS_OID", bean.getOid());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(20, bean.getOwnerId());
        assertEquals(21, bean.getUpdaterId());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyStudySubjectBean() {
        when(repository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(StudySubjectBean.class, bean);
        assertEquals(0, ((StudySubjectBean) bean).getId());
    }

    @Test
    void findAllActiveByStudyOrderByLabel_usesAvailableStatus() {
        StudyBean study = new StudyBean();
        study.setId(10);
        when(repository.findByStudyIdAndStatusIdOrderByLabel(10, Status.AVAILABLE.getId()))
                .thenReturn(List.of(studySubject(1, 10, 20, "A", 1)));

        ArrayList subjects = adapter.findAllActiveByStudyOrderByLabel(study);

        assertEquals(1, subjects.size());
        verify(repository).findByStudyIdAndStatusIdOrderByLabel(10, Status.AVAILABLE.getId());
    }

    @Test
    void findAnotherBySameLabel_excludesCurrentStudySubjectId() {
        when(repository.findByLabelAndStudyId("SUBJ", 9)).thenReturn(List.of(
                studySubject(5, 9, 30, "SUBJ", 1),
                studySubject(6, 9, 31, "SUBJ", 1)));

        StudySubjectBean bean = (StudySubjectBean) adapter.findAnotherBySameLabel("SUBJ", 9, 5);

        assertEquals(6, bean.getId());
    }

    @Test
    void findAnotherBySameLabel_whenOnlyCurrentMatch_returnsNull() {
        when(repository.findByLabelAndStudyId("SUBJ", 9))
                .thenReturn(List.of(studySubject(5, 9, 30, "SUBJ", 1)));

        assertNull(adapter.findAnotherBySameLabel("SUBJ", 9, 5));
    }

    @Test
    void findStudySubjectIdsByStudyIds_ignoresInvalidIdsAndJoinsResults() {
        when(repository.findByStudyId(1)).thenReturn(List.of(
                studySubject(10, 1, 50, "A", 1),
                studySubject(11, 1, 51, "B", 1)));
        when(repository.findByStudyId(2)).thenReturn(List.of(studySubject(20, 2, 60, "C", 1)));

        String ids = adapter.findStudySubjectIdsByStudyIds("1, bad, 2");

        assertEquals("10,11,20", ids);
        verify(repository).findByStudyId(1);
        verify(repository).findByStudyId(2);
    }

    @Test
    void findTheGreatestLabel_parsesTopStudySubjectLabel() {
        when(repository.findTopByOrderByStudySubjectIdDesc())
                .thenReturn(Optional.of(studySubject(40, 1, 50, "123", 1)));

        assertEquals(123, adapter.findTheGreatestLabel());
    }

    @Test
    void countByStudyAndStatus_delegatesToRepository() {
        StudyBean study = new StudyBean();
        study.setId(17);
        when(repository.countByStudyIdAndStatusId(17, Status.LOCKED.getId())).thenReturn(5L);

        assertEquals(5, adapter.getCountofStudySubjectsBasedOnStatus(study, Status.LOCKED));
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        StudySubjectEntity saved = studySubject(15, 9, 30, "CREATED", Status.AVAILABLE.getId(), "OID15");
        when(repository.save(argThat(e -> {
            assertEquals(9, e.getStudyId());
            assertEquals(30, e.getSubjectId());
            assertEquals("CREATED", e.getLabel());
            assertEquals("ALT", e.getSecondaryLabel());
            assertEquals("OID15", e.getOcOid());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals(31, e.getOwnerId());
            return e.getDateCreated() != null;
        }))).thenReturn(saved);

        StudySubjectBean input = new StudySubjectBean();
        input.setStudyId(9);
        input.setSubjectId(30);
        input.setLabel("CREATED");
        input.setSecondaryLabel("ALT");
        input.setOid("OID15");
        input.setStatus(Status.AVAILABLE);
        input.setOwnerId(31);

        StudySubjectBean result = (StudySubjectBean) adapter.create(input);

        assertEquals(15, result.getId());
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        Date now = new Date();
        HashMap row = new HashMap();
        row.put("study_subject_id", 50);
        row.put("study_id", 51);
        row.put("subject_id", 52);
        row.put("label", "ROW");
        row.put("secondary_label", "ROW_ALT");
        row.put("enrollment_date", now);
        row.put("oc_oid", "ROW_OID");
        row.put("date_created", now);
        row.put("owner_id", 53);
        row.put("update_id", 54);
        row.put("status_id", Status.AVAILABLE.getId());

        StudySubjectBean bean = (StudySubjectBean) adapter.getEntityFromHashMap(row);

        assertEquals(50, bean.getId());
        assertEquals(51, bean.getStudyId());
        assertEquals(52, bean.getSubjectId());
        assertEquals("ROW", bean.getLabel());
        assertEquals("ROW_ALT", bean.getSecondaryLabel());
        assertEquals("ROW_OID", bean.getOid());
        assertEquals(53, bean.getOwnerId());
        assertEquals(54, bean.getUpdaterId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
    }

    private static StudySubjectEntity studySubject(Integer id, Integer studyId, Integer subjectId, String label,
                                                   Integer statusId) {
        return studySubject(id, studyId, subjectId, label, statusId, null);
    }

    private static StudySubjectEntity studySubject(Integer id, Integer studyId, Integer subjectId, String label,
                                                   Integer statusId, String oid) {
        StudySubjectEntity entity = new StudySubjectEntity();
        entity.setStudySubjectId(id);
        entity.setStudyId(studyId);
        entity.setSubjectId(subjectId);
        entity.setLabel(label);
        entity.setStatusId(statusId);
        entity.setOcOid(oid);
        return entity;
    }
}
