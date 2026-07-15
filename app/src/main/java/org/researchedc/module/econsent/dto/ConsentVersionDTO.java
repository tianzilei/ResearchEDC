package org.researchedc.module.econsent.dto;

import java.time.LocalDateTime;
import org.researchedc.module.econsent.enums.ConsentVersionStatus;

public class ConsentVersionDTO {
    private Long id;
    private Long templateId;
    private Integer studyId;
    private String versionLabel;
    private String bodyText;
    private ConsentVersionStatus status;
    private LocalDateTime publishedDate;
    private Integer createdBy;
    private LocalDateTime createdDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }

    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }

    public ConsentVersionStatus getStatus() { return status; }
    public void setStatus(ConsentVersionStatus status) { this.status = status; }

    public LocalDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDateTime publishedDate) { this.publishedDate = publishedDate; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
