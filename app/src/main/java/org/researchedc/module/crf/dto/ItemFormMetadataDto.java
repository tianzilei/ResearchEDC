package org.researchedc.module.crf.dto;

import org.researchedc.app.dto.Entity;

import java.util.ArrayList;

public class ItemFormMetadataDto extends Entity {
    private int itemId;
    private int crfVersionId;
    private int sectionId;
    private int ordinal;
    private boolean required;
    private String defaultValue;
    private String regexp;
    private String regexpErrorMsg;
    private String responseLayout;
    private String widthDecimal;
    private boolean showItem;
    private int responseSetId;
    private ResponseSetDto responseSet;

    public ItemFormMetadataDto() {
        defaultValue = "";
        regexp = "";
        regexpErrorMsg = "";
        responseLayout = "";
        widthDecimal = "";
        responseSet = new ResponseSetDto();
    }

    public int getItemId() { return itemId; }
    public void setItemId(int v) { this.itemId = v; }
    public int getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(int v) { this.crfVersionId = v; }
    public int getSectionId() { return sectionId; }
    public void setSectionId(int v) { this.sectionId = v; }
    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int v) { this.ordinal = v; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean v) { this.required = v; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String v) { this.defaultValue = v; }
    public String getRegexp() { return regexp; }
    public void setRegexp(String v) { this.regexp = v; }
    public String getRegexpErrorMsg() { return regexpErrorMsg; }
    public void setRegexpErrorMsg(String v) { this.regexpErrorMsg = v; }
    public String getResponseLayout() { return responseLayout; }
    public void setResponseLayout(String v) { this.responseLayout = v; }
    public String getWidthDecimal() { return widthDecimal; }
    public void setWidthDecimal(String v) { this.widthDecimal = v; }
    public boolean isShowItem() { return showItem; }
    public void setShowItem(boolean v) { this.showItem = v; }
    public int getResponseSetId() { return responseSetId; }
    public void setResponseSetId(int v) { this.responseSetId = v; }
    public ResponseSetDto getResponseSet() { return responseSet; }
    public void setResponseSet(ResponseSetDto v) { this.responseSet = v; }

    public static class ResponseSetDto extends Entity {
        public static final int RESPONSE_TYPE_TEXT = 1;
        public static final int RESPONSE_TYPE_CHECKBOX = 3;
        public static final int RESPONSE_TYPE_RADIO = 5;
        public static final int RESPONSE_TYPE_SELECT = 6;
        public static final int RESPONSE_TYPE_SELECT_MULTI = 7;

        private int responseTypeId;
        private ArrayList<Option> options;
        private String value;

        public ResponseSetDto() {
            setResponseTypeId(RESPONSE_TYPE_TEXT);
            options = new ArrayList<>();
        }

        public String getLabel() { return getName(); }
        public void setLabel(String label) { setName(label); }
        public ArrayList<Option> getOptions() { return options; }
        public int getResponseTypeId() { return responseTypeId; }
        public void setResponseTypeId(int v) { this.responseTypeId = v; }
        public String getValue() { return value; }
        public void setValue(String v) { this.value = v; }

        public void setOptions(String optionsText, String optionsValues) {
            String text1 = optionsText.replaceAll("\\\\,", "##");
            String value1 = optionsValues.replaceAll("\\\\,", "##");
            String[] texts = text1.split(",", -1);
            String[] values = value1.split(",", -1);
            if (values == null) return;
            if (texts == null) texts = new String[0];
            for (int i = 0; i < values.length; i++) {
                if (values[i] == null) continue;
                String val = values[i].trim().replaceAll("##", ",");
                String text = (texts.length <= i || texts[i] == null) ? val : texts[i].trim().replaceAll("##", ",");
                options.add(new Option(text, val));
            }
        }

        public static class Option implements java.io.Serializable {
            private final String text;
            private final String value;
            public Option(String text, String value) { this.text = text; this.value = value; }
            public String getText() { return text; }
            public String getValue() { return value; }
        }
    }
}
