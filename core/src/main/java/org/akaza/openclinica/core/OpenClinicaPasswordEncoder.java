package org.akaza.openclinica.core;

import org.springframework.security.crypto.password.PasswordEncoder;

public class OpenClinicaPasswordEncoder implements PasswordEncoder {

    private PasswordEncoder currentPasswordEncoder;
    private PasswordEncoder oldPasswordEncoder;

    public OpenClinicaPasswordEncoder() {
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return currentPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        boolean result = false;
        if (currentPasswordEncoder.matches(rawPassword, encodedPassword) || 
            oldPasswordEncoder.matches(rawPassword, encodedPassword)) {
            result = true;
        }
        return result;
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return currentPasswordEncoder.upgradeEncoding(encodedPassword);
    }

    public PasswordEncoder getCurrentPasswordEncoder() {
        return currentPasswordEncoder;
    }

    public void setCurrentPasswordEncoder(PasswordEncoder currentPasswordEncoder) {
        this.currentPasswordEncoder = currentPasswordEncoder;
    }

    public PasswordEncoder getOldPasswordEncoder() {
        return oldPasswordEncoder;
    }

    public void setOldPasswordEncoder(PasswordEncoder oldPasswordEncoder) {
        this.oldPasswordEncoder = oldPasswordEncoder;
    }
}
