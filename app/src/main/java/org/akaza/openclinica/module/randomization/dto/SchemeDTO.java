package org.akaza.openclinica.module.randomization.dto;

import java.util.List;
import org.akaza.openclinica.module.randomization.enums.RandomizationAlgorithm;
import org.akaza.openclinica.module.randomization.enums.SchemeStatus;

public class SchemeDTO {

    private Long id;
    private Integer studyId;
    private String name;
    private RandomizationAlgorithm algorithm;
    private SchemeStatus status;
    private Long seed;
    private Integer minBlockSize;
    private Integer maxBlockSize;
    private List<ArmDTO> arms;
    private List<StratumDTO> stratifications;
    private long totalAssigned;
    private long totalArms;

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

    public Long getSeed() { return seed; }
    public void setSeed(Long seed) { this.seed = seed; }

    public Integer getMinBlockSize() { return minBlockSize; }
    public void setMinBlockSize(Integer minBlockSize) { this.minBlockSize = minBlockSize; }

    public Integer getMaxBlockSize() { return maxBlockSize; }
    public void setMaxBlockSize(Integer maxBlockSize) { this.maxBlockSize = maxBlockSize; }

    public List<ArmDTO> getArms() { return arms; }
    public void setArms(List<ArmDTO> arms) { this.arms = arms; }

    public List<StratumDTO> getStratifications() { return stratifications; }
    public void setStratifications(List<StratumDTO> stratifications) { this.stratifications = stratifications; }

    public long getTotalAssigned() { return totalAssigned; }
    public void setTotalAssigned(long totalAssigned) { this.totalAssigned = totalAssigned; }

    public long getTotalArms() { return totalArms; }
    public void setTotalArms(long totalArms) { this.totalArms = totalArms; }
}
