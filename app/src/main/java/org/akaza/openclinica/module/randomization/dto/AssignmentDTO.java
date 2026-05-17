package org.akaza.openclinica.module.randomization.dto;

import java.time.LocalDateTime;
import org.akaza.openclinica.module.randomization.enums.AssignmentStatus;

public class AssignmentDTO {

    private Long id;
    private Long schemeId;
    private Integer studySubjectId;
    private String subjectKey;
    private Long armId;
    private String armName;
    private String stratumPath;
    private AssignmentStatus status;
    private LocalDateTime assignedDate;
    private Integer assignedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

    public String getSubjectKey() { return subjectKey; }
    public void setSubjectKey(String subjectKey) { this.subjectKey = subjectKey; }

    public Long getArmId() { return armId; }
    public void setArmId(Long armId) { this.armId = armId; }

    public String getArmName() { return armName; }
    public void setArmName(String armName) { this.armName = armName; }

    public String getStratumPath() { return stratumPath; }
    public void setStratumPath(String stratumPath) { this.stratumPath = stratumPath; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }

    public Integer getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Integer assignedBy) { this.assignedBy = assignedBy; }
}
