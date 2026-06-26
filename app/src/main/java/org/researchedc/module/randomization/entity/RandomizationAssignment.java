package org.researchedc.module.randomization.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.researchedc.module.randomization.enums.AssignmentStatus;

@Entity
@Table(name = "randomization_assignment")
public class RandomizationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private RandomizationScheme scheme;

    @Column(name = "study_subject_id", nullable = false)
    private Integer studySubjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arm_id", nullable = false)
    private RandomizationArm arm;

    @Column(name = "stratum_path", length = 512)
    private String stratumPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private RandomizationBlock block;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    @Column(name = "assigned_by")
    private Integer assignedBy;

    @PrePersist
    protected void onCreate() {
        assignedDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RandomizationScheme getScheme() { return scheme; }
    public void setScheme(RandomizationScheme scheme) { this.scheme = scheme; }

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer studySubjectId) { this.studySubjectId = studySubjectId; }

    public RandomizationArm getArm() { return arm; }
    public void setArm(RandomizationArm arm) { this.arm = arm; }

    public String getStratumPath() { return stratumPath; }
    public void setStratumPath(String stratumPath) { this.stratumPath = stratumPath; }

    public RandomizationBlock getBlock() { return block; }
    public void setBlock(RandomizationBlock block) { this.block = block; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDateTime getAssignedDate() { return assignedDate; }
    public Integer getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Integer assignedBy) { this.assignedBy = assignedBy; }
}
