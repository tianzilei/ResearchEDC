package org.researchedc.module.studybuild.dto;

import org.researchedc.module.study.dto.StudyDetailDTO;

public class StudyTemplateApplicationDTO {
    private StudyTemplateDTO template;
    private StudyDetailDTO study;

    public StudyTemplateApplicationDTO() {
    }

    public StudyTemplateApplicationDTO(StudyTemplateDTO template, StudyDetailDTO study) {
        this.template = template;
        this.study = study;
    }

    public StudyTemplateDTO getTemplate() { return template; }
    public void setTemplate(StudyTemplateDTO template) { this.template = template; }
    public StudyDetailDTO getStudy() { return study; }
    public void setStudy(StudyDetailDTO study) { this.study = study; }
}
