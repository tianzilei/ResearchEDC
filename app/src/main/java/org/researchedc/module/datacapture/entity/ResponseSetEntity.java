package org.researchedc.module.datacapture.entity;

import jakarta.persistence.*;

@Entity(name = "ModuleResponseSet")
@Table(name = "response_set")
public class ResponseSetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rs_seq")
    @SequenceGenerator(name = "rs_seq", sequenceName = "response_set_response_set_id_seq", allocationSize = 1)
    @Column(name = "response_set_id")
    private Integer responseSetId;

    @Column(name = "response_type_id")
    private Integer responseTypeId;

    @Column(length = 255)
    private String label;

    @Column(name = "options_text", columnDefinition = "TEXT")
    private String optionsText;

    @Column(name = "options_values", columnDefinition = "TEXT")
    private String optionsValues;

    @Column(name = "version_id")
    private Integer versionId;

    public Integer getResponseSetId() { return responseSetId; }
    public void setResponseSetId(Integer v) { this.responseSetId = v; }
    public Integer getResponseTypeId() { return responseTypeId; }
    public void setResponseTypeId(Integer v) { this.responseTypeId = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public String getOptionsText() { return optionsText; }
    public void setOptionsText(String v) { this.optionsText = v; }
    public String getOptionsValues() { return optionsValues; }
    public void setOptionsValues(String v) { this.optionsValues = v; }
    public Integer getVersionId() { return versionId; }
    public void setVersionId(Integer v) { this.versionId = v; }
}
