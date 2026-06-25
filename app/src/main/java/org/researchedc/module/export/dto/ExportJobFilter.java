package org.researchedc.module.export.dto;

import java.time.LocalDateTime;
import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.export.enums.OdmContractVersion;

public class ExportJobFilter {

    private ExportJobStatus status;
    private ExportFormat exportFormat;
    private OdmContractVersion odmContractVersion;
    private Integer requestedBy;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;

    public ExportJobStatus getStatus() { return status; }
    public void setStatus(ExportJobStatus status) { this.status = status; }

    public ExportFormat getExportFormat() { return exportFormat; }
    public void setExportFormat(ExportFormat exportFormat) { this.exportFormat = exportFormat; }

    public OdmContractVersion getOdmContractVersion() { return odmContractVersion; }
    public void setOdmContractVersion(OdmContractVersion odmContractVersion) { this.odmContractVersion = odmContractVersion; }

    public Integer getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Integer requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getCreatedAfter() { return createdAfter; }
    public void setCreatedAfter(LocalDateTime createdAfter) { this.createdAfter = createdAfter; }

    public LocalDateTime getCreatedBefore() { return createdBefore; }
    public void setCreatedBefore(LocalDateTime createdBefore) { this.createdBefore = createdBefore; }
}
