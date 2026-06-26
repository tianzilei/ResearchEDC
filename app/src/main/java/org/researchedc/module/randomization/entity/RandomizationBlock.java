package org.researchedc.module.randomization.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "randomization_block")
public class RandomizationBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private RandomizationScheme scheme;

    @Column(name = "block_size", nullable = false)
    private Integer blockSize;

    @Column(name = "block_index", nullable = false)
    private Integer blockIndex;

    @Column(name = "stratum_path", length = 512)
    private String stratumPath;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RandomizationScheme getScheme() { return scheme; }
    public void setScheme(RandomizationScheme scheme) { this.scheme = scheme; }

    public Integer getBlockSize() { return blockSize; }
    public void setBlockSize(Integer blockSize) { this.blockSize = blockSize; }

    public Integer getBlockIndex() { return blockIndex; }
    public void setBlockIndex(Integer blockIndex) { this.blockIndex = blockIndex; }

    public String getStratumPath() { return stratumPath; }
    public void setStratumPath(String stratumPath) { this.stratumPath = stratumPath; }

    public LocalDateTime getCreatedDate() { return createdDate; }
}
