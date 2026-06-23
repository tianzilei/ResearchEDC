package org.researchedc.module.study.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
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
import org.researchedc.module.study.entity.StudyEntity;
import org.researchedc.module.study.repository.StudyRepository;

@ExtendWith(MockitoExtension.class)
class StudyDaoAdapterTest {

    @Mock
    private StudyRepository repository;

    private StudyDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new StudyDaoAdapter(repository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        LocalDateTime created = LocalDateTime.now().minusDays(3);
        StudyEntity entity = study(7, "Parent", "PARENT", null, Status.AVAILABLE.getId());
        entity.setSummary("summary");
        entity.setSecondaryIdentifier("S1");
        entity.setDateCreated(created);
        entity.setDateUpdated(created.plusDays(1));
        entity.setOwnerId(20);
        entity.setUpdateId(21);
        entity.setTypeId(StudyBean.TYPE_NON_GENETIC);
        entity.setPrincipalInvestigator("PI");
        entity.setFacilityName("Clinic");
        entity.setExpectedTotalEnrollment(100);
        entity.setHealthyVolunteerAccepted(true);
        entity.setResultsReference(true);
        entity.setOcOid("S_OID");
        when(repository.findById(7)).thenReturn(Optional.of(entity));

        StudyBean bean = (StudyBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals("Parent", bean.getName());
        assertEquals("PARENT", bean.getIdentifier());
        assertEquals("S1", bean.getSecondaryIdentifier());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(StudyBean.TYPE_NON_GENETIC, bean.getTypeId());
        assertEquals(20, bean.getOwnerId());
        assertEquals(21, bean.getUpdaterId());
        assertEquals("PI", bean.getPrincipalInvestigator());
        assertEquals("Clinic", bean.getFacilityName());
        assertEquals(100, bean.getExpectedTotalEnrollment());
        assertTrue(bean.getHealthyVolunteerAccepted());
        assertTrue(bean.isResultsReference());
        assertEquals("S_OID", bean.getOid());
        assertTrue(bean.isActive());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyStudyBean() {
        when(repository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(StudyBean.class, bean);
        assertEquals(0, ((StudyBean) bean).getId());
    }

    @Test
    void create_mapsLegacyBeanToModuleEntityAndForcesAvailableStatus() {
        StudyEntity saved = study(11, "Created", "CREATED", null, Status.AVAILABLE.getId());
        when(repository.save(argThat(e -> {
            assertEquals("Created", e.getName());
            assertEquals("CREATED", e.getUniqueIdentifier());
            assertEquals("created summary", e.getSummary());
            assertEquals(StudyBean.TYPE_GENETIC, e.getTypeId());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals("OID_CREATED", e.getOcOid());
            assertEquals(31, e.getOwnerId());
            return true;
        }))).thenReturn(saved);

        StudyBean input = new StudyBean();
        input.setName("Created");
        input.setIdentifier("CREATED");
        input.setSummary("created summary");
        input.setStatus(Status.DELETED);
        input.setTypeId(StudyBean.TYPE_GENETIC);
        input.setOid("OID_CREATED");
        input.setOwnerId(31);

        StudyBean result = (StudyBean) adapter.create(input);

        assertEquals(11, result.getId());
        verify(repository).save(argThat(e -> e.getDateCreated() != null));
    }

    @Test
    void findSiteByUniqueIdentifier_resolvesParentThenSite() {
        StudyEntity parent = study(1, "Parent", "PARENT", null, Status.AVAILABLE.getId());
        StudyEntity site = study(2, "Site", "SITE", 1, Status.AVAILABLE.getId());
        when(repository.findByUniqueIdentifier("PARENT")).thenReturn(Optional.of(parent));
        when(repository.findByParentStudyIdAndUniqueIdentifier(1, "SITE")).thenReturn(Optional.of(site));

        StudyBean result = adapter.findSiteByUniqueIdentifier("PARENT", "SITE");

        assertEquals(2, result.getId());
        assertEquals(1, result.getParentStudyId());
        verify(repository).findByParentStudyIdAndUniqueIdentifier(1, "SITE");
    }

    @Test
    void findSiteByUniqueIdentifier_whenParentMissing_returnsNull() {
        when(repository.findByUniqueIdentifier("MISSING")).thenReturn(Optional.empty());

        assertNull(adapter.findSiteByUniqueIdentifier("MISSING", "SITE"));
    }

    @Test
    void updateSitesStatus_updatesEveryChildSite() {
        StudyEntity siteA = study(2, "Site A", "A", 1, Status.AVAILABLE.getId());
        StudyEntity siteB = study(3, "Site B", "B", 1, Status.AVAILABLE.getId());
        when(repository.findByParentStudyIdOrderByName(1)).thenReturn(List.of(siteA, siteB));
        when(repository.save(argThat(e -> e.getStatusId().equals(Status.LOCKED.getId())
                && e.getUpdateId().equals(77)
                && e.getDateUpdated() != null))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyBean input = new StudyBean();
        input.setId(1);
        input.setStatus(Status.LOCKED);
        input.setUpdaterId(77);

        StudyBean result = adapter.updateSitesStatus(input);

        assertEquals(1, result.getId());
        verify(repository).save(siteA);
        verify(repository).save(siteB);
    }

    @Test
    void updateStudyStatus_setsStatusOldStatusUpdaterAndTimestamp() {
        StudyEntity existing = study(8, "Study", "STUDY", null, Status.AVAILABLE.getId());
        when(repository.findById(8)).thenReturn(Optional.of(existing));
        when(repository.save(argThat(e -> {
            assertEquals(8, e.getStudyId());
            assertEquals(Status.DELETED.getId(), e.getStatusId());
            assertEquals(Status.AVAILABLE.getId(), e.getOldStatusId());
            assertEquals(91, e.getUpdateId());
            return e.getDateUpdated() != null;
        }))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyBean input = new StudyBean();
        input.setId(8);
        input.setStatus(Status.DELETED);
        input.setOldStatus(Status.AVAILABLE);
        input.setUpdaterId(91);

        StudyBean result = adapter.updateStudyStatus(input);

        assertEquals(8, result.getId());
    }

    @Test
    void findByOid_whenMissing_returnsNullLikeLegacyDao() {
        when(repository.findByOcOid("MISSING")).thenReturn(Optional.empty());

        assertNull(adapter.findByOid("MISSING"));
    }

    @Test
    void getStudyIdsByCRF_delegatesToModuleRepositoryQuery() {
        when(repository.findStudyIdsByCrfId(42)).thenReturn(List.of(3, 4));

        ArrayList<Integer> result = adapter.getStudyIdsByCRF(42);

        assertEquals(List.of(3, 4), result);
        verify(repository).findStudyIdsByCrfId(42);
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        Date now = new Date();
        HashMap row = new HashMap();
        row.put("study_id", 20);
        row.put("parent_study_id", 10);
        row.put("unique_identifier", "ROW");
        row.put("secondary_identifier", "RS");
        row.put("name", "From row");
        row.put("summary", "row summary");
        row.put("date_created", now);
        row.put("owner_id", 21);
        row.put("update_id", 22);
        row.put("type_id", StudyBean.TYPE_GENETIC);
        row.put("status_id", Status.AVAILABLE.getId());
        row.put("principal_investigator", "Row PI");
        row.put("facility_name", "Row Clinic");
        row.put("expected_total_enrollment", 40);
        row.put("healthy_volunteer_accepted", true);
        row.put("results_reference", true);
        row.put("oc_oid", "ROW_OID");

        StudyBean bean = (StudyBean) adapter.getEntityFromHashMap(row);

        assertEquals(20, bean.getId());
        assertEquals(10, bean.getParentStudyId());
        assertEquals("ROW", bean.getIdentifier());
        assertEquals("From row", bean.getName());
        assertEquals(StudyBean.TYPE_GENETIC, bean.getTypeId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals("Row PI", bean.getPrincipalInvestigator());
        assertEquals("Row Clinic", bean.getFacilityName());
        assertEquals(40, bean.getExpectedTotalEnrollment());
        assertTrue(bean.getHealthyVolunteerAccepted());
        assertTrue(bean.isResultsReference());
        assertEquals("ROW_OID", bean.getOid());
    }

    @Test
    void findAllByUserNotRemoved_mapsRepositoryResults() {
        when(repository.findByUserNameNotRemoved("alice")).thenReturn(List.of(
                study(1, "A", "A", null, Status.AVAILABLE.getId()),
                study(2, "B", "B", null, Status.LOCKED.getId())));

        Collection results = adapter.findAllByUserNotRemoved("alice");

        assertEquals(2, results.size());
        verify(repository).findByUserNameNotRemoved("alice");
    }

    @Test
    void retiredLegacyDaoStubs_areNotExposed() {
        assertNoMethod("findAll", String.class, boolean.class, String.class);
        assertNoMethod("findAllByPermission", Object.class, int.class, String.class, boolean.class, String.class);
        assertNoMethod("findAllByPermission", Object.class, int.class);
        assertNoMethod("findAllByLimit", boolean.class);
        assertNoMethod("getChildrenByParentIds", ArrayList.class);
        assertNoMethod("deleteTestOnly", String.class);
    }

    private static void assertNoMethod(String name, Class<?>... parameterTypes) {
        assertThrows(NoSuchMethodException.class, () -> StudyDaoAdapter.class.getDeclaredMethod(name, parameterTypes));
    }

    private static StudyEntity study(Integer id, String name, String identifier, Integer parentStudyId,
                                     Integer statusId) {
        StudyEntity entity = new StudyEntity();
        entity.setStudyId(id);
        entity.setName(name);
        entity.setUniqueIdentifier(identifier);
        entity.setParentStudyId(parentStudyId);
        entity.setStatusId(statusId);
        return entity;
    }
}
