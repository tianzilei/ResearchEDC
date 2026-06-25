package org.researchedc.module.crf.dto;

public class CreateCrfVersionRequest {
    private String name;
    private String description;
    private String revisionNotes;

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getRevisionNotes() { return revisionNotes; }
    public void setRevisionNotes(String v) { this.revisionNotes = v; }
}
