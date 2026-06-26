package org.researchedc.module.dataimport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.researchedc.module.dataimport.enums.ImportJobStatus;
import org.researchedc.module.dataimport.enums.ImportType;

@Entity
@Table(name = "import_job")
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "import_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ImportType importType;

    @Column(name = "file_name", length = 500)
    private String fileName;

    @Column(name = "stored_file_path", length = 1000)
    private String storedFilePath;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ImportJobStatus status = ImportJobStatus.STAGED;

    @Column(name = "requested_by")
    private Integer requestedBy;

    @Column(name = "requested_date")
    private LocalDateTime requestedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    @Column(name = "summary_json", length = 4000)
    private String summaryJson;

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

    public ImportType getImportType() { return importType; }
    public void setImportType(ImportType importType) { this.importType = importType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStoredFilePath() { return storedFilePath; }
    public void setStoredFilePath(String storedFilePath) { this.storedFilePath = storedFilePath; }

    public ImportJobStatus getStatus() { return status; }
    public void setStatus(ImportJobStatus status) { this.status = status; }

    public Integer getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Integer requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getRequestedDate() { return requestedDate; }
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getSummaryJson() { return summaryJson; }
    public void setSummaryJson(String summaryJson) { this.summaryJson = summaryJson; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
}
