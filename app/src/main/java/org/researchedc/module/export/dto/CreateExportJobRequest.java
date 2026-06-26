package org.researchedc.module.export.dto;

import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.OdmContractVersion;

public class CreateExportJobRequest {

    private Integer studyId;
    private String name;
    private ExportFormat exportFormat;
    private OdmContractVersion odmContractVersion;
    private Integer requestedBy;
    private String criteriaJson;

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ExportFormat getExportFormat() { return exportFormat; }
    public void setExportFormat(ExportFormat exportFormat) { this.exportFormat = exportFormat; }

    public Integer getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Integer requestedBy) { this.requestedBy = requestedBy; }

    public String getCriteriaJson() { return criteriaJson; }
    public void setCriteriaJson(String criteriaJson) { this.criteriaJson = criteriaJson; }

    public OdmContractVersion getOdmContractVersion() { return odmContractVersion; }
    public void setOdmContractVersion(OdmContractVersion odmContractVersion) { this.odmContractVersion = odmContractVersion; }
}
