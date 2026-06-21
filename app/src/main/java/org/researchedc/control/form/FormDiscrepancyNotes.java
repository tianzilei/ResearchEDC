/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.control.form;

import java.util.HashMap;

/**
 * @author jxu
 *
 * 
 */
public class FormDiscrepancyNotes {
    private HashMap fieldNotes;
    private HashMap numExistingFieldNotes;

    public FormDiscrepancyNotes() {
        fieldNotes = new HashMap();
        numExistingFieldNotes = new HashMap();
    }

    public boolean hasNote(String field) {
        if (fieldNotes.containsKey(field)) {
            Object notes = fieldNotes.get(field);
            if (notes instanceof java.util.Collection collection) {
                return !collection.isEmpty();
            }
            return notes != null;
        }
        return false;
    }

    public void setNumExistingFieldNotes(String field, int num) {
        numExistingFieldNotes.put(field, Integer.valueOf(num));
    }

    public int getNumExistingFieldNotes(String field) {
        if (numExistingFieldNotes.containsKey(field)) {
            Integer numInt = (Integer) numExistingFieldNotes.get(field);
            if (numInt != null) {
                return numInt.intValue();
            }
        }
        return 0;
    }

    /**
     * @return Returns the numExistingFieldNotes.
     */
    public HashMap getNumExistingFieldNotes() {
        return numExistingFieldNotes;
    }

    /**
     * @return the fieldNotes
     */
    public HashMap getFieldNotes() {
        return fieldNotes;
    }

    /**
     * @param fieldNotes the fieldNotes to set
     */
    public void setFieldNotes(HashMap fieldNotes) {
        this.fieldNotes = fieldNotes;
    }

    /**
     * @param numExistingFieldNotes the numExistingFieldNotes to set
     */
    public void setNumExistingFieldNotes(HashMap numExistingFieldNotes) {
        this.numExistingFieldNotes = numExistingFieldNotes;
    }

}
