package org.researchedc.module.export.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.export.enums.OdmContractVersion;

@Entity
@Table(name = "export_job")
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "export_format", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExportFormat exportFormat;

    @Column(name = "odm_contract_version", length = 20)
    @Enumerated(EnumType.STRING)
    private OdmContractVersion odmContractVersion = OdmContractVersion.OC2_1;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExportJobStatus status = ExportJobStatus.PENDING;

    @Column(name = "requested_by")
    private Integer requestedBy;

    @Column(name = "requested_date")
    private LocalDateTime requestedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(nullable = false)
    private Boolean retryable = true;

    @Column(name = "criteria_json", length = 4000)
    private String criteriaJson;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @PrePersist
    protected void onCreate() {
        requestedDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ExportFormat getExportFormat() { return exportFormat; }
    public void setExportFormat(ExportFormat exportFormat) { this.exportFormat = exportFormat; }

    public OdmContractVersion getOdmContractVersion() { return odmContractVersion; }
    public void setOdmContractVersion(OdmContractVersion odmContractVersion) { this.odmContractVersion = odmContractVersion; }

    public ExportJobStatus getStatus() { return status; }
    public void setStatus(ExportJobStatus status) { this.status = status; }

    public Integer getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Integer requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getRequestedDate() { return requestedDate; }
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; }

    public Boolean getRetryable() { return retryable; }
    public void setRetryable(Boolean retryable) { this.retryable = retryable; }

    public String getCriteriaJson() { return criteriaJson; }
    public void setCriteriaJson(String criteriaJson) { this.criteriaJson = criteriaJson; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
}
