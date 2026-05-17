package org.akaza.openclinica.module.crf.dto;

public class SectionDTO {
    private int sectionId;
    private int crfVersionId;
    private String label;
    private String title;
    private int ordinal;

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public int getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(int crfVersionId) { this.crfVersionId = crfVersionId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }
}
