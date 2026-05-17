package org.akaza.openclinica.module.export.dto;

import java.time.LocalDateTime;
import org.akaza.openclinica.module.export.enums.ExportFormat;
import org.akaza.openclinica.module.export.enums.ExportJobStatus;

public class ExportJobDTO {

    private Long id;
    private Integer studyId;
    private String name;
    private ExportFormat exportFormat;
    private ExportJobStatus status;
    private Integer requestedBy;
    private LocalDateTime requestedDate;
    private LocalDateTime completedDate;
    private String filePath;
    private Long fileSize;
    private String errorMessage;
    private String criteriaJson;
    private Integer retryCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ExportFormat getExportFormat() { return exportFormat; }
    public void setExportFormat(ExportFormat exportFormat) { this.exportFormat = exportFormat; }

    public ExportJobStatus getStatus() { return status; }
    public void setStatus(ExportJobStatus status) { this.status = status; }

    public Integer getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Integer requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getCriteriaJson() { return criteriaJson; }
    public void setCriteriaJson(String criteriaJson) { this.criteriaJson = criteriaJson; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
}
