package org.researchedc.module.legacy.controller;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.researchedc.module.legacy.dto.SubjectGroupClassDTO;
import org.researchedc.module.legacy.dto.SubjectGroupDTO;
import org.researchedc.module.subjectgroup.entity.StudyGroupClassEntity;
import org.researchedc.module.subjectgroup.entity.StudyGroupEntity;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.subjectgroup.service.SubjectGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/subject-groups")
public class LegacySubjectGroupController {

    private final SubjectGroupService subjectGroupService;
    private final CurrentUserUtils currentUserUtils;

    public LegacySubjectGroupController(SubjectGroupService subjectGroupService, CurrentUserUtils currentUserUtils) {
        this.subjectGroupService = subjectGroupService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/classes")
    public ResponseEntity<List<SubjectGroupClassDTO>> listClasses(
            @RequestParam int studyId) {
        List<SubjectGroupClassDTO> result = new ArrayList<>();
        for (StudyGroupClassEntity entity : subjectGroupService.listClassesByStudy(studyId)) {
            result.add(toClassDto(entity));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/classes/{id}")
    public ResponseEntity<SubjectGroupClassDTO> getClass(@PathVariable int id) {
        try {
            StudyGroupClassEntity entity = subjectGroupService.getClassById(id);
            SubjectGroupClassDTO dto = toClassDto(entity);
            List<SubjectGroupDTO> groups = new ArrayList<>();
            for (StudyGroupEntity g : subjectGroupService.getGroupsByClassId(id)) {
                groups.add(toGroupDto(g));
            }
            dto.setGroups(groups);
            return ResponseEntity.ok(dto);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/classes")
    public ResponseEntity<SubjectGroupClassDTO> createClass(
            @RequestBody SubjectGroupClassDTO dto) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        StudyGroupClassEntity entity = subjectGroupService.createClass(
                dto.getName(), dto.getStudyId(), dto.getSubjectAssignment(), ownerId);
        return ResponseEntity.ok(toClassDto(entity));
    }

    @PutMapping("/classes/{id}")
    public ResponseEntity<SubjectGroupClassDTO> updateClass(
            @PathVariable int id, @RequestBody SubjectGroupClassDTO dto) {
        try {
            StudyGroupClassEntity entity = subjectGroupService.updateClass(
                    id, dto.getName(), dto.getSubjectAssignment());
            return ResponseEntity.ok(toClassDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/classes/{classId}/groups")
    public ResponseEntity<List<SubjectGroupDTO>> listGroups(@PathVariable int classId) {
        List<SubjectGroupDTO> result = new ArrayList<>();
        for (StudyGroupEntity entity : subjectGroupService.getGroupsByClassId(classId)) {
            result.add(toGroupDto(entity));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/classes/{classId}/groups")
    public ResponseEntity<SubjectGroupDTO> createGroup(
            @PathVariable int classId, @RequestBody SubjectGroupDTO dto) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        StudyGroupEntity entity = subjectGroupService.createGroup(
                dto.getName(), dto.getDescription(), classId, ownerId);
        return ResponseEntity.ok(toGroupDto(entity));
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<SubjectGroupDTO> updateGroup(
            @PathVariable int id, @RequestBody SubjectGroupDTO dto) {
        try {
            StudyGroupEntity entity = subjectGroupService.updateGroup(
                    id, dto.getName(), dto.getDescription());
            return ResponseEntity.ok(toGroupDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private SubjectGroupClassDTO toClassDto(StudyGroupClassEntity entity) {
        SubjectGroupClassDTO dto = new SubjectGroupClassDTO();
        dto.setGroupClassId(entity.getStudyGroupClassId());
        dto.setName(entity.getName());
        dto.setStudyId(entity.getStudyId());
        dto.setSubjectAssignment(entity.getSubjectAssignment());
        dto.setOwnerId(entity.getOwnerId() != null ? entity.getOwnerId() : 0);
        if (entity.getDateCreated() != null) {
            dto.setDateCreated(Date.from(
                    entity.getDateCreated().atZone(ZoneId.systemDefault()).toInstant()));
        }
        return dto;
    }

    private SubjectGroupDTO toGroupDto(StudyGroupEntity entity) {
        SubjectGroupDTO dto = new SubjectGroupDTO();
        dto.setGroupId(entity.getStudyGroupId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setGroupClassId(entity.getStudyGroupClassId());
        return dto;
    }
}
