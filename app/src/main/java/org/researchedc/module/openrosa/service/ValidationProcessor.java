package org.researchedc.module.openrosa.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ValidationProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(ValidationProcessor.class);

    private static final String DATE_FORMAT_DASHES = "yyyy-MM-dd";
    private static final String DATE_PARTIAL_YM = "yyyy-MM";
    private static final String DATE_PARTIAL_Y = "yyyy";
    private static final String DATE_REGEX_FULL = "\\d{4}-\\d{1,2}-\\d{1,2}";
    private static final String DATE_REGEX_YM = "\\d{4}-\\d{1,2}";
    private static final String DATE_REGEX_Y = "\\d{4}";

    @Override
    public void process(SubmissionContext ctx) {
        for (ItemValue item : ctx.getItems()) {
            Integer responseTypeId = item.getResponseTypeId();
            Integer dataTypeId = item.getItemDataTypeId();
            String value = item.getValue();

            if (value == null || value.isEmpty()) continue;
            if (dataTypeId == null) continue;

            if (responseTypeId != null && (responseTypeId == 3 || responseTypeId == 7)) {
                String[] values = value.split(",");
                for (String v : values) {
                    validateSingleValue(dataTypeId, v.trim(), item.getItemOid(), ctx);
                }
            } else {
                validateSingleValue(dataTypeId, value, item.getItemOid(), ctx);
            }
        }
    }

    private void validateSingleValue(Integer dataTypeId, String value, String itemOid, SubmissionContext ctx) {
        if (value == null || value.isEmpty()) return;

        switch (dataTypeId) {
            case 5: // STRING — max 3999 chars
                if (value.length() > 3999) {
                    ctx.addError("Item " + itemOid + ": value exceeds 3999 characters");
                    logger.info("{} — value.invalid.STRING — text over 3999 chars", itemOid);
                }
                break;

            case 6: // INTEGER
                try {
                    Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    ctx.addError("Item " + itemOid + ": invalid integer: " + value);
                    logger.info("{} — value.invalid.INTEGER: {}", itemOid, value);
                }
                break;

            case 7: // REAL
                try {
                    Float.valueOf(value);
                } catch (NumberFormatException e) {
                    ctx.addError("Item " + itemOid + ": invalid real number: " + value);
                    logger.info("{} — value.invalid.REAL: {}", itemOid, value);
                }
                break;

            case 9: // DATE — must be yyyy-MM-dd
                if (!isExactDate(value, DATE_FORMAT_DASHES, DATE_REGEX_FULL)) {
                    ctx.addError("Item " + itemOid + ": invalid date (expected yyyy-MM-dd): " + value);
                    logger.info("{} — value.invalid.DATE: {}", itemOid, value);
                }
                break;

            case 10: // PDATE — partial date: yyyy, yyyy-MM, or yyyy-MM-dd
                if (!isPartialDate(value)) {
                    ctx.addError("Item " + itemOid + ": invalid partial date: " + value);
                    logger.info("{} — value.invalid.PDATE: {}", itemOid, value);
                }
                break;

            case 11: // FILE — no validation needed
                break;

            default:
                break;
        }
    }

    private boolean isExactDate(String value, String format, String regex) {
        if (!value.matches(regex)) return false;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        try {
            sdf.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isPartialDate(String value) {
        if (value == null || value.isEmpty()) return true;
        if (isExactDate(value, DATE_FORMAT_DASHES, DATE_REGEX_FULL)) return true;
        if (isExactDate(value, DATE_PARTIAL_YM, DATE_REGEX_YM)) return true;
        if (isExactDate(value, DATE_PARTIAL_Y, DATE_REGEX_Y)) return true;
        return false;
    }
}
