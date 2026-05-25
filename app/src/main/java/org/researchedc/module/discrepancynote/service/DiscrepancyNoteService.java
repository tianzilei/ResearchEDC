package org.researchedc.module.discrepancynote.service;

import java.time.LocalDateTime;
import java.util.List;
import org.researchedc.module.discrepancynote.entity.DiscrepancyNoteEntity;
import org.researchedc.module.discrepancynote.repository.DiscrepancyNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DiscrepancyNoteService {

    private final DiscrepancyNoteRepository discrepancyNoteRepository;

    public DiscrepancyNoteService(DiscrepancyNoteRepository discrepancyNoteRepository) {
        this.discrepancyNoteRepository = discrepancyNoteRepository;
    }

    public List<DiscrepancyNoteEntity> listByStudy(Integer studyId) {
        return discrepancyNoteRepository.findByStudyId(studyId);
    }

    public DiscrepancyNoteEntity getById(Integer id) {
        return discrepancyNoteRepository.findById(id)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "DiscrepancyNote not found: " + id));
    }

    @Transactional
    public DiscrepancyNoteEntity create(String description, Integer discrepancyNoteTypeId,
                                         Integer resolutionStatusId, String detailedNotes,
                                         Integer ownerId, Integer parentDnId,
                                         String entityType, Integer entityId,
                                         Integer studyId, Integer assignedUserId) {
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
    public DiscrepancyNoteEntity resolveNote(Integer id) {
        DiscrepancyNoteEntity entity = getById(id);
        entity.setResolutionStatusId(5);
        return discrepancyNoteRepository.save(entity);
    }

    public long countOpenNotes() {
        return discrepancyNoteRepository.countByResolutionStatusIdNot(3);
    }
}
