package org.researchedc.app.dto;

import java.util.ResourceBundle;

public enum Status {
    INVALID(0, "invalid"),
    AVAILABLE(1, "available"),
    UNAVAILABLE(2, "unavailable"),
    PRIVATE(3, "private"),
    PENDING(4, "pending"),
    DELETED(5, "removed"),
    LOCKED(6, "locked"),
    AUTO_DELETED(7, "auto-removed"),
    SIGNED(8, "signed"),
    FROZEN(9, "frozen"),
    SOURCE_DATA_VERIFICATION(10, "source_data_verification"),
    RESET(11, "reset");

    private final int id;
    private final String name;

    Status(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        ResourceBundle resterm = ResourceBundle.getBundle("org.researchedc.i18n.terms");
        String localized = resterm.getString(this.name);
        return localized != null ? localized.trim() : "";
    }

    public static Status get(int id) {
        for (Status status : values()) {
            if (status.id == id) {
                return status;
            }
        }
        return INVALID;
    }

    public static Status getFromMap(int id) {
        if (id < 0 || id >= values().length) {
            return INVALID;
        }
        return get(id);
    }
}
