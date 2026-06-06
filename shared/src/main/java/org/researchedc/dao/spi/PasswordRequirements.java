package org.researchedc.dao.spi;

import java.util.List;
import java.util.Map;

public interface PasswordRequirements {
    String SPECIALS = "!@#$%&*()";

    Map<String, Object> configs();
    List<String> boolConfigKeys();
    List<String> intConfigKeys();
    void setHasLower(boolean hasLower);
    void setHasUpper(boolean hasUpper);
    void setHasDigits(boolean hasDigits);
    void setHasSpecials(boolean hasSpecials);
    void setMinLength(int minLen);
    void setMaxLength(int maxLen);
    void setExpirationDays(int expirationDays);
    void setChangeRequired(int changeRequired);
    boolean hasLower();
    boolean hasUpper();
    boolean hasDigits();
    boolean hasSpecials();
    boolean changeRequired();
    int minLength();
    int maxLength();
    int expirationDays();
}
