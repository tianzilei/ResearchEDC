package org.researchedc.module.legacy.controller;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyGroupBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;
import org.researchedc.bean.core.GroupClassType;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.managestudy.StudyGroupClassDAO;
import org.researchedc.dao.managestudy.StudyGroupDAO;
import org.researchedc.module.legacy.dto.SubjectGroupClassDTO;
import org.researchedc.module.legacy.dto.SubjectGroupDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    private final StudyGroupClassDAO groupClassDao;
    private final StudyGroupDAO groupDao;
    private final StudyDAO studyDao;
    private final UserAccountDAO userAccountDao;

    public LegacySubjectGroupController(DataSource dataSource) {
        this.groupClassDao = new StudyGroupClassDAO(dataSource);
        this.groupDao = new StudyGroupDAO(dataSource);
        this.studyDao = new StudyDAO(dataSource);
        this.userAccountDao = new UserAccountDAO(dataSource);
    }

    @GetMapping("/classes")
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<SubjectGroupClassDTO>> listClasses(
            @RequestParam int studyId) {
        List<SubjectGroupClassDTO> result = new ArrayList<>();
        StudyBean study = (StudyBean) studyDao.findByPK(studyId);
        if (study == null || study.getId() == 0) {
            return ResponseEntity.ok(List.of());
        }
        for (Object obj : groupClassDao.findAllByStudy(study)) {
            result.add(toClassDto((StudyGroupClassBean) obj));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/classes/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<SubjectGroupClassDTO> getClass(@PathVariable int id) {
        StudyGroupClassBean bean = (StudyGroupClassBean) groupClassDao.findByPK(id);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toClassDto(bean));
    }

    @PostMapping("/classes")
    @SuppressWarnings("unchecked")
    public ResponseEntity<SubjectGroupClassDTO> createClass(
            @RequestBody SubjectGroupClassDTO dto) {
        StudyGroupClassBean bean = new StudyGroupClassBean();
        bean.setName(dto.getName());
        bean.setStudyId(dto.getStudyId());
        bean.setGroupClassTypeId(1);
        bean.setSubjectAssignment(dto.getSubjectAssignment() != null ? dto.getSubjectAssignment() : "optimal");
        UserAccountBean defaultUser = (UserAccountBean) userAccountDao.findByPK(1);
        bean.setOwner(defaultUser);
        bean = (StudyGroupClassBean) groupClassDao.create(bean);
        return ResponseEntity.ok(toClassDto(bean));
    }

    @PutMapping("/classes/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<SubjectGroupClassDTO> updateClass(
            @PathVariable int id, @RequestBody SubjectGroupClassDTO dto) {
        StudyGroupClassBean bean = (StudyGroupClassBean) groupClassDao.findByPK(id);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        bean.setName(dto.getName());
        bean.setSubjectAssignment(dto.getSubjectAssignment() != null ? dto.getSubjectAssignment() : "optimal");
        groupClassDao.update(bean);
        return ResponseEntity.ok(toClassDto(bean));
    }

    @GetMapping("/classes/{classId}/groups")
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<SubjectGroupDTO>> listGroups(@PathVariable int classId) {
        List<SubjectGroupDTO> result = new ArrayList<>();
        StudyGroupClassBean groupClass = (StudyGroupClassBean) groupClassDao.findByPK(classId);
        if (groupClass == null || groupClass.getId() == 0) {
            return ResponseEntity.ok(List.of());
        }
        for (Object obj : groupDao.findAllByGroupClass(groupClass)) {
            result.add(toGroupDto((StudyGroupBean) obj));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/classes/{classId}/groups")
    @SuppressWarnings("unchecked")
    public ResponseEntity<SubjectGroupDTO> createGroup(
            @PathVariable int classId, @RequestBody SubjectGroupDTO dto) {
        StudyGroupBean bean = new StudyGroupBean();
        bean.setName(dto.getName());
        bean.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        bean.setStudyGroupClassId(classId);
        UserAccountBean defaultUser = (UserAccountBean) userAccountDao.findByPK(1);
        bean.setOwner(defaultUser);
        bean = (StudyGroupBean) groupDao.create(bean);
        return ResponseEntity.ok(toGroupDto(bean));
    }

    @PutMapping("/groups/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<SubjectGroupDTO> updateGroup(
            @PathVariable int id, @RequestBody SubjectGroupDTO dto) {
        StudyGroupBean bean = (StudyGroupBean) groupDao.findByPK(id);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        bean.setName(dto.getName());
        bean.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        groupDao.update(bean);
        return ResponseEntity.ok(toGroupDto(bean));
    }

    private SubjectGroupClassDTO toClassDto(StudyGroupClassBean bean) {
        SubjectGroupClassDTO dto = new SubjectGroupClassDTO();
        dto.setGroupClassId(bean.getId());
        dto.setName(bean.getName());
        dto.setStudyId(bean.getStudyId());
        dto.setSubjectAssignment(bean.getSubjectAssignment());
        dto.setOwnerId(bean.getOwnerId());
        dto.setDateCreated(bean.getCreatedDate());

        GroupClassType type = GroupClassType.get(bean.getGroupClassTypeId());
        dto.setGroupClassType(type != null ? type.getName() : null);

        List<SubjectGroupDTO> groups = new ArrayList<>();
        for (Object obj : groupDao.findAllByGroupClass(bean)) {
            groups.add(toGroupDto((StudyGroupBean) obj));
        }
        dto.setGroups(groups);
        return dto;
    }

    private SubjectGroupDTO toGroupDto(StudyGroupBean bean) {
        SubjectGroupDTO dto = new SubjectGroupDTO();
        dto.setGroupId(bean.getId());
        dto.setName(bean.getName());
        dto.setDescription(bean.getDescription());
        dto.setGroupClassId(bean.getStudyGroupClassId());
        return dto;
    }
}
