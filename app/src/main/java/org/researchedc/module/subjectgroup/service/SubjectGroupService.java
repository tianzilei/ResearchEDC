package org.researchedc.module.subjectgroup.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.subjectgroup.entity.StudyGroupClassEntity;
import org.researchedc.module.subjectgroup.entity.StudyGroupEntity;
import org.researchedc.module.subjectgroup.repository.StudyGroupClassRepository;
import org.researchedc.module.subjectgroup.repository.StudyGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SubjectGroupService {

    private final StudyGroupClassRepository classRepository;
    private final StudyGroupRepository groupRepository;
    private final AuditService auditService;

    public SubjectGroupService(StudyGroupClassRepository classRepository,
                               StudyGroupRepository groupRepository,
                               AuditService auditService) {
        this.classRepository = classRepository;
        this.groupRepository = groupRepository;
        this.auditService = auditService;
    }

    public List<StudyGroupClassEntity> listClassesByStudy(Integer studyId) {
        return classRepository.findByStudyId(studyId);
    }

    public StudyGroupClassEntity getClassById(Integer id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Study group class not found: " + id));
    }

    public List<StudyGroupEntity> getGroupsByClassId(Integer classId) {
        return groupRepository.findByStudyGroupClassId(classId);
    }

    @Transactional
    public StudyGroupClassEntity createClass(String name, Integer studyId,
                                              String subjectAssignment, Integer ownerId) {
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
    public StudyGroupClassEntity updateClass(Integer id, String name, String subjectAssignment) {
        StudyGroupClassEntity entity = classRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Study group class not found: " + id));
        entity.setName(name);
        entity.setSubjectAssignment(subjectAssignment);
        entity.setDateUpdated(LocalDateTime.now());
        return classRepository.save(entity);
    }

    @Transactional
    public StudyGroupClassEntity removeClass(Integer id, Integer userId) {
        StudyGroupClassEntity entity = classRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Study group class not found: " + id));
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
    public StudyGroupClassEntity restoreClass(Integer id, Integer userId) {
        StudyGroupClassEntity entity = classRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Study group class not found: " + id));
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
                                         Integer classId, Integer ownerId) {
        StudyGroupEntity entity = new StudyGroupEntity();
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setStudyGroupClassId(classId);
        return groupRepository.save(entity);
    }

    @Transactional
    public StudyGroupEntity updateGroup(Integer id, String name, String description) {
        StudyGroupEntity entity = groupRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Study group not found: " + id));
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        return groupRepository.save(entity);
    }
}
