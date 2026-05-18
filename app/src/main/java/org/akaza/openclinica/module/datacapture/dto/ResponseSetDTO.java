package org.akaza.openclinica.module.datacapture.dto;

import java.util.List;

public class ResponseSetDTO {
    private Integer responseSetId;
    private Integer responseTypeId;
    private String label;
    private List<OptionDTO> options;

    public Integer getResponseSetId() { return responseSetId; }
    public void setResponseSetId(Integer v) { this.responseSetId = v; }
    public Integer getResponseTypeId() { return responseTypeId; }
    public void setResponseTypeId(Integer v) { this.responseTypeId = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public List<OptionDTO> getOptions() { return options; }
    public void setOptions(List<OptionDTO> v) { this.options = v; }

    public static class OptionDTO {
        private String text;
        private String value;

        public OptionDTO() {}
        public OptionDTO(String text, String value) { this.text = text; this.value = value; }

        public String getText() { return text; }
        public void setText(String v) { this.text = v; }
        public String getValue() { return value; }
        public void setValue(String v) { this.value = v; }
    }
}
