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
    private int responseTypeId;

    private org.researchedc.bean.core.ResponseType responseType;

    /**
     * A set of options to display to the user. The elements are
     * ResponseOptionBean objects.
     */
    private ArrayList options;

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
        setResponseType(org.researchedc.bean.core.ResponseType.TEXT);
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
    public ArrayList getOptions() {
        return options;
    }

    /**
     * @return Returns the responseType.
     */
    public org.researchedc.bean.core.ResponseType getResponseType() {
        return responseType;
    }

    /**
     * @param responseType
     *            The responseType to set.
     */
    public void setResponseType(org.researchedc.bean.core.ResponseType responseType) {
        this.responseType = responseType;
    }

    /**
     * @return Returns the responseTypeId.
     */
    public int getResponseTypeId() {
        return responseType.getId();
    }

    /**
     * @param responseTypeId
     *            The responseTypeId to set.
     */
    public void setResponseTypeId(int responseTypeId) {
        responseType = org.researchedc.bean.core.ResponseType.get(responseTypeId);
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
            ResponseOptionBean ro = new ResponseOptionBean();

            if (values[i] == null) {
                continue;
            }

            String value = values[i].trim();
            value.replaceAll("##", ",");
            ro.setValue(value);

            if (texts.length <= i || texts[i] == null) {
                ro.setText(value);
            } else {
                String t = texts[i].trim();
                String t1 = t.replaceAll("##", ",");
                ro.setText(t1);
            }

            options.add(ro);
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
}