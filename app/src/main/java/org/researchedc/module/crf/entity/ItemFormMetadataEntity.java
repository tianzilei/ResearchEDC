package org.researchedc.module.crf.entity;

import jakarta.persistence.*;

@Entity(name = "ModuleItemFormMetadata")
@Table(name = "module_item_form_metadata")
public class ItemFormMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_ifm_seq")
    @SequenceGenerator(name = "module_ifm_seq", sequenceName = "module_ifm_id_seq", allocationSize = 1)
    @Column(name = "item_form_metadata_id")
    private Integer itemFormMetadataId;

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "crf_version_id")
    private Integer crfVersionId;

    @Column(name = "ordinal")
    private Integer ordinal;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "regexp")
    private String regexp;

    @Column(name = "regexp_error_msg")
    private String regexpErrorMsg;

    @Column(name = "response_set_id")
    private Integer responseSetId;

    @Column(name = "response_layout")
    private String responseLayout;

    @Column(name = "width_decimal")
    private String widthDecimal;

    @Column(name = "show_item")
    private Boolean showItem;

    public Integer getItemFormMetadataId() { return itemFormMetadataId; }
    public void setItemFormMetadataId(Integer v) { this.itemFormMetadataId = v; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer v) { this.itemId = v; }
    public Integer getSectionId() { return sectionId; }
    public void setSectionId(Integer v) { this.sectionId = v; }
    public Integer getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(Integer v) { this.crfVersionId = v; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer v) { this.ordinal = v; }
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean v) { this.required = v; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String v) { this.defaultValue = v; }
    public String getRegexp() { return regexp; }
    public void setRegexp(String v) { this.regexp = v; }
    public String getRegexpErrorMsg() { return regexpErrorMsg; }
    public void setRegexpErrorMsg(String v) { this.regexpErrorMsg = v; }
    public Integer getResponseSetId() { return responseSetId; }
    public void setResponseSetId(Integer v) { this.responseSetId = v; }
    public String getResponseLayout() { return responseLayout; }
    public void setResponseLayout(String v) { this.responseLayout = v; }
    public String getWidthDecimal() { return widthDecimal; }
    public void setWidthDecimal(String v) { this.widthDecimal = v; }
    public Boolean getShowItem() { return showItem; }
    public void setShowItem(Boolean v) { this.showItem = v; }
}
