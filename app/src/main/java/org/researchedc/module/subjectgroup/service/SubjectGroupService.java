package org.researchedc.module.subjectgroup.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.subjectgroup.entity.StudyGroupClassEntity;
import org.researchedc.module.subjectgroup.entity.StudyGroupEntity;
import org.researchedc.module.subjectgroup.repository.StudyGroupClassRepository;
import org.researchedc.module.subjectgroup.repository.StudyGroupRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SubjectGroupService {

    private final StudyGroupClassRepository classRepository;
    private final StudyGroupRepository groupRepository;
    private final AuditService auditService;
    private final CurrentStudyAccessService currentStudyAccessService;

    public SubjectGroupService(StudyGroupClassRepository classRepository,
                               StudyGroupRepository groupRepository,
                               AuditService auditService,
                               CurrentStudyAccessService currentStudyAccessService) {
        this.classRepository = classRepository;
        this.groupRepository = groupRepository;
        this.auditService = auditService;
        this.currentStudyAccessService = currentStudyAccessService;
    }

    public List<StudyGroupClassEntity> listClassesByStudy(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return classRepository.findByStudyId(studyId);
    }

    public StudyGroupClassEntity getClassById(Integer id, Integer currentUserId) {
        StudyGroupClassEntity entity = classRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Study group class not found: " + id));
        requireReadAccess(currentUserId, entity.getStudyId());
        return entity;
    }

    public List<StudyGroupEntity> getGroupsByClassId(Integer classId, Integer currentUserId) {
        StudyGroupClassEntity entity = classRepository.findById(classId)
                .orElseThrow(() -> new NoSuchElementException("Study group class not found: " + classId));
        requireReadAccess(currentUserId, entity.getStudyId());
        return groupRepository.findByStudyGroupClassId(classId);
    }

    @Transactional
    public StudyGroupClassEntity createClass(String name, Integer studyId,
                                              String subjectAssignment, Integer ownerId,
                                              Integer currentUserId) {
        requireWriteAccess(currentUserId, studyId);
        StudyGroupClassEntity entity = new StudyGroupClassEntity();
        entity.setName(name);
        entity.setStudyId(studyId);
        entity.setSubjectAssignment(subjectAssignment);
        entity.setOwnerId(ownerId);
        entity.setStatusId(1);
        entity.setDateCreated(LocalDateTime.now());
        return classRepository.save(entity);
    }

    @Transactional
    public StudyGroupClassEntity updateClass(Integer id, String name, String subjectAssignment,
                                             Integer currentUserId) {
        StudyGroupClassEntity entity = classRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Study group class not found: " + id));
        requireWriteAccess(currentUserId, entity.getStudyId());
        entity.setName(name);
        entity.setSubjectAssignment(subjectAssignment);
        entity.setDateUpdated(LocalDateTime.now());
        return classRepository.save(entity);
    }

    @Transactional
    public StudyGroupClassEntity removeClass(Integer id, Integer userId, Integer currentUserId) {
        StudyGroupClassEntity entity = classRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Study group class not found: " + id));
        requireWriteAccess(currentUserId, entity.getStudyId());
        entity.setStatusId(5);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(userId);
        StudyGroupClassEntity saved = classRepository.save(entity);

        auditService.recordAudit(
                saved.getStudyId(), AuditEventType.DELETE, "StudyGroupClass",
                saved.getStudyGroupClassId().longValue(), saved.getName(),
                null, null, userId, "Class removed (status=5)", "subjectgroup");
        return saved;
    }

    @Transactional
    public StudyGroupClassEntity restoreClass(Integer id, Integer userId, Integer currentUserId) {
        StudyGroupClassEntity entity = classRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Study group class not found: " + id));
        requireWriteAccess(currentUserId, entity.getStudyId());
        entity.setStatusId(1);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(userId);
        StudyGroupClassEntity saved = classRepository.save(entity);

        auditService.recordAudit(
                saved.getStudyId(), AuditEventType.UPDATE, "StudyGroupClass",
                saved.getStudyGroupClassId().longValue(), saved.getName(),
                null, null, userId, "Class restored (status=1)", "subjectgroup");
        return saved;
    }

    @Transactional
    public StudyGroupEntity createGroup(String name, String description,
                                         Integer classId, Integer ownerId,
                                         Integer currentUserId) {
        StudyGroupClassEntity groupClass = classRepository.findById(classId)
                .orElseThrow(() -> new NoSuchElementException("Study group class not found: " + classId));
        requireWriteAccess(currentUserId, groupClass.getStudyId());
        StudyGroupEntity entity = new StudyGroupEntity();
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setStudyGroupClassId(classId);
        return groupRepository.save(entity);
    }

    @Transactional
    public StudyGroupEntity updateGroup(Integer id, String name, String description,
                                        Integer currentUserId) {
        StudyGroupEntity entity = groupRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Study group not found: " + id));
        StudyGroupClassEntity groupClass = classRepository.findById(entity.getStudyGroupClassId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Study group class not found: " + entity.getStudyGroupClassId()));
        requireWriteAccess(currentUserId, groupClass.getStudyId());
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        return groupRepository.save(entity);
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
