package org.researchedc.app.dto;

public class StudyEventDefinitionDto extends AuditableEntity {
    private String description;
    private boolean repeating;
    private String category;
    private String type;
    private int studyId;
    private int ordinal;
    private String oid;

    public StudyEventDefinitionDto() {
        description = "";
        category = "";
        type = "";
        oid = "";
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isRepeating() { return repeating; }
    public void setRepeating(boolean repeating) { this.repeating = repeating; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getStudyId() { return studyId; }
    public void setStudyId(int studyId) { this.studyId = studyId; }
    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }
    public String getOid() { return oid; }
    public void setOid(String oid) { this.oid = oid; }
}
