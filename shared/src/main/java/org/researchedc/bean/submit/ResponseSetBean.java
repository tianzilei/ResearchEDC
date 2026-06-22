/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.submit;

import org.researchedc.bean.core.EntityBean;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author ssachs
 */
public class ResponseSetBean extends EntityBean {
    public static final int RESPONSE_TYPE_TEXT = 1;
    public static final int RESPONSE_TYPE_CHECKBOX = 3;
    public static final int RESPONSE_TYPE_RADIO = 5;
    public static final int RESPONSE_TYPE_SELECT = 6;
    public static final int RESPONSE_TYPE_SELECT_MULTI = 7;

    private int responseTypeId;

    /**
     * A set of options to display to the user. The elements are
     * response option values.
     */
    private ArrayList<Option> options;

    /**
     * A HashMap which tells us, for a given value, what is the index in the
     * options array where the option with that value is stored? The keys are
     * values, the values are Integer objects.
     */
    private HashMap optionIndexesByValue;

    /**
     * Contains the value of the item if the item is a text input. Not in the
     * database.
     */
    private String value;

    public ResponseSetBean() {
        super();
        setResponseTypeId(RESPONSE_TYPE_TEXT);
        options = new ArrayList();
        optionIndexesByValue = new HashMap();
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return getName();
    }

    /**
     * @param label
     *            The label to set.
     */
    public void setLabel(String label) {
        setName(label);
    }

    /**
     * @return Returns the options.
     */
    public ArrayList<Option> getOptions() {
        return options;
    }

    /**
     * @return Returns the responseTypeId.
     */
    public int getResponseTypeId() {
        return responseTypeId;
    }

    /**
     * @param responseTypeId
     *            The responseTypeId to set.
     */
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
            optionIndexesByValue.put(value, Integer.valueOf(options.size() - 1));
        }

        return;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
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
