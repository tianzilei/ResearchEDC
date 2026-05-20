package org.researchedc.module.randomization.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.researchedc.module.randomization.enums.RandomizationAlgorithm;
import org.researchedc.module.randomization.enums.SchemeStatus;

@Entity
@Table(name = "randomization_scheme")
public class RandomizationScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_id", nullable = false)
    private Integer studyId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RandomizationAlgorithm algorithm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchemeStatus status = SchemeStatus.DRAFT;

    private Long seed;

    @Column(name = "min_block_size")
    private Integer minBlockSize;

    @Column(name = "max_block_size")
    private Integer maxBlockSize;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNumber")
    private List<RandomizationArm> arms = new ArrayList<>();

    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNumber")
    private List<RandomizationStratum> stratifications = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (seed == null) {
            seed = System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

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

    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }

    public List<RandomizationArm> getArms() { return arms; }
    public void setArms(List<RandomizationArm> arms) { this.arms = arms; }

    public List<RandomizationStratum> getStratifications() { return stratifications; }
    public void setStratifications(List<RandomizationStratum> stratifications) { this.stratifications = stratifications; }
}
