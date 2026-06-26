package org.researchedc.module.dataimport.dto;

import java.time.LocalDateTime;
import org.researchedc.module.dataimport.enums.ImportJobStatus;
import org.researchedc.module.dataimport.enums.ImportType;

public class ImportJobDTO {

    private Long id;
    private Integer studyId;
    private String name;
    private ImportType importType;
    private String fileName;
    private String storedFilePath;
    private ImportJobStatus status;
    private Integer requestedBy;
    private LocalDateTime requestedDate;
    private LocalDateTime completedDate;
    private String errorMessage;
    private String summaryJson;
    private Integer retryCount;

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
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getSummaryJson() { return summaryJson; }
    public void setSummaryJson(String summaryJson) { this.summaryJson = summaryJson; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
}
