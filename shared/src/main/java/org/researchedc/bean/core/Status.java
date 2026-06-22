/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static List<Status> list = Arrays.asList(members);

    private static final Status[] activeMembers = { AVAILABLE, SIGNED, DELETED, AUTO_DELETED };
    private static List<Status> activeList = Arrays.asList(activeMembers);

    private static final Status[] studySubjectDropDownMembers = { AVAILABLE, SIGNED, DELETED, AUTO_DELETED };
    private static List<Status> studySubjectDropDownList = Arrays.asList(studySubjectDropDownMembers);

    private static final Status[] subjectDropDownMembers = { AVAILABLE, DELETED };
    private static List<Status> subjectDropDownList = Arrays.asList(subjectDropDownMembers);

    private static final Status[] studyUpdateMembers = { PENDING, AVAILABLE, FROZEN, LOCKED };
    private static List<Status> studyUpdateMembersList = Arrays.asList(studyUpdateMembers);

    //Solve the problem with the get() method...
    private static final Map<Integer, String> membersMap = new HashMap<Integer, String>();
    static {
        membersMap.put(0, "invalid");
        membersMap.put(1, "available");
        membersMap.put(2, "unavailable");
        membersMap.put(3, "private");
        membersMap.put(4, "pending");
        membersMap.put(5, "removed");
        membersMap.put(6, "locked");
        membersMap.put(7, "auto-removed");
        membersMap.put(8, "signed");
        membersMap.put(9, "frozen");
        membersMap.put(10, "source_data_verification");
        membersMap.put(11, "reset");
    }

    private Status(int id, String name) {
        setId(id);
        setName(name);
    }

    private Status() {
    }

    public static boolean contains(int id) {
        return find(id) != INVALID;
    }

    public static Status get(int id) {
        return find(id);
    }

    public static Status getFromMap(int id) {
        if (id < 0 || id > membersMap.size() - 1) {
            return Status.INVALID;
        }
        return get(id);
    }

    public boolean equals(Status status) {
        return status != null && id == status.id;
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
        for (Status status : list) {
            if (status.getId() == id) {
                return status;
            }
        }
        return INVALID;
    }

    /* public static void main(String[] args) {
         int[] nums = {0,1,2,3,4,5,6,7,8,9};
         Status stat;

         for(int tmp : nums){
              stat = (Status) get(tmp);
         }
     }*/

}
