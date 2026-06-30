package org.researchedc.module.subjectgroup.controller;

import java.util.List;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.subjectgroup.dto.CreateGroupClassRequest;
import org.researchedc.module.subjectgroup.dto.CreateGroupRequest;
import org.researchedc.module.subjectgroup.dto.SubjectGroupClassDTO;
import org.researchedc.module.subjectgroup.dto.SubjectGroupDTO;
import org.researchedc.module.subjectgroup.entity.StudyGroupClassEntity;
import org.researchedc.module.subjectgroup.entity.StudyGroupEntity;
import org.researchedc.module.subjectgroup.service.SubjectGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subject-groups")
public class SubjectGroupController {

    private final SubjectGroupService subjectGroupService;
    private final CurrentUserUtils currentUserUtils;

    public SubjectGroupController(SubjectGroupService subjectGroupService, CurrentUserUtils currentUserUtils) {
        this.subjectGroupService = subjectGroupService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/classes")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<SubjectGroupClassDTO>> listClasses(@RequestParam int studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(subjectGroupService.listClassesByStudy(studyId, currentUserId)
                .stream().map(this::toClassDto).toList());
    }

    @GetMapping("/classes/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<SubjectGroupClassDTO> getClass(@PathVariable int id) {
        try {
            Integer currentUserId = currentUserUtils.getCurrentUserId();
            SubjectGroupClassDTO dto = toClassDto(subjectGroupService.getClassById(id, currentUserId));
            dto.setGroups(subjectGroupService.getGroupsByClassId(id, currentUserId)
                    .stream().map(this::toGroupDto).toList());
            return ResponseEntity.ok(dto);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/classes")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<SubjectGroupClassDTO> createClass(@RequestBody CreateGroupClassRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        StudyGroupClassEntity entity = subjectGroupService.createClass(
                request.getName(), request.getStudyId(), request.getSubjectAssignment(), ownerId, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toClassDto(entity));
    }

    @PutMapping("/classes/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<SubjectGroupClassDTO> updateClass(
            @PathVariable int id, @RequestBody CreateGroupClassRequest request) {
        try {
            Integer currentUserId = currentUserUtils.getCurrentUserId();
            StudyGroupClassEntity entity = subjectGroupService.updateClass(
                    id, request.getName(), request.getSubjectAssignment(), currentUserId);
            return ResponseEntity.ok(toClassDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/classes/{classId}/groups")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<SubjectGroupDTO>> listGroups(@PathVariable int classId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(subjectGroupService.getGroupsByClassId(classId, currentUserId)
                .stream().map(this::toGroupDto).toList());
    }

    @PostMapping("/classes/{classId}/groups")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<SubjectGroupDTO> createGroup(
            @PathVariable int classId, @RequestBody CreateGroupRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        StudyGroupEntity entity = subjectGroupService.createGroup(
                request.getName(), request.getDescription(), classId, ownerId, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toGroupDto(entity));
    }

    @PutMapping("/groups/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<SubjectGroupDTO> updateGroup(
            @PathVariable int id, @RequestBody CreateGroupRequest request) {
        try {
            Integer currentUserId = currentUserUtils.getCurrentUserId();
            StudyGroupEntity entity = subjectGroupService.updateGroup(
                    id, request.getName(), request.getDescription(), currentUserId);
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
        dto.setOwnerId(entity.getOwnerId());
        dto.setDateCreated(entity.getDateCreated());
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
