/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.core;

import java.util.ResourceBundle;

public class Status extends EntityBean {
    // waiting for the db to come in sync with our set of terms...
    public static final Status INVALID = new Status(0, "invalid");
    public static final Status AVAILABLE = new Status(1, "available");
    public static final Status UNAVAILABLE = new Status(2, "unavailable");
    public static final Status PRIVATE = new Status(3, "private");
    public static final Status PENDING = new Status(4, "pending");
    public static final Status DELETED = new Status(5, "removed");
    public static final Status LOCKED = new Status(6, "locked");
    public static final Status AUTO_DELETED = new Status(7, "auto-removed");
    public static final Status SIGNED = new Status(8, "signed");
    public static final Status FROZEN = new Status(9, "frozen");
    public static final Status SOURCE_DATA_VERIFICATION = new Status(10, "source_data_verification");
    public static final Status RESET = new Status(11, "reset");

    private static final Status[] members =
        { INVALID, AVAILABLE, PENDING, PRIVATE, UNAVAILABLE, LOCKED, DELETED, AUTO_DELETED, SIGNED, FROZEN, SOURCE_DATA_VERIFICATION,RESET };

    private Status(int id, String name) {
        setId(id);
        setName(name);
    }

    public static Status get(int id) {
        return find(id);
    }

    public static Status getFromMap(int id) {
        if (id < 0 || id >= members.length) {
            return Status.INVALID;
        }
        return get(id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String getName() {
        ResourceBundle resterm = ResourceBundle.getBundle("org.researchedc.i18n.terms");
        String localized = resterm.getString(this.name);
        return localized != null ? localized.trim() : "";
    }

    private static Status find(int id) {
        for (Status status : members) {
            if (status.getId() == id) {
                return status;
            }
        }
        return INVALID;
    }
}
