package org.researchedc.module.sdv.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.researchedc.module.sdv.dto.UpdateSdvReviewRequest;
import org.researchedc.module.sdv.entity.SdvReviewEntity;
import org.researchedc.module.sdv.enums.SdvStatus;
import org.researchedc.module.sdv.repository.SdvReviewRepository;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class SdvServiceTest {

    @Mock private SdvReviewRepository sdvReviewRepository;
    @Mock private EventCrfRepository eventCrfRepository;
    @Mock private StudySubjectRepository studySubjectRepository;
    @Mock private CurrentStudyAccessService currentStudyAccessService;
    @Mock private AuditService auditService;

    private SdvService service;

    @BeforeEach
    void setUp() {
        service = new SdvService(sdvReviewRepository, eventCrfRepository, studySubjectRepository,
                currentStudyAccessService, auditService);
    }

    @Test
    void createOrGetReview_createsPendingReviewForEventCrfStudy() {
        when(eventCrfRepository.findById(7)).thenReturn(Optional.of(eventCrf()));
        when(studySubjectRepository.findById(100)).thenReturn(Optional.of(studySubject()));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(sdvReviewRepository.findByEventCrfId(7)).thenReturn(Optional.empty());
        when(sdvReviewRepository.save(any(SdvReviewEntity.class))).thenAnswer(invocation -> {
            SdvReviewEntity review = invocation.getArgument(0);
            review.setId(1L);
            review.setCreatedDate(LocalDateTime.now());
            return review;
        });

        var result = service.createOrGetReview(7, 42);

        assertEquals(1L, result.getId());
        assertEquals(10, result.getStudyId());
        assertEquals(SdvStatus.PENDING, result.getStatus());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.CREATE), eq("sdv_review"),
                eq(1L), eq("eventCrf:7"), isNull(), isNull(), eq(42),
                eq("SDV review queued"), eq("sdv"));
    }

    @Test
    void updateReview_marksEventCrfVerified() {
        SdvReviewEntity review = review(SdvStatus.PENDING);
        UpdateSdvReviewRequest request = new UpdateSdvReviewRequest();
        request.setStatus(SdvStatus.VERIFIED);
        request.setReviewNotes("Verified against source");
        when(sdvReviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(sdvReviewRepository.save(review)).thenReturn(review);
        when(eventCrfRepository.findById(7)).thenReturn(Optional.of(eventCrf()));

        var result = service.updateReview(1L, request, 42);

        assertEquals(SdvStatus.VERIFIED, result.getStatus());
        verify(eventCrfRepository).save(argThat(eventCrf -> Boolean.TRUE.equals(eventCrf.getSdvStatus())
                && Integer.valueOf(42).equals(eventCrf.getSdvUpdateId())));
    }

    @Test
    void listReviews_whenNoReadAccess_denies() {
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.listReviews(10, null, 42));
        verifyNoInteractions(sdvReviewRepository);
    }

    @Test
    void listReviews_filtersByStatus() {
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(true);
        when(sdvReviewRepository.findByStudyIdAndStatusOrderByCreatedDateDesc(10, SdvStatus.REQUIRES_CHANGES))
                .thenReturn(List.of(review(SdvStatus.REQUIRES_CHANGES)));

        var result = service.listReviews(10, SdvStatus.REQUIRES_CHANGES, 42);

        assertEquals(1, result.size());
        assertEquals(SdvStatus.REQUIRES_CHANGES, result.get(0).getStatus());
    }

    private static EventCrfEntity eventCrf() {
        EventCrfEntity eventCrf = new EventCrfEntity();
        eventCrf.setEventCrfId(7);
        eventCrf.setStudySubjectId(100);
        return eventCrf;
    }

    private static StudySubjectEntity studySubject() {
        StudySubjectEntity studySubject = new StudySubjectEntity();
        studySubject.setStudySubjectId(100);
        studySubject.setStudyId(10);
        return studySubject;
    }

    private static SdvReviewEntity review(SdvStatus status) {
        SdvReviewEntity review = new SdvReviewEntity();
        review.setId(1L);
        review.setStudyId(10);
        review.setEventCrfId(7);
        review.setStudySubjectId(100);
        review.setStatus(status);
        review.setCreatedDate(LocalDateTime.now());
        return review;
    }
}
