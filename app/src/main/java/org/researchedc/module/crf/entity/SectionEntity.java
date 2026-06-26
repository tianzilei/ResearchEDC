package org.researchedc.module.crf.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleSection")
@Table(name = "module_section")
public class SectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_section_seq")
    @SequenceGenerator(name = "module_section_seq", sequenceName = "module_section_id_seq", allocationSize = 1)
    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "crf_version_id")
    private Integer crfVersionId;

    @Column(name = "label", length = 2000)
    private String label;

    @Column(name = "title", length = 2000)
    private String title;

    @Column(name = "subtitle", length = 2000)
    private String subtitle;

    @Column(name = "instructions", length = 2000)
    private String instructions;

    @Column(name = "ordinal")
    private Integer ordinal;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "borders")
    private Integer borders;

    @Column(name = "page_number_label", length = 5)
    private String pageNumberLabel;

    public Integer getSectionId() { return sectionId; }
    public void setSectionId(Integer v) { this.sectionId = v; }
    public Integer getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(Integer v) { this.crfVersionId = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String v) { this.subtitle = v; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String v) { this.instructions = v; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer v) { this.ordinal = v; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer v) { this.parentId = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
    public Integer getBorders() { return borders; }
    public void setBorders(Integer v) { this.borders = v; }
    public String getPageNumberLabel() { return pageNumberLabel; }
    public void setPageNumberLabel(String v) { this.pageNumberLabel = v; }
}
