package org.researchedc.module.dataimport.dto;

import org.researchedc.module.dataimport.enums.ImportType;

public class CreateImportJobRequest {

    private Integer studyId;
    private String name;
    private ImportType importType;
    private String fileName;
    private String storedFilePath;
    private Integer requestedBy;

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

    public Integer getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Integer requestedBy) { this.requestedBy = requestedBy; }
}
