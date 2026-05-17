package org.akaza.openclinica.module.randomization.dto;

import java.util.Map;

public class RandomizeRequest {

    private Long schemeId;
    private Integer studySubjectId;
    private Integer assignedBy;
    private Map<String, String> stratumValues;

    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

    public Integer getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Integer assignedBy) { this.assignedBy = assignedBy; }

    public Map<String, String> getStratumValues() { return stratumValues; }
    public void setStratumValues(Map<String, String> stratumValues) { this.stratumValues = stratumValues; }
}
