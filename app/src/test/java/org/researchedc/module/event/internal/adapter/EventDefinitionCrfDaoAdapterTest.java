package org.researchedc.module.event.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
import org.researchedc.bean.managestudy.EventDefinitionCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.domain.SourceDataVerification;
import org.researchedc.module.event.entity.EventDefinitionCrfEntity;
import org.researchedc.module.event.repository.EventDefinitionCrfRepository;

@ExtendWith(MockitoExtension.class)
class EventDefinitionCrfDaoAdapterTest {

    @Mock
    private EventDefinitionCrfRepository eventDefinitionCrfRepository;

    private EventDefinitionCrfDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EventDefinitionCrfDaoAdapter(eventDefinitionCrfRepository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        LocalDateTime created = LocalDateTime.now().minusDays(2);
        EventDefinitionCrfEntity entity = eventDefinitionCrf(7, 3, 4, 5, Status.AVAILABLE.getId());
        entity.setRequiredCrf(true);
        entity.setDoubleEntry(true);
        entity.setRequireAllTextFilled(true);
        entity.setDecisionConditions(false);
        entity.setNullValues("NI,NA");
        entity.setDefaultVersionId(6);
        entity.setDateCreated(created);
        entity.setDateUpdated(created.plusDays(1));
        entity.setOwnerId(20);
        entity.setUpdateId(21);
        entity.setOrdinal(2);
        entity.setElectronicSignature(true);
        entity.setHideCrf(true);
        entity.setSourceDataVerificationCode(SourceDataVerification.PARTIALREQUIRED.getCode());
        entity.setSelectedVersionIds("6,8");
        entity.setParentId(22);
        entity.setParticipantForm(true);
        entity.setAllowAnonymousSubmission(true);
        entity.setSubmissionUrl("public-form");
        when(eventDefinitionCrfRepository.findById(7)).thenReturn(Optional.of(entity));

        EventDefinitionCRFBean bean = (EventDefinitionCRFBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals(3, bean.getStudyEventDefinitionId());
        assertEquals(4, bean.getStudyId());
        assertEquals(5, bean.getCrfId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertTrue(bean.isRequiredCRF());
        assertTrue(bean.isDoubleEntry());
        assertTrue(bean.isRequireAllTextFilled());
        assertEquals(false, bean.isDecisionCondition());
        assertEquals("NI,NA", bean.getNullValues());
        assertEquals(6, bean.getDefaultVersionId());
        assertEquals(20, bean.getOwnerId());
        assertEquals(21, bean.getUpdaterId());
        assertEquals(2, bean.getOrdinal());
        assertTrue(bean.isElectronicSignature());
        assertTrue(bean.isHideCrf());
        assertEquals(SourceDataVerification.PARTIALREQUIRED, bean.getSourceDataVerification());
        assertEquals("6,8", bean.getSelectedVersionIds());
        assertEquals(22, bean.getParentId());
        assertTrue(bean.isParticipantForm());
        assertTrue(bean.isAllowAnonymousSubmission());
        assertEquals("public-form", bean.getSubmissionUrl());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyEventDefinitionCrfBean() {
        when(eventDefinitionCrfRepository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(EventDefinitionCRFBean.class, bean);
        assertEquals(0, ((EventDefinitionCRFBean) bean).getId());
    }

    @Test
    void findAllByDefinition_delegatesToDefinitionRepositoryLookupAndSortsById() {
        when(eventDefinitionCrfRepository.findByStudyEventDefinitionId(3))
                .thenReturn(List.of(eventDefinitionCrf(9, 3, 4, 5, 1), eventDefinitionCrf(2, 3, 4, 6, 1)));

        ArrayList beans = (ArrayList) adapter.findAllByDefinition(3);

        assertEquals(2, beans.size());
        assertEquals(2, ((EventDefinitionCRFBean) beans.get(0)).getId());
        assertEquals(9, ((EventDefinitionCRFBean) beans.get(1)).getId());
        verify(eventDefinitionCrfRepository).findByStudyEventDefinitionId(3);
    }

    @Test
    void findAllByDefinitionWithStudy_usesStudyScopedRepositoryLookup() {
        StudyBean study = new StudyBean();
        study.setId(4);
        when(eventDefinitionCrfRepository.findByStudyEventDefinitionIdAndStudyId(3, 4))
                .thenReturn(List.of(eventDefinitionCrf(7, 3, 4, 5, 1)));

        ArrayList beans = (ArrayList) adapter.findAllByDefinition(study, 3);

        assertEquals(1, beans.size());
        verify(eventDefinitionCrfRepository).findByStudyEventDefinitionIdAndStudyId(3, 4);
    }

    @Test
    void findAllActiveByEventDefinitionId_usesAvailableStatus() {
        when(eventDefinitionCrfRepository.findByStudyEventDefinitionIdAndStatusId(3, Status.AVAILABLE.getId()))
                .thenReturn(List.of(eventDefinitionCrf(7, 3, 4, 5, Status.AVAILABLE.getId())));

        ArrayList beans = adapter.findAllActiveByEventDefinitionId(3);

        assertEquals(1, beans.size());
        verify(eventDefinitionCrfRepository).findByStudyEventDefinitionIdAndStatusId(3, Status.AVAILABLE.getId());
    }

    @Test
    void findByStudyEventDefinitionIdAndCRFId_returnsMatchingBeanOrEmpty() {
        when(eventDefinitionCrfRepository.findByStudyEventDefinitionIdAndCrfId(3, 5))
                .thenReturn(Optional.of(eventDefinitionCrf(7, 3, 4, 5, Status.AVAILABLE.getId())));

        EventDefinitionCRFBean bean = adapter.findByStudyEventDefinitionIdAndCRFId(3, 5);

        assertEquals(7, bean.getId());
        verify(eventDefinitionCrfRepository).findByStudyEventDefinitionIdAndCrfId(3, 5);
    }

    @Test
    void findByStudyEventDefinitionIdAndCRFIdAndStudyId_usesCompositeRepositoryLookup() {
        when(eventDefinitionCrfRepository.findByStudyEventDefinitionIdAndCrfIdAndStudyId(3, 5, 4))
                .thenReturn(Optional.of(eventDefinitionCrf(7, 3, 4, 5, Status.AVAILABLE.getId())));

        EventDefinitionCRFBean bean = adapter.findByStudyEventDefinitionIdAndCRFIdAndStudyId(3, 5, 4);

        assertEquals(7, bean.getId());
        verify(eventDefinitionCrfRepository).findByStudyEventDefinitionIdAndCrfIdAndStudyId(3, 5, 4);
    }

    @Test
    void findAllSubmissionUriAndStudyId_usesSubmissionUrlRepositoryLookup() {
        when(eventDefinitionCrfRepository.findBySubmissionUrlAndStudyId("public-form", 4))
                .thenReturn(List.of(eventDefinitionCrf(7, 3, 4, 5, Status.AVAILABLE.getId())));

        ArrayList<EventDefinitionCRFBean> beans = adapter.findAllSubmissionUriAndStudyId("public-form", 4);

        assertEquals(1, beans.size());
        verify(eventDefinitionCrfRepository).findBySubmissionUrlAndStudyId("public-form", 4);
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        EventDefinitionCrfEntity saved = eventDefinitionCrf(11, 3, 4, 5, Status.AVAILABLE.getId());
        when(eventDefinitionCrfRepository.save(argThat(e -> {
            assertEquals(3, e.getStudyEventDefinitionId());
            assertEquals(4, e.getStudyId());
            assertEquals(5, e.getCrfId());
            assertEquals(true, e.getRequiredCrf());
            assertEquals(true, e.getDoubleEntry());
            assertEquals(true, e.getRequireAllTextFilled());
            assertEquals(false, e.getDecisionConditions());
            assertEquals("NI,NA", e.getNullValues());
            assertEquals(6, e.getDefaultVersionId());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals(20, e.getOwnerId());
            assertEquals(21, e.getUpdateId());
            assertEquals(2, e.getOrdinal());
            assertEquals(true, e.getElectronicSignature());
            assertEquals(true, e.getHideCrf());
            assertEquals(SourceDataVerification.AllREQUIRED.getCode(), e.getSourceDataVerificationCode());
            assertEquals("6,8", e.getSelectedVersionIds());
            assertEquals(22, e.getParentId());
            assertEquals(true, e.getParticipantForm());
            assertEquals(true, e.getAllowAnonymousSubmission());
            assertEquals("public-form", e.getSubmissionUrl());
            return e.getDateCreated() != null;
        }))).thenReturn(saved);

        EventDefinitionCRFBean input = new EventDefinitionCRFBean();
        input.setStudyEventDefinitionId(3);
        input.setStudyId(4);
        input.setCrfId(5);
        input.setRequiredCRF(true);
        input.setDoubleEntry(true);
        input.setRequireAllTextFilled(true);
        input.setDecisionCondition(false);
        input.setNullValues("NI,NA");
        input.setDefaultVersionId(6);
        input.setStatus(Status.AVAILABLE);
        input.setOwnerId(20);
        input.setUpdaterId(21);
        input.setOrdinal(2);
        input.setElectronicSignature(true);
        input.setHideCrf(true);
        input.setSourceDataVerification(SourceDataVerification.AllREQUIRED);
        input.setSelectedVersionIds("6,8");
        input.setParentId(22);
        input.setParticipantForm(true);
        input.setAllowAnonymousSubmission(true);
        input.setSubmissionUrl("public-form");

        EventDefinitionCRFBean result = (EventDefinitionCRFBean) adapter.create(input);

        assertEquals(11, result.getId());
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        Date now = new Date();
        HashMap row = new HashMap();
        row.put("event_definition_crf_id", 50);
        row.put("study_event_definition_id", 51);
        row.put("study_id", 52);
        row.put("crf_id", 53);
        row.put("required_crf", true);
        row.put("double_entry", true);
        row.put("require_all_text_filled", true);
        row.put("decision_conditions", false);
        row.put("null_values", "NI");
        row.put("default_version_id", 54);
        row.put("status_id", Status.AVAILABLE.getId());
        row.put("owner_id", 55);
        row.put("date_created", now);
        row.put("date_updated", now);
        row.put("update_id", 56);
        row.put("ordinal", 3);
        row.put("electronic_signature", true);
        row.put("hide_crf", true);
        row.put("source_data_verification_code", SourceDataVerification.NOTREQUIRED.getCode());
        row.put("selected_version_ids", "54,57");
        row.put("parent_id", 58);
        row.put("participant_form", true);
        row.put("allow_anonymous_submission", true);
        row.put("submission_url", "row-form");

        EventDefinitionCRFBean bean = (EventDefinitionCRFBean) adapter.getEntityFromHashMap(row);

        assertEquals(50, bean.getId());
        assertEquals(51, bean.getStudyEventDefinitionId());
        assertEquals(52, bean.getStudyId());
        assertEquals(53, bean.getCrfId());
        assertTrue(bean.isRequiredCRF());
        assertTrue(bean.isDoubleEntry());
        assertTrue(bean.isRequireAllTextFilled());
        assertEquals(false, bean.isDecisionCondition());
        assertEquals("NI", bean.getNullValues());
        assertEquals(54, bean.getDefaultVersionId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(55, bean.getOwnerId());
        assertEquals(56, bean.getUpdaterId());
        assertEquals(3, bean.getOrdinal());
        assertTrue(bean.isElectronicSignature());
        assertTrue(bean.isHideCrf());
        assertEquals(SourceDataVerification.NOTREQUIRED, bean.getSourceDataVerification());
        assertEquals("54,57", bean.getSelectedVersionIds());
        assertEquals(58, bean.getParentId());
        assertTrue(bean.isParticipantForm());
        assertTrue(bean.isAllowAnonymousSubmission());
        assertEquals("row-form", bean.getSubmissionUrl());
    }

    private static EventDefinitionCrfEntity eventDefinitionCrf(
            Integer id, Integer definitionId, Integer studyId, Integer crfId, Integer statusId) {
        EventDefinitionCrfEntity entity = new EventDefinitionCrfEntity();
        entity.setEventDefinitionCrfId(id);
        entity.setStudyEventDefinitionId(definitionId);
        entity.setStudyId(studyId);
        entity.setCrfId(crfId);
        entity.setStatusId(statusId);
        return entity;
    }
}
