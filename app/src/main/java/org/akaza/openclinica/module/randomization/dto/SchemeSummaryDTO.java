package org.akaza.openclinica.module.randomization.dto;

import org.akaza.openclinica.module.randomization.enums.RandomizationAlgorithm;
import org.akaza.openclinica.module.randomization.enums.SchemeStatus;

public class SchemeSummaryDTO {

    private Long id;
    private Integer studyId;
    private String name;
    private RandomizationAlgorithm algorithm;
    private SchemeStatus status;
    private long totalAssigned;
    private int totalArms;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RandomizationAlgorithm getAlgorithm() { return algorithm; }
    public void setAlgorithm(RandomizationAlgorithm algorithm) { this.algorithm = algorithm; }

    public SchemeStatus getStatus() { return status; }
    public void setStatus(SchemeStatus status) { this.status = status; }

    public long getTotalAssigned() { return totalAssigned; }
    public void setTotalAssigned(long totalAssigned) { this.totalAssigned = totalAssigned; }

    public int getTotalArms() { return totalArms; }
    public void setTotalArms(int totalArms) { this.totalArms = totalArms; }
}
