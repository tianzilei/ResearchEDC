/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.submit;

import org.researchedc.bean.core.EntityBean;

import java.util.ArrayList;

/**
 * @author ssachs
 */
public class ItemFormMetadataBean extends EntityBean {
    public static class ResponseSetBean extends EntityBean {
        public static final int RESPONSE_TYPE_TEXT = 1;
        public static final int RESPONSE_TYPE_CHECKBOX = 3;
        public static final int RESPONSE_TYPE_RADIO = 5;
        public static final int RESPONSE_TYPE_SELECT = 6;
        public static final int RESPONSE_TYPE_SELECT_MULTI = 7;

        private int responseTypeId;
        private ArrayList<Option> options;
        private String value;

        public ResponseSetBean() {
            super();
            setResponseTypeId(RESPONSE_TYPE_TEXT);
            options = new ArrayList<>();
        }

        public String getLabel() {
            return getName();
        }

        public void setLabel(String label) {
            setName(label);
        }

        public ArrayList<Option> getOptions() {
            return options;
        }

        public int getResponseTypeId() {
            return responseTypeId;
        }

        public void setResponseTypeId(int responseTypeId) {
            this.responseTypeId = responseTypeId;
        }

        public void setOptions(String optionsText, String optionsValues) {
            String text1 = optionsText.replaceAll("\\\\,", "##");
            String value1 = optionsValues.replaceAll("\\\\,", "##");

            String[] texts = text1.split(",", -1);
            String[] values = value1.split(",", -1);

            if (values == null) {
                return;
            }

            if (texts == null) {
                texts = new String[0];
            }

            for (int i = 0; i < values.length; i++) {
                if (values[i] == null) {
                    continue;
                }

                String value = values[i].trim();
                value = value.replaceAll("##", ",");

                String text;
                if (texts.length <= i || texts[i] == null) {
                    text = value;
                } else {
                    String t = texts[i].trim();
                    text = t.replaceAll("##", ",");
                }

                options.add(new Option(text, value));
            }
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static class Option implements java.io.Serializable {
            private final String text;
            private final String value;

            public Option(String text, String value) {
                this.text = text;
                this.value = value;
            }

            public String getText() {
                return text;
            }

            public String getValue() {
                return value;
            }
        }
    }

    //
    private int itemId;
    private int crfVersionId;
    private String header;
    private String subHeader;
    private int parentId;
    private String parentLabel;
    private int columnNumber;
    private String pageNumberLabel;

    private String questionNumberLabel;
    private String leftItemText;
    private String rightItemText;
    private int sectionId;
    private int descisionConditionId;
    private int responseSetId;
    private String regexp;
    private String regexpErrorMsg;
    private int ordinal;
    private boolean required;
    // YW 08-01-2007, default_value has been added
    private String defaultValue;
    private String widthDecimal;
    
    private boolean showItem;
    private String responseLayout;

