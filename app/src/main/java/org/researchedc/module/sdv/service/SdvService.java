package org.researchedc.module.sdv.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.researchedc.module.sdv.dto.SdvReviewDTO;
import org.researchedc.module.sdv.dto.UpdateSdvReviewRequest;
import org.researchedc.module.sdv.entity.SdvReviewEntity;
import org.researchedc.module.sdv.enums.SdvStatus;
import org.researchedc.module.sdv.repository.SdvReviewRepository;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SdvService {

    private final SdvReviewRepository sdvReviewRepository;
    private final EventCrfRepository eventCrfRepository;
    private final StudySubjectRepository studySubjectRepository;
    private final CurrentStudyAccessService currentStudyAccessService;
    private final AuditService auditService;

    public SdvService(SdvReviewRepository sdvReviewRepository,
                      EventCrfRepository eventCrfRepository,
                      StudySubjectRepository studySubjectRepository,
                      CurrentStudyAccessService currentStudyAccessService,
                      AuditService auditService) {
        this.sdvReviewRepository = sdvReviewRepository;
        this.eventCrfRepository = eventCrfRepository;
        this.studySubjectRepository = studySubjectRepository;
        this.currentStudyAccessService = currentStudyAccessService;
        this.auditService = auditService;
    }

    public List<SdvReviewDTO> listReviews(Integer studyId, SdvStatus status, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        List<SdvReviewEntity> reviews = status == null
                ? sdvReviewRepository.findByStudyIdOrderByCreatedDateDesc(studyId)
                : sdvReviewRepository.findByStudyIdAndStatusOrderByCreatedDateDesc(studyId, status);
        return reviews.stream().map(this::toDto).toList();
    }

    @Transactional
    public SdvReviewDTO createOrGetReview(Integer eventCrfId, Integer currentUserId) {
        EventCrfEntity eventCrf = findEventCrf(eventCrfId);
        Integer studyId = resolveStudyId(eventCrf);
        requireWriteAccess(currentUserId, studyId);
        return sdvReviewRepository.findByEventCrfId(eventCrfId)
                .map(this::toDto)
                .orElseGet(() -> toDto(createReview(eventCrf, studyId, currentUserId)));
    }

    @Transactional
    public SdvReviewDTO updateReview(Long reviewId, UpdateSdvReviewRequest request, Integer currentUserId) {
        SdvReviewEntity review = sdvReviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("SDV review not found: " + reviewId));
        requireWriteAccess(currentUserId, review.getStudyId());
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("status is required");
        }
        review.setStatus(request.getStatus());
        review.setReviewNotes(request.getReviewNotes() == null ? "" : request.getReviewNotes());
        review.setReviewedBy(currentUserId);
        review.setReviewedDate(LocalDateTime.now());
        review.setUpdatedDate(LocalDateTime.now());
        SdvReviewEntity saved = sdvReviewRepository.save(review);

        EventCrfEntity eventCrf = findEventCrf(saved.getEventCrfId());
        eventCrf.setSdvStatus(saved.getStatus() == SdvStatus.VERIFIED);
        eventCrf.setSdvUpdateId(currentUserId);
        eventCrf.setDateUpdated(LocalDateTime.now());
        eventCrfRepository.save(eventCrf);

        record(saved.getStudyId(), AuditEventType.UPDATE, "sdv_review", saved.getId(),
                "eventCrf:" + saved.getEventCrfId(), currentUserId, "SDV status: " + saved.getStatus());
        return toDto(saved);
    }

    private SdvReviewEntity createReview(EventCrfEntity eventCrf, Integer studyId, Integer currentUserId) {
        SdvReviewEntity review = new SdvReviewEntity();
        review.setStudyId(studyId);
        review.setEventCrfId(eventCrf.getEventCrfId());
        review.setStudySubjectId(eventCrf.getStudySubjectId());
        review.setStatus(Boolean.TRUE.equals(eventCrf.getSdvStatus()) ? SdvStatus.VERIFIED : SdvStatus.PENDING);
        SdvReviewEntity saved = sdvReviewRepository.save(review);
        record(studyId, AuditEventType.CREATE, "sdv_review", saved.getId(),
                "eventCrf:" + saved.getEventCrfId(), currentUserId, "SDV review queued");
        return saved;
    }

    private EventCrfEntity findEventCrf(Integer eventCrfId) {
        if (eventCrfId == null) {
            throw new IllegalArgumentException("eventCrfId is required");
        }
        return eventCrfRepository.findById(eventCrfId)
                .orElseThrow(() -> new NoSuchElementException("Event CRF not found: " + eventCrfId));
    }

    private Integer resolveStudyId(EventCrfEntity eventCrf) {
        StudySubjectEntity studySubject = studySubjectRepository.findById(eventCrf.getStudySubjectId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Study subject not found: " + eventCrf.getStudySubjectId()));
        return studySubject.getStudyId();
    }

    private SdvReviewDTO toDto(SdvReviewEntity entity) {
        SdvReviewDTO dto = new SdvReviewDTO();
        dto.setId(entity.getId());
        dto.setStudyId(entity.getStudyId());
        dto.setEventCrfId(entity.getEventCrfId());
        dto.setStudySubjectId(entity.getStudySubjectId());
        dto.setStatus(entity.getStatus());
        dto.setReviewNotes(entity.getReviewNotes());
        dto.setReviewedBy(entity.getReviewedBy());
        dto.setReviewedDate(entity.getReviewedDate());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
    }

    private void requireReadAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canReadStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have read access to this study");
        }
    }

    private void requireWriteAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canWriteStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have write access to this study");
        }
    }

    private void record(Integer studyId, AuditEventType eventType, String entityType, Long entityId,
                        String entityLabel, Integer performedBy, String details) {
        auditService.recordAudit(studyId, eventType, entityType, entityId, entityLabel,
                null, null, performedBy, details, "sdv");
    }
}
