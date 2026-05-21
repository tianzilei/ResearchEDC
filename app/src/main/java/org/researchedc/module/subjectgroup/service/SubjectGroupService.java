package org.researchedc.module.subjectgroup.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

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

    public SubjectGroupService(StudyGroupClassRepository classRepository,
                               StudyGroupRepository groupRepository) {
        this.classRepository = classRepository;
        this.groupRepository = groupRepository;
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
