package org.researchedc.control.form.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.junit.jupiter.api.Test;
import org.researchedc.control.form.Validator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

class FormSupportIsolationTest {

    @Test
    void localeSupport_usesSessionLocaleBeforeRequestNegotiation() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(Locale.SIMPLIFIED_CHINESE);
        request.getSession(true).setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, Locale.JAPANESE);

        assertEquals(Locale.JAPANESE, FormLocaleSupport.getLocale(request));
    }

    @Test
    void localeSupport_fallsBackToDefaultWhenRequestLocaleIsNotQualified() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(Locale.SIMPLIFIED_CHINESE);

        assertEquals(Locale.ENGLISH, FormLocaleSupport.getLocale(request));
    }

    @Test
    void resourceBundleSupport_loadsRuntimeBundlesWithoutSharedProvider() {
        ResourceBundle wordsZh = FormResourceBundleSupport.getWordsBundle(Locale.SIMPLIFIED_CHINESE);
        ResourceBundle formatEn = FormResourceBundleSupport.getFormatBundle(Locale.ENGLISH);

        assertEquals("查找", wordsZh.getString("find"));
        assertEquals("dd-MMM-yyyy", formatEn.getString("date_format_string"));
    }

    @Test
    void validator_usesAppOwnedSupportForDateValidationErrors() {
        Validator validator = new Validator(Map.of("startDate", "31-Feb-2024"), Locale.ENGLISH);
        validator.addValidation("startDate", Validator.IS_A_DATE);

        HashMap errors = validator.validate();

        assertTrue(errors.containsKey("startDate"));
        assertTrue(errors.get("startDate").toString().contains("DD-MMM-YYYY"));
    }

    @Test
    void validator_usesAppOwnedTermAndComparisonSupport() {
        Validator validator = new Validator(
                Map.of("roleId", "2", "userTypeId", "3", "password", "abcd"),
                Locale.ENGLISH);
        validator.addValidation("roleId", Validator.IS_VALID_TERM, FormTermType.ROLE);
        validator.addValidation("userTypeId", Validator.IS_VALID_TERM, FormTermType.USER_TYPE);
        validator.addValidation(
                "password",
                Validator.LENGTH_NUMERIC_COMPARISON,
                NumericComparisonOperator.GREATER_THAN_OR_EQUAL_TO,
                4);

        assertTrue(validator.validate().isEmpty());
    }

    @Test
    void validator_rejectsRetiredSharedRoleAndUserTypeTerms() {
        Validator validator = new Validator(Map.of("roleId", "0", "userTypeId", "9"), Locale.ENGLISH);
        validator.addValidation("roleId", Validator.IS_VALID_TERM, FormTermType.ROLE);
        validator.addValidation("userTypeId", Validator.IS_VALID_TERM, FormTermType.USER_TYPE);

        HashMap errors = validator.validate();

        assertTrue(errors.containsKey("roleId"));
        assertTrue(errors.containsKey("userTypeId"));
    }
}