    /**
     * Not in the database. Not guaranteed to correspond to responseSetId,
     * although ItemFormDAO should take care of that correspondence.
     */
    private ResponseSetBean responseSet;
    
    
    public ItemFormMetadataBean() {
        itemId = 0;
        crfVersionId = 0;
        header = "";
        responseLayout = "";
        subHeader = "";
        parentId = 0;
        parentLabel = "";
        columnNumber = 1;
        pageNumberLabel = "";
        questionNumberLabel = "";
        leftItemText = "";
        rightItemText = "";
        sectionId = 0;
        descisionConditionId = 0;
        responseSetId = 0;
        regexp = "";
        regexpErrorMsg = "";
        ordinal = 0;
        required = false;
        showItem = true;
        defaultValue = "";
        responseSet = new ResponseSetBean();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * If more than one default, will remove all the spaces between each default
     * value
     *
     * @param defaults
     */
    public void setDefaultValue(String defaults) {

        if (defaults != null && !defaults.trim().isEmpty()) {
            String[] defaults2 = defaults.split(",", -1);

            for (int i = 0; i < defaults2.length; i++) {
                if (defaults2[i] == null) {
                    continue;
                }
                String t = defaults2[i].trim();

                this.defaultValue = defaultValue + t;
                if (i < defaults2.length - 1) {// don't want to add comma at
                    // the end, only in between
                    this.defaultValue = defaultValue + ",";
                }
            }
        } else {
            this.defaultValue = "";
        }

    }

    public boolean isShowItem() {
        return showItem;
    }

    public void setShowItem(boolean showItem) {
        this.showItem = showItem;
    }

    /**
     * @return Returns the columnNumber.
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * @param columnNumber
     *            The columnNumber to set.
     */
    public void setColumnNumber(int columnNumber) {
        if (columnNumber >= 1) {
            this.columnNumber = columnNumber;
        }
    }

    /**
     * @return Returns the crfVersionId.
     */
    public int getCrfVersionId() {
        return crfVersionId;
    }

    /**
     * @param crfVersionId
     *            The crfVersionId to set.
     */
    public void setCrfVersionId(int crfVersionId) {
        this.crfVersionId = crfVersionId;
    }

    /**
     * @return Returns the descisionConditionId.
     */
    public int getDescisionConditionId() {
        return descisionConditionId;
    }

    /**
     * @param descisionConditionId
     *            The descisionConditionId to set.
     */
    public void setDescisionConditionId(int descisionConditionId) {
        this.descisionConditionId = descisionConditionId;
    }

    /**
     * @return Returns the header.
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header
     *            The header to set.
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @return Returns the itemId.
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * @param itemId
     *            The itemId to set.
     */
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    /**
     * @return Returns the leftItemText.
     */
    public String getLeftItemText() {
        return leftItemText;
    }

    /**
     * @param leftItemText
     *            The leftItemText to set.
     */
    public void setLeftItemText(String leftItemText) {
        this.leftItemText = leftItemText;
    }

    /**
     * @return Returns the ordinal.
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * @param ordinal
     *            The ordinal to set.
     */
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    /**
     * @return Returns the pageNumberLabel.
     */
    public String getPageNumberLabel() {
        return pageNumberLabel;
    }

    /**
     * @param pageNumberLabel
     *            The pageNumberLabel to set.
     */
    public void setPageNumberLabel(String pageNumberLabel) {
        this.pageNumberLabel = pageNumberLabel;
    }

    /**
     * @return Returns the parentId.
     */
    public int getParentId() {
        return parentId;
    }

    /**
     * @param parentId
     *            The parentId to set.
     */
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    /**
     * @return Returns the parentLabel.
     */
    public String getParentLabel() {
        return parentLabel;
    }

    /**
     * @param parentLabel
     *            The parentLabel to set.
     */
    public void setParentLabel(String parentLabel) {
        this.parentLabel = parentLabel;
    }

    /**
     * @return Returns the questionNumberLabel.
     */
    public String getQuestionNumberLabel() {
        return questionNumberLabel;
    }

    /**
     * @param questionNumberLabel
     *            The questionNumberLabel to set.
     */
    public void setQuestionNumberLabel(String questionNumberLabel) {
        this.questionNumberLabel = questionNumberLabel;
    }

    /**
     * @return Returns the regexp.
     */
    public String getRegexp() {
        return regexp;
    }

    /**
     * @param regexp
     *            The regexp to set.
     */
    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    /**
     * @return Returns the regexpErrorMsg.
     */
    public String getRegexpErrorMsg() {
        return regexpErrorMsg;
    }

    /**
     * @param regexpErrorMsg
     *            The regexpErrorMsg to set.
     */
    public void setRegexpErrorMsg(String regexpErrorMsg) {
        this.regexpErrorMsg = regexpErrorMsg;
    }

    /**
     * @return Returns the responseSetId.
     */
    public int getResponseSetId() {
        return responseSetId;
    }

    /**
     * @param responseSetId
     *            The responseSetId to set.
     */
    public void setResponseSetId(int responseSetId) {
        this.responseSetId = responseSetId;
    }

    /**
     * @return Returns the rightItemText.
     */
    public String getRightItemText() {
        return rightItemText;
    }

    /**
     * @param rightItemText
     *            The rightItemText to set.
     */
    public void setRightItemText(String rightItemText) {
        this.rightItemText = rightItemText;
    }

    /**
     * @return Returns the sectionId.
     */
    public int getSectionId() {
        return sectionId;
    }

    /**
     * @param sectionId
     *            The sectionId to set.
     */
    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    /**
     * @return Returns the subHeader.
     */
    public String getSubHeader() {
        return subHeader;
    }

    /**
     * @param subHeader
     *            The subHeader to set.
     */
    public void setSubHeader(String subHeader) {
        this.subHeader = subHeader;
    }

    /**
     * @return Returns the responseSet.
     */
    public ResponseSetBean getResponseSet() {
        return responseSet;
    }

    /**
     * @param responseSet
     *            The responseSet to set.
     */
    public void setResponseSet(ResponseSetBean responseSet) {
        this.responseSet = responseSet;
    }

    /**
     * @return Returns the required.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required
     *            The required to set.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getResponseLayout() {
        return responseLayout;
    }

    public void setResponseLayout(String responseLayout) {
        this.responseLayout = responseLayout;
    }

    public String getWidthDecimal() {
        return widthDecimal;
    }

    public void setWidthDecimal(String widthDecimal) {
        this.widthDecimal = widthDecimal;
    }

}
