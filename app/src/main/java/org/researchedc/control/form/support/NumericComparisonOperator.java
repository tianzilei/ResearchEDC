package org.researchedc.control.form.support;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public enum NumericComparisonOperator {
    EQUALS("equal_to"),
    NOT_EQUALS("not_equal_to"),
    LESS_THAN("less_than"),
    LESS_THAN_OR_EQUAL_TO("less_than_or_equal_to"),
    GREATER_THAN("greater_than"),
    GREATER_THAN_OR_EQUAL_TO("greater_than_or_equal_to");

    private final String descriptionKey;

    NumericComparisonOperator(String descriptionKey) {
        this.descriptionKey = descriptionKey;
    }

    public String getDescription() {
        try {
            return ResourceBundle.getBundle("org.researchedc.i18n.terms").getString(descriptionKey).trim();
        } catch (MissingResourceException e) {
            return descriptionKey;
        }
    }
}
