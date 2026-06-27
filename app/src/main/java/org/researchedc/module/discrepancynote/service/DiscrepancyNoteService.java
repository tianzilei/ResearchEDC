package org.researchedc.module.discrepancynote.service;

import java.time.LocalDateTime;
import java.util.List;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.discrepancynote.entity.DiscrepancyNoteEntity;
import org.researchedc.module.discrepancynote.repository.DiscrepancyNoteRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DiscrepancyNoteService {

    private final DiscrepancyNoteRepository discrepancyNoteRepository;
    private final CurrentStudyAccessService currentStudyAccessService;

    public DiscrepancyNoteService(DiscrepancyNoteRepository discrepancyNoteRepository,
                                  CurrentStudyAccessService currentStudyAccessService) {
        this.discrepancyNoteRepository = discrepancyNoteRepository;
        this.currentStudyAccessService = currentStudyAccessService;
    }

    public List<DiscrepancyNoteEntity> listByStudy(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return discrepancyNoteRepository.findByStudyId(studyId);
    }

    public List<DiscrepancyNoteEntity> listByEventCrf(Integer eventCrfId, Integer currentUserId) {
        return java.util.stream.Stream.concat(
                        discrepancyNoteRepository
                                .findByEntityTypeAndEntityIdAndParentDnIdIsNull("eventCrf", eventCrfId)
                                .stream(),
                        discrepancyNoteRepository
                                .findByEntityTypeAndEntityIdAndParentDnIdIsNull("itemData", eventCrfId)
                                .stream())
                .filter(note -> currentStudyAccessService.canReadStudy(currentUserId, note.getStudyId()))
                .toList();
    }

    public DiscrepancyNoteEntity getById(Integer id, Integer currentUserId) {
        DiscrepancyNoteEntity entity = discrepancyNoteRepository.findById(id)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "DiscrepancyNote not found: " + id));
        requireReadAccess(currentUserId, entity.getStudyId());
        return entity;
    }

    @Transactional
    public DiscrepancyNoteEntity create(String description, Integer discrepancyNoteTypeId,
                                         Integer resolutionStatusId, String detailedNotes,
                                         Integer ownerId, Integer parentDnId,
                                         String entityType, Integer entityId,
                                         Integer studyId, Integer assignedUserId,
                                         Integer currentUserId) {
        requireWriteAccess(currentUserId, studyId);
        DiscrepancyNoteEntity entity = new DiscrepancyNoteEntity();
        entity.setDescription(description);
        entity.setDiscrepancyNoteTypeId(discrepancyNoteTypeId);
        entity.setResolutionStatusId(resolutionStatusId);
        entity.setDetailedNotes(detailedNotes);
        entity.setOwnerId(ownerId);
        entity.setParentDnId(parentDnId);
        entity.setEntityType(entityType);
        entity.setEntityId(entityId);
        entity.setStudyId(studyId);
        entity.setAssignedUserId(assignedUserId);
        entity.setDateCreated(LocalDateTime.now());
        return discrepancyNoteRepository.save(entity);
    }

    @Transactional
    public DiscrepancyNoteEntity resolveNote(Integer id, Integer currentUserId) {
        DiscrepancyNoteEntity entity = discrepancyNoteRepository.findById(id)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "DiscrepancyNote not found: " + id));
        requireWriteAccess(currentUserId, entity.getStudyId());
        entity.setResolutionStatusId(5);
        return discrepancyNoteRepository.save(entity);
    }

    public long countOpenNotes() {
        return discrepancyNoteRepository.countByResolutionStatusIdNot(3);
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
}
