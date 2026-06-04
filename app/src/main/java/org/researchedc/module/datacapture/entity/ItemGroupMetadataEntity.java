package org.researchedc.module.datacapture.entity;

import jakarta.persistence.*;

@Entity(name = "ModuleItemGroupMetadata")
@Table(name = "module_item_group_metadata")
public class ItemGroupMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_igm_seq")
    @SequenceGenerator(name = "module_igm_seq", sequenceName = "module_item_group_metadata_id_seq", allocationSize = 1)
    @Column(name = "item_group_metadata_id")
    private Integer itemGroupMetadataId;

    @Column(name = "item_group_id")
    private Integer itemGroupId;

    @Column(name = "header")
    private String header;

    @Column(name = "subheader")
    private String subheader;

    @Column(name = "layout")
    private String layout;

    @Column(name = "repeat_number")
    private Integer repeatNumber;

    @Column(name = "repeat_max")
    private Integer repeatMax;

    @Column(name = "repeat_array")
    private String repeatArray;

    @Column(name = "row_start_number")
    private Integer rowStartNumber;

    @Column(name = "crf_version_id")
    private Integer crfVersionId;

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "ordinal")
    private Integer ordinal;

    @Column(name = "borders")
    private Integer borders;

    @Column(name = "show_group")
    private Boolean showGroup;

    @Column(name = "repeating_group")
    private Boolean repeatingGroup;

    public Integer getItemGroupMetadataId() { return itemGroupMetadataId; }
    public void setItemGroupMetadataId(Integer itemGroupMetadataId) { this.itemGroupMetadataId = itemGroupMetadataId; }
    public Integer getItemGroupId() { return itemGroupId; }
    public void setItemGroupId(Integer itemGroupId) { this.itemGroupId = itemGroupId; }
    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }
    public String getSubheader() { return subheader; }
    public void setSubheader(String subheader) { this.subheader = subheader; }
    public String getLayout() { return layout; }
    public void setLayout(String layout) { this.layout = layout; }
    public Integer getRepeatNumber() { return repeatNumber; }
    public void setRepeatNumber(Integer repeatNumber) { this.repeatNumber = repeatNumber; }
    public Integer getRepeatMax() { return repeatMax; }
    public void setRepeatMax(Integer repeatMax) { this.repeatMax = repeatMax; }
    public String getRepeatArray() { return repeatArray; }
    public void setRepeatArray(String repeatArray) { this.repeatArray = repeatArray; }
    public Integer getRowStartNumber() { return rowStartNumber; }
    public void setRowStartNumber(Integer rowStartNumber) { this.rowStartNumber = rowStartNumber; }
    public Integer getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(Integer crfVersionId) { this.crfVersionId = crfVersionId; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer ordinal) { this.ordinal = ordinal; }
    public Integer getBorders() { return borders; }
    public void setBorders(Integer borders) { this.borders = borders; }
    public Boolean getShowGroup() { return showGroup; }
    public void setShowGroup(Boolean showGroup) { this.showGroup = showGroup; }
    public Boolean getRepeatingGroup() { return repeatingGroup; }
    public void setRepeatingGroup(Boolean repeatingGroup) { this.repeatingGroup = repeatingGroup; }
}
