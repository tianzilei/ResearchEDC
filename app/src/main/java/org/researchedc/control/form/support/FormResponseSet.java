package org.researchedc.control.form.support;

import java.util.ArrayList;
import java.util.List;

public record FormResponseSet(int responseTypeId, List<Option> options) {

    public static final int RESPONSE_TYPE_TEXT = 1;
    public static final int RESPONSE_TYPE_CHECKBOX = 3;
    public static final int RESPONSE_TYPE_RADIO = 5;
    public static final int RESPONSE_TYPE_SELECT = 6;
    public static final int RESPONSE_TYPE_SELECT_MULTI = 7;

    public FormResponseSet {
        options = options == null ? List.of() : List.copyOf(options);
    }

    public static FormResponseSet fromDelimitedOptions(
            Integer responseTypeId, String optionsText, String optionsValues) {
        if (responseTypeId == null) {
            return null;
        }
        return new FormResponseSet(responseTypeId, parseOptions(optionsText, optionsValues));
    }

    public boolean isSingleValueType() {
        return responseTypeId == RESPONSE_TYPE_RADIO || responseTypeId == RESPONSE_TYPE_SELECT;
    }

    public boolean isMultiValueType() {
        return responseTypeId == RESPONSE_TYPE_CHECKBOX || responseTypeId == RESPONSE_TYPE_SELECT_MULTI;
    }

    private static List<Option> parseOptions(String optionsText, String optionsValues) {
        if (optionsValues == null) {
            return List.of();
        }

        String normalizedText = optionsText == null ? "" : optionsText.replaceAll("\\\\,", "##");
        String normalizedValues = optionsValues.replaceAll("\\\\,", "##");

        String[] texts = normalizedText.split(",", -1);
        String[] values = normalizedValues.split(",", -1);
        List<Option> parsed = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                continue;
            }

            String value = values[i].trim().replaceAll("##", ",");
            String text = texts.length <= i || texts[i] == null
                    ? value
                    : texts[i].trim().replaceAll("##", ",");
            parsed.add(new Option(text, value));
        }
        return parsed;
    }

    public record Option(String text, String value) {
    }
}
